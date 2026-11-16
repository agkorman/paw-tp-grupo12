package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.AccessTokenIdentity;
import ar.edu.itba.paw.model.AccessTokenResult;
import ar.edu.itba.paw.model.AuthenticationTokens;
import ar.edu.itba.paw.model.TokenAuthenticationResult;
import ar.edu.itba.paw.services.AccessTokenService;
import ar.edu.itba.paw.services.TokenAuthenticationService;
import ar.edu.itba.paw.webapp.api.common.ApiAuthentication;
import ar.edu.itba.paw.webapp.api.common.ApiError;
import ar.edu.itba.paw.webapp.api.common.ApiSecurityErrorHandler;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** HTTP credential parsing only; authentication use cases remain in services. */
public class ApiAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAuthenticationFilter.class);

    private final TokenAuthenticationService authentication;
    private final AccessTokenService accessTokens;
    private final ApiSecurityErrorHandler errors;

    public ApiAuthenticationFilter(final TokenAuthenticationService authentication,
                                    final AccessTokenService accessTokens, final ApiSecurityErrorHandler errors) {
        this.authentication = authentication;
        this.accessTokens = accessTokens;
        this.errors = errors;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain chain) throws ServletException, IOException {
        final HttpServletResponse authenticatedResponse;
        try {
            authenticatedResponse = authenticate(request, response);
        } catch (final RuntimeException e) {
            LOGGER.error("API authentication failed unexpectedly", e);
            errors.write(response, 500, new ApiError("INTERNAL_ERROR", "Internal server error"));
            return;
        }
        if (authenticatedResponse != null) {
            // Jersey's exception mappers normally handle resource failures. This also covers an
            // unmapped servlet failure, preserving newly issued credentials in the JSON response.
            try {
                chain.doFilter(request, authenticatedResponse);
            } catch (final ServletException | RuntimeException e) {
                if (authenticatedResponse.isCommitted()) {
                    throw e;
                }
                LOGGER.error("API request failed after authentication", e);
                authenticatedResponse.resetBuffer();
                errors.write(authenticatedResponse, 500, new ApiError("INTERNAL_ERROR", "Internal server error"));
            }
        }
    }

    private HttpServletResponse authenticate(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        final List<String> headers = Collections.list(request.getHeaders("Authorization"));
        if (headers.isEmpty()) {
            return response;
        }
        if (headers.size() != 1) {
            return reject(response, ApiAuthentication.INVALID_CREDENTIALS);
        }
        final String header = headers.get(0);
        final int separator = header.indexOf(' ');
        if (separator < 1 || separator == header.length() - 1) {
            return reject(response, ApiAuthentication.INVALID_CREDENTIALS);
        }
        final String scheme = header.substring(0, separator);
        final String credential = header.substring(separator + 1);
        if ("Basic".equalsIgnoreCase(scheme)) {
            // Passwords are only used on the agreed entry resource, never to execute mutations.
            final String path = request.getRequestURI().substring(request.getContextPath().length());
            if (!"GET".equals(request.getMethod()) || !("/api".equals(path) || "/api/".equals(path))) {
                return reject(response, ApiAuthentication.INVALID_CREDENTIALS);
            }
            final List<String> remember = Collections.list(request.getHeaders(ApiAuthentication.REMEMBER_ME_HEADER));
            if (remember.size() > 1 || (remember.size() == 1
                    && !"true".equals(remember.get(0)) && !"false".equals(remember.get(0)))) {
                errors.write(response, 400, new ApiError("INVALID_REQUEST", "X-Remember-Me must be true or false"));
                return null;
            }
            final String decoded;
            try {
                decoded = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(Base64.getDecoder().decode(credential))).toString();
            } catch (final IllegalArgumentException | CharacterCodingException e) {
                return reject(response, ApiAuthentication.INVALID_CREDENTIALS);
            }
            final int colon = decoded.indexOf(':');
            if (colon <= 0 || colon == decoded.length() - 1) {
                return reject(response, ApiAuthentication.INVALID_CREDENTIALS);
            }
            return grant(request, response, authentication.authenticate(decoded.substring(0, colon),
                    decoded.substring(colon + 1), !remember.isEmpty() && "true".equals(remember.get(0))));
        }
        if (!"Bearer".equalsIgnoreCase(scheme)) {
            return reject(response, ApiAuthentication.INVALID_CREDENTIALS);
        }
        if (credential.startsWith("rt_")) {
            return grant(request, response, authentication.renew(credential));
        }
        final AccessTokenResult result = accessTokens.validate(credential);
        if (result.getStatus() != AccessTokenResult.Status.VALID) {
            return reject(response, result.getStatus() == AccessTokenResult.Status.EXPIRED
                    ? ApiAuthentication.TOKEN_EXPIRED : ApiAuthentication.INVALID_TOKEN);
        }
        final AccessTokenIdentity identity = result.getIdentity().orElseThrow();
        setIdentity(identity.getUserId(), identity.getSessionId(), identity.getRoles());
        return response;
    }

    private HttpServletResponse grant(final HttpServletRequest request, final HttpServletResponse response,
                                     final TokenAuthenticationResult result) throws IOException {
        if (result.getStatus() != TokenAuthenticationResult.Status.AUTHENTICATED) {
            return reject(response, result.getStatus().name());
        }
        final AuthenticationTokens tokens = result.getTokens().orElseThrow();
        setIdentity(tokens.getSession().getUserId(), tokens.getSession().getSessionId(), tokens.getRoles());
        return new TokenResponseWrapper(response, request.getContextPath(), tokens);
    }

    private HttpServletResponse reject(final HttpServletResponse response, final String code) throws IOException {
        errors.unauthorized(response, code);
        return null;
    }

    private void setIdentity(final long userId, final long sessionId, final Set<String> roles) {
        final SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(new ApiPrincipal(userId, sessionId), null,
                roles.stream().map(role -> new SimpleGrantedAuthority(
                        RoleAuthorityUtils.toAuthority(role))).toList()));
        SecurityContextHolder.setContext(context);
    }
}
