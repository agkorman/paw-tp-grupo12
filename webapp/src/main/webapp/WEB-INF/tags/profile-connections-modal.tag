<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="followingUsers" required="true" type="java.util.List" %>
<%@ attribute name="followerUsers" required="true" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="profileConnectionsModal" class="profile-modal" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-connections-dialog" role="dialog" aria-modal="true" aria-labelledby="profileConnectionsTitle">
        <header class="profile-modal-header profile-connections-header">
            <h2 id="profileConnectionsTitle" data-connections-title>Seguidos</h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="Cerrar modal">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" aria-hidden="true">
                    <path d="M18 6 6 18M6 6l12 12"/>
                </svg>
            </button>
        </header>

        <div class="profile-connections-search">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" aria-hidden="true">
                <circle cx="11" cy="11" r="7"></circle>
                <path d="m20 20-3.5-3.5"></path>
            </svg>
            <input type="search" placeholder="Buscar" aria-label="Buscar usuarios" data-connections-search>
        </div>

        <div class="profile-connections-list" data-connections-list="following">
            <c:forEach var="user" items="${followingUsers}">
                <article class="profile-connection-row" data-connection-row data-search-text="${user.username}">
                    <div class="profile-connection-avatar" aria-hidden="true">
                        <span><c:out value="${user.initials}"/></span>
                    </div>
                    <div class="profile-connection-copy">
                        <strong><c:out value="${user.username}"/></strong>
                    </div>
                    <button
                            type="button"
                            class="profile-connection-button ${user.following ? 'is-following' : ''}"
                            data-follow-toggle
                            data-following="${user.following}"
                            aria-pressed="${user.following}">
                        <c:choose>
                            <c:when test="${user.following}">Siguiendo</c:when>
                            <c:otherwise>Seguir</c:otherwise>
                        </c:choose>
                    </button>
                </article>
            </c:forEach>
        </div>

        <div class="profile-connections-list" data-connections-list="followers" hidden>
            <c:forEach var="user" items="${followerUsers}">
                <article class="profile-connection-row" data-connection-row data-search-text="${user.username}">
                    <div class="profile-connection-avatar" aria-hidden="true">
                        <span><c:out value="${user.initials}"/></span>
                    </div>
                    <div class="profile-connection-copy">
                        <strong><c:out value="${user.username}"/></strong>
                    </div>
                    <button
                            type="button"
                            class="profile-connection-button ${user.following ? 'is-following' : ''}"
                            data-follow-toggle
                            data-following="${user.following}"
                            aria-pressed="${user.following}">
                        <c:choose>
                            <c:when test="${user.following}">Siguiendo</c:when>
                            <c:otherwise>Seguir</c:otherwise>
                        </c:choose>
                    </button>
                </article>
            </c:forEach>
        </div>
    </section>
</div>
