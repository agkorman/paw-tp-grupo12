<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="followingUsers" required="true" type="java.util.List" %>
<%@ attribute name="followerUsers" required="true" type="java.util.List" %>
<%@ attribute name="activeKind" required="false" type="java.lang.String" %>
<%@ attribute name="pagination" required="false" type="ar.edu.itba.paw.webapp.controller.ProfileController.ConnectionsPagination" %>
<%@ attribute name="profileBasePath" required="true" type="java.lang.String" %>
<%@ attribute name="activeTab" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
<c:set var="modalOpen" value="${activeKind eq 'followers' or activeKind eq 'following'}"/>
<c:set var="activeKindFollowers" value="${activeKind eq 'followers'}"/>
<spring:message var="followingTabLabel" code="profile.following"/>
<spring:message var="followersTabLabel" code="profile.followers"/>
<spring:message var="modalTitle" code="profile.connections.title"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="followLabel" code="common.label.follow"/>
<spring:message var="followingLabel" code="common.label.following"/>
<spring:message var="followingEmpty" code="profile.connections.following.empty"/>
<spring:message var="followersEmpty" code="profile.connections.followers.empty"/>
<spring:message var="prevLabel" code="profile.connections.pagination.previous"/>
<spring:message var="nextLabel" code="profile.connections.pagination.next"/>
<spring:message var="followUserAction" code="profile.authRequired.followAction"/>

<c:url var="followingTabUrl" value="${profileBasePath}">
    <c:param name="modal" value="following"/>
    <c:param name="followingPage" value="1"/>
    <c:if test="${not empty activeTab}">
        <c:param name="tab" value="${activeTab}"/>
    </c:if>
</c:url>
<c:url var="followersTabUrl" value="${profileBasePath}">
    <c:param name="modal" value="followers"/>
    <c:param name="followersPage" value="1"/>
    <c:if test="${not empty activeTab}">
        <c:param name="tab" value="${activeTab}"/>
    </c:if>
</c:url>

<div id="profileConnectionsModal" class="profile-modal" hidden ${modalOpen ? 'data-open-on-load="true"' : ''}>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-connections-dialog" role="dialog" aria-modal="true" aria-labelledby="profileConnectionsTitle">
        <header class="profile-modal-header profile-connections-header">
            <h2 id="profileConnectionsTitle"><c:out value="${modalTitle}"/></h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="20"/>
            </button>
        </header>

        <nav class="profile-connections-tabs" aria-label="${modalTitle}">
            <a href="${followingTabUrl}#profileConnectionsModal"
               class="profile-connections-tab ${activeKind eq 'following' ? 'is-active' : ''}"
               aria-current="${activeKind eq 'following' ? 'page' : 'false'}">
                <c:out value="${followingTabLabel}"/>
            </a>
            <a href="${followersTabUrl}#profileConnectionsModal"
               class="profile-connections-tab ${activeKind eq 'followers' ? 'is-active' : ''}"
               aria-current="${activeKind eq 'followers' ? 'page' : 'false'}">
                <c:out value="${followersTabLabel}"/>
            </a>
        </nav>

        <c:choose>
            <c:when test="${activeKind eq 'following'}">
                <c:set var="connectionUsers" value="${followingUsers}"/>
                <c:set var="emptyMessage" value="${followingEmpty}"/>
            </c:when>
            <c:when test="${activeKind eq 'followers'}">
                <c:set var="connectionUsers" value="${followerUsers}"/>
                <c:set var="emptyMessage" value="${followersEmpty}"/>
            </c:when>
            <c:otherwise>
                <c:set var="connectionUsers" value="${null}"/>
                <c:set var="emptyMessage" value=""/>
            </c:otherwise>
        </c:choose>

        <div class="profile-connections-list" data-connections-list="${activeKind}">
            <c:choose>
                <c:when test="${empty connectionUsers}">
                    <p class="profile-connections-empty"><c:out value="${emptyMessage}"/></p>
                </c:when>
                <c:otherwise>
                    <c:forEach var="user" items="${connectionUsers}">
                        <c:url var="connectionProfileUrl" value="/profiles/${user.id}"/>
                        <article class="profile-connection-row">
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
                                            <button type="submit"
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
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${not empty pagination and pagination.totalPages gt 1}">
            <nav class="profile-connections-pagination" aria-label="${modalTitle}">
                <c:set var="pageParamName" value="${pagination.kind eq 'followers' ? 'followersPage' : 'followingPage'}"/>

                <c:if test="${pagination.hasPrevious}">
                    <c:url var="prevPageUrl" value="${profileBasePath}">
                        <c:param name="modal" value="${pagination.kind}"/>
                        <c:param name="${pageParamName}" value="${pagination.currentPage - 1}"/>
                        <c:if test="${not empty activeTab}">
                            <c:param name="tab" value="${activeTab}"/>
                        </c:if>
                    </c:url>
                    <a class="profile-connections-pagination-link" href="${prevPageUrl}#profileConnectionsModal">
                        <c:out value="${prevLabel}"/>
                    </a>
                </c:if>

                <c:forEach var="i" begin="1" end="${pagination.totalPages}">
                    <c:url var="numberedPageUrl" value="${profileBasePath}">
                        <c:param name="modal" value="${pagination.kind}"/>
                        <c:param name="${pageParamName}" value="${i}"/>
                        <c:if test="${not empty activeTab}">
                            <c:param name="tab" value="${activeTab}"/>
                        </c:if>
                    </c:url>
                    <a class="profile-connections-pagination-link ${i eq pagination.currentPage ? 'is-current' : ''}"
                       href="${numberedPageUrl}#profileConnectionsModal"
                       aria-current="${i eq pagination.currentPage ? 'page' : 'false'}">
                        <c:out value="${i}"/>
                    </a>
                </c:forEach>

                <c:if test="${pagination.hasNext}">
                    <c:url var="nextPageUrl" value="${profileBasePath}">
                        <c:param name="modal" value="${pagination.kind}"/>
                        <c:param name="${pageParamName}" value="${pagination.currentPage + 1}"/>
                        <c:if test="${not empty activeTab}">
                            <c:param name="tab" value="${activeTab}"/>
                        </c:if>
                    </c:url>
                    <a class="profile-connections-pagination-link" href="${nextPageUrl}#profileConnectionsModal">
                        <c:out value="${nextLabel}"/>
                    </a>
                </c:if>
            </nav>
        </c:if>
    </section>
</div>
