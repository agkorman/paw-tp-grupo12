package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.AuthenticationTokens;
import ar.edu.itba.paw.webapp.api.common.ApiAuthentication;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/** Keeps a committed rotation deliverable even if the resource replaces/reset its response. */
final class TokenResponseWrapper extends HttpServletResponseWrapper {

    private final String access;
    private final String refresh;
    private final String links;

    TokenResponseWrapper(final HttpServletResponse response, final String contextPath,
                         final AuthenticationTokens tokens) {
        super(response);
        access = tokens.getAccessToken();
        refresh = tokens.getSession().getRefreshToken();
        final String user = contextPath + "/api/users/" + tokens.getSession().getUserId();
        links = "<" + user + ">; rel=\"" + ApiAuthentication.USER_RELATION + "\", <"
                + user + "/sessions/" + tokens.getSession().getSessionId() + ">; rel=\""
                + ApiAuthentication.SESSION_RELATION + "\"";
        restore();
    }

    @Override
    public void reset() {
        super.reset();
        restore();
    }

    @Override
    public void setHeader(final String name, final String value) {
        if ("Link".equalsIgnoreCase(name)) {
            super.setHeader(name, value == null || value.isEmpty() ? links : links + ", " + value);
        } else if (!isProtected(name)) {
            super.setHeader(name, value);
        }
    }

    @Override
    public void addHeader(final String name, final String value) {
        if (!isProtected(name)) {
            super.addHeader(name, value);
        }
    }

    private boolean isProtected(final String name) {
        return "Cache-Control".equalsIgnoreCase(name)
                || ApiAuthentication.ACCESS_TOKEN_HEADER.equalsIgnoreCase(name)
                || ApiAuthentication.REFRESH_TOKEN_HEADER.equalsIgnoreCase(name);
    }

    private void restore() {
        super.setHeader(ApiAuthentication.ACCESS_TOKEN_HEADER, access);
        super.setHeader(ApiAuthentication.REFRESH_TOKEN_HEADER, refresh);
        super.setHeader("Cache-Control", "no-store");
        super.setHeader("Link", links);
    }
}
