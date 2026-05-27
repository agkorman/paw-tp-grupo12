<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${pageTitle}" styles="/css/community-detail.css|/css/community-members.css|/css/communities-responsive.css|/css/profile-modal.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="communityDetailUrl" value="/communities/${community.slug}"/>
    <spring:message var="memberActionMenuLabel" code="communities.members.actionMenu.label"/>

    <main class="communities-page">
        <section class="community-members-shell">
            <header class="community-members-header">
                <a class="community-members-back" href="${fn:escapeXml(communityDetailUrl)}">
                    <pa:icon name="chevron-left" size="16"/>
                    <span><c:out value="${community.name}"/></span>
                </a>
                <p class="community-section-kicker"><spring:message code="communities.members.kicker"/></p>
                <h1 class="community-section-title"><spring:message code="communities.members.title"/></h1>
                <p class="community-members-count">
                    <spring:message code="communities.members.count" arguments="${fn:length(memberRows)}"/>
                </p>
            </header>

            <ul class="community-members-list" role="list">
                <c:forEach var="row" items="${memberRows}">
                    <li class="community-member-card">
                        <span class="community-member-avatar" aria-hidden="true">
                            <c:out value="${fn:substring(row.username, 0, 1)}"/>
                        </span>
                        <div class="community-member-info">
                            <c:url var="memberProfileUrl" value="/users/${row.userId}"/>
                            <a class="community-member-name"
                               href="${fn:escapeXml(memberProfileUrl)}">
                                <c:out value="${row.username}"/>
                            </a>
                            <div class="community-member-badges">
                                <c:choose>
                                    <c:when test="${row.moderator}">
                                        <span class="community-member-badge is-moderator">
                                            <spring:message code="communities.members.role.moderator"/>
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="community-member-badge is-member">
                                            <spring:message code="communities.members.role.member"/>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                                <c:if test="${row.creator}">
                                    <span class="community-member-badge is-creator">
                                        <spring:message code="communities.members.creatorBadge"/>
                                    </span>
                                </c:if>
                            </div>
                        </div>
                        <div class="community-member-actions">
                            <c:if test="${viewerIsModerator and not row.creator and not row.currentUser}">
                                <pa:action-menu label="${memberActionMenuLabel}" cssClass="community-member-menu">
                                    <c:if test="${not row.moderator}">
                                        <c:url var="promoteUrl" value="/communities/${community.slug}/members/${row.userId}/promote"/>
                                        <form method="post" action="${fn:escapeXml(promoteUrl)}"
                                              data-confirm-modal="promoteMemberConfirmModal">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button type="submit">
                                                <spring:message code="communities.members.action.promote"/>
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${viewerIsCreator and row.moderator}">
                                        <c:url var="transferUrl" value="/communities/${community.slug}/members/${row.userId}/transfer"/>
                                        <form method="post" action="${fn:escapeXml(transferUrl)}"
                                              data-confirm-modal="transferOwnerConfirmModal">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button type="submit">
                                                <spring:message code="communities.members.action.transfer"/>
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:url var="kickUrl" value="/communities/${community.slug}/members/${row.userId}/kick"/>
                                    <form method="post" action="${fn:escapeXml(kickUrl)}"
                                          data-confirm-modal="kickMemberConfirmModal">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <button type="submit" class="action-menu-danger">
                                            <spring:message code="communities.members.action.kick"/>
                                        </button>
                                    </form>
                                </pa:action-menu>
                            </c:if>
                        </div>
                    </li>
                </c:forEach>
            </ul>
        </section>
    </main>

    <c:if test="${viewerIsModerator}">
        <pa:confirmation-modal id="kickMemberConfirmModal"
                               titleCode="communities.members.kick.title"
                               bodyCode="communities.members.kick.body"
                               confirmCode="communities.members.action.kick"
                               confirmCssClass="btn-primary"/>
        <pa:confirmation-modal id="promoteMemberConfirmModal"
                               titleCode="communities.members.promote.title"
                               bodyCode="communities.members.promote.body"
                               confirmCode="communities.members.action.promote"
                               confirmCssClass="btn-primary"/>
        <c:if test="${viewerIsCreator}">
            <pa:confirmation-modal id="transferOwnerConfirmModal"
                                   titleCode="communities.members.transfer.title"
                                   bodyCode="communities.members.transfer.body"
                                   confirmCode="communities.members.action.transfer"
                                   confirmCssClass="btn-primary"/>
        </c:if>
        <pa:script src="/js/shared/action-menu.js"/>
        <pa:script src="/js/shared/confirmation-modal.js"/>
    </c:if>

    <pa:footer/>
</body>
</html>
