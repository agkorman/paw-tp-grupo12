<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="followingUsers" required="true" type="java.util.List" %>
<%@ attribute name="followerUsers" required="true" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
<spring:message var="followingTitle" code="profile.connections.title"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="searchPlaceholder" code="profile.connections.search.placeholder"/>
<spring:message var="searchAria" code="profile.connections.search.aria"/>
<spring:message var="searchEmpty" code="profile.connections.search.empty"/>
<spring:message var="followUserAction" code="profile.authRequired.followAction"/>
<spring:message var="followLabel" code="common.label.follow"/>
<spring:message var="followingLabel" code="common.label.following"/>

<div id="profileConnectionsModal" class="profile-modal" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-connections-dialog" role="dialog" aria-modal="true" aria-labelledby="profileConnectionsTitle">
        <header class="profile-modal-header profile-connections-header">
            <h2 id="profileConnectionsTitle" data-connections-title><c:out value="${followingTitle}"/></h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="20"/>
            </button>
        </header>

        <div class="profile-connections-search">
            <pa:icon name="search" size="18"/>
            <input type="search" maxlength="80" placeholder="${searchPlaceholder}" aria-label="${searchAria}" data-connections-search>
        </div>

        <div class="profile-connections-list" data-connections-list="following">
            <c:forEach var="user" items="${followingUsers}">
                <c:url var="connectionProfileUrl" value="/profiles/${user.id}"/>
                <article class="profile-connection-row" data-connection-row data-search-text="${fn:escapeXml(user.username)}">
                    <a class="profile-connection-avatar" href="${connectionProfileUrl}" aria-hidden="true" tabindex="-1">
                        <pa:icon name="user-avatar" size="24"/>
                    </a>
                    <div class="profile-connection-copy">
                        <a class="profile-connection-name" href="${connectionProfileUrl}">
                            <strong><c:out value="${user.username}"/></strong>
                        </a>
                    </div>
                    <c:if test="${user.followable}">
                        <c:url var="connectionFollowUrl" value="/profiles/${user.id}/follow"/>
                        <c:choose>
                            <c:when test="${authenticated}">
                                <form class="profile-connection-follow-form"
                                      method="post"
                                      action="${connectionFollowUrl}"
                                      data-enhanced-follow="true"
                                      data-follow-user-id="${user.id}"
                                      data-auth-resume-intent="follow-profile-${user.id}">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button
                                            type="submit"
                                            class="profile-connection-button ${user.following ? 'is-following' : ''}"
                                            aria-pressed="${user.following}"
                                            data-follow-toggle
                                            data-follow-user-id="${user.id}"
                                            data-follow-label="${fn:escapeXml(followLabel)}"
                                            data-following-label="${fn:escapeXml(followingLabel)}">
                                        <c:choose>
                                            <c:when test="${user.following}"><spring:message code="common.label.following"/></c:when>
                                            <c:otherwise><spring:message code="common.label.follow"/></c:otherwise>
                                        </c:choose>
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <c:url var="connectionFollowLoginUrl" value="/login">
                                    <c:param name="redirect" value="/profiles/${user.id}"/>
                                    <c:param name="intent" value="follow-profile-${user.id}"/>
                                </c:url>
                                <a href="${connectionFollowLoginUrl}"
                                   class="profile-connection-button"
                                   data-auth-resume-intent="follow-profile-${user.id}"
                                   data-auth-required="true"
                                   data-auth-required-action="${followUserAction}"
                                   data-auth-required-intent="follow-profile-${user.id}">
                                    <spring:message code="common.label.follow"/>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </article>
            </c:forEach>
            <p class="profile-connections-empty" data-connections-empty hidden><c:out value="${searchEmpty}"/></p>
        </div>

        <div class="profile-connections-list" data-connections-list="followers" hidden>
            <c:forEach var="user" items="${followerUsers}">
                <c:url var="connectionProfileUrl" value="/profiles/${user.id}"/>
                <article class="profile-connection-row" data-connection-row data-search-text="${fn:escapeXml(user.username)}">
                    <a class="profile-connection-avatar" href="${connectionProfileUrl}" aria-hidden="true" tabindex="-1">
                        <pa:icon name="user-avatar" size="24"/>
                    </a>
                    <div class="profile-connection-copy">
                        <a class="profile-connection-name" href="${connectionProfileUrl}">
                            <strong><c:out value="${user.username}"/></strong>
                        </a>
                    </div>
                    <c:if test="${user.followable}">
                        <c:url var="connectionFollowUrl" value="/profiles/${user.id}/follow"/>
                        <c:choose>
                            <c:when test="${authenticated}">
                                <form class="profile-connection-follow-form"
                                      method="post"
                                      action="${connectionFollowUrl}"
                                      data-enhanced-follow="true"
                                      data-follow-user-id="${user.id}"
                                      data-auth-resume-intent="follow-profile-${user.id}">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button
                                            type="submit"
                                            class="profile-connection-button ${user.following ? 'is-following' : ''}"
                                            aria-pressed="${user.following}"
                                            data-follow-toggle
                                            data-follow-user-id="${user.id}"
                                            data-follow-label="${fn:escapeXml(followLabel)}"
                                            data-following-label="${fn:escapeXml(followingLabel)}">
                                        <c:choose>
                                            <c:when test="${user.following}"><spring:message code="common.label.following"/></c:when>
                                            <c:otherwise><spring:message code="common.label.follow"/></c:otherwise>
                                        </c:choose>
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <c:url var="connectionFollowLoginUrl" value="/login">
                                    <c:param name="redirect" value="/profiles/${user.id}"/>
                                    <c:param name="intent" value="follow-profile-${user.id}"/>
                                </c:url>
                                <a href="${connectionFollowLoginUrl}"
                                   class="profile-connection-button"
                                   data-auth-resume-intent="follow-profile-${user.id}"
                                   data-auth-required="true"
                                   data-auth-required-action="${followUserAction}"
                                   data-auth-required-intent="follow-profile-${user.id}">
                                    <spring:message code="common.label.follow"/>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </article>
            </c:forEach>
            <p class="profile-connections-empty" data-connections-empty hidden><c:out value="${searchEmpty}"/></p>
        </div>
    </section>
</div>
