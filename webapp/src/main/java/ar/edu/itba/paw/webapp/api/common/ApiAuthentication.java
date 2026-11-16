package ar.edu.itba.paw.webapp.api.common;

/**
 * Wire contract shared by API authentication and its clients.
 * Basic credentials contain the email and password. Successful authentication and refresh
 * rotation emit both token headers and Link headers using the extension relations below.
 * Ordinary access-token requests do not emit tokens. Authentication responses are no-store.
 * The relations identify canonical resources, never a caller-dependent /me or /current alias.
 *
 * Remember-me defaults to false and only selects the initial session lifetime. Rotation
 * preserves its absolute expiry. Only TOKEN_EXPIRED permits one coordinated retry of the
 * rejected request with a refresh token, before resource execution. A consumed refresh
 * revokes its entire session chain; other sessions are unaffected. DELETE of the linked
 * session accepts its current refresh without rotating it. Issued access tokens expire
 * naturally even after revocation.
 */
public final class ApiAuthentication {

    public static final String ACCESS_TOKEN_HEADER = "X-Access-Token";
    public static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
    public static final String REMEMBER_ME_HEADER = "X-Remember-Me";
    /** Internal request attribute, never a client-controlled header or an authenticated identity. */
    public static final String REVOCATION_REFRESH_ATTRIBUTE = ApiAuthentication.class.getName() + ".revocationRefresh";

    // RFC 8288 extension relation types use absolute URIs; these are identifiers, not endpoints.
    public static final String USER_RELATION = "urn:paw:relation:authenticated-user";
    public static final String SESSION_RELATION = "urn:paw:relation:authenticated-session";

    public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String SESSION_EXPIRED = "SESSION_EXPIRED";
    public static final String SESSION_REVOKED = "SESSION_REVOKED";
    public static final String REFRESH_TOKEN_REUSED = "REFRESH_TOKEN_REUSED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";

    private ApiAuthentication() { }
}
