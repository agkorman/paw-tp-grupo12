<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${pageTitle}" styles="/css/community-detail.css|/css/communities-responsive.css|/css/profile-modal.css|/css/image-lightbox.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
    <c:url var="communitiesLoginUrl" value="/login">
        <c:param name="redirect" value="/communities/${communityDetail.community.slug}"/>
        <c:param name="intent" value="communities-join"/>
    </c:url>
    <c:url var="communitySubmitUrl" value="/communities/${communityDetail.community.slug}/submit"/>
    <c:url var="communityJoinUrl" value="/communities/${communityDetail.community.slug}/join"/>
    <c:url var="communityMembersUrl" value="/communities/${communityDetail.community.slug}/members"/>
    <c:url var="communityDetailUrl" value="/communities/${communityDetail.community.slug}"/>
    <c:url var="communityPostsReturnBaseUrl" value="/communities/${communityDetail.community.slug}">
        <c:if test="${not empty currentSort}">
            <c:param name="sort" value="${currentSort}"/>
        </c:if>
        <c:param name="page" value="${postsCurrentPage}"/>
    </c:url>
    <c:set var="communityPostsReturnUrl" value="${communityPostsReturnBaseUrl}#communityFeedTitle"/>
    <c:set var="viewerIsMember" value="${communityDetail.viewerMember}"/>
    <c:set var="viewerIsModerator" value="${communityDetail.viewerModerator}"/>
    <c:set var="viewerIsCreator" value="${communityDetail.viewerCreator}"/>
    <spring:message var="communityStatsAria" code="communities.sidebar.stats.aria"/>
    <c:set var="communityJoinButtonClass" value="btn-primary community-banner-join-btn"/>
    <c:if test="${communityDetail.joined}">
        <c:set var="communityJoinButtonClass" value="btn-primary community-banner-join-btn is-joined"/>
    </c:if>

    <main class="communities-page">
        <section class="community-hero">
            <div class="community-banner">
                <div class="community-banner-copy">
                    <p class="community-banner-kicker"><spring:message code="communities.hero.kicker"/></p>
                    <div class="community-banner-heading">
                        <h1 class="community-banner-title"><c:out value="${communityDetail.community.name}"/></h1>
                        <div class="community-banner-actions">
                            <c:if test="${viewerIsModerator}">
                                <c:url var="communityEditUrl" value="/communities/${communityDetail.community.slug}/edit"/>
                                <a class="btn-secondary community-banner-edit-btn" href="${fn:escapeXml(communityEditUrl)}">
                                    <spring:message code="communities.detail.edit"/>
                                </a>
                            </c:if>
                            <c:if test="${viewerIsCreator}">
                                <c:url var="communityDeleteUrl" value="/communities/${communityDetail.community.slug}/delete"/>
                                <form action="${fn:escapeXml(communityDeleteUrl)}" method="post"
                                      class="community-banner-join-form"
                                      data-confirm-modal="leaveCreatorConfirmModal">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button type="submit" class="btn-secondary community-banner-leave-btn">
                                        <spring:message code="communities.hero.leaveCreator"/>
                                    </button>
                                </form>
                            </c:if>
                            <c:if test="${authenticated and not viewerIsCreator}">
                                <c:choose>
                                    <c:when test="${communityDetail.joined}">
                                        <form action="${fn:escapeXml(communityJoinUrl)}" method="post"
                                              class="community-banner-join-form"
                                              data-confirm-modal="leaveCommunityConfirmModal">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button type="submit"
                                                    class="${communityJoinButtonClass}"
                                                    aria-pressed="true">
                                                <spring:message code="communities.hero.primaryAction.joined"/>
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${fn:escapeXml(communityJoinUrl)}" method="post"
                                              class="community-banner-join-form">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button type="submit"
                                                    class="${communityJoinButtonClass}"
                                                    aria-pressed="false">
                                                <spring:message code="communities.hero.primaryAction.authenticated"/>
                                            </button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                            <c:if test="${not authenticated}">
                                <a class="btn-primary community-banner-join-btn" href="${fn:escapeXml(communitiesLoginUrl)}">
                                    <spring:message code="communities.hero.primaryAction.anonymous"/>
                                </a>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <div class="community-layout">
            <section class="community-feed-panel" aria-labelledby="communityFeedTitle">
                <div class="community-feed-header">
                    <div class="community-feed-heading">
                        <p class="community-section-kicker"><spring:message code="communities.feed.kicker"/></p>
                        <h2 id="communityFeedTitle" class="community-section-title"><spring:message code="communities.feed.title"/></h2>
                    </div>
                    <div class="community-feed-header-actions">
                        <c:if test="${authenticated and viewerIsMember}">
                            <a class="btn-secondary community-members-btn" href="${fn:escapeXml(communityMembersUrl)}">
                                <spring:message code="communities.detail.viewMembers"/>
                            </a>
                        </c:if>
                        <c:choose>
                            <c:when test="${authenticated and viewerIsMember}">
                                <a class="btn-primary community-create-post-btn" href="${fn:escapeXml(communitySubmitUrl)}">
                                    <spring:message code="communities.detail.createPost"/>
                                </a>
                            </c:when>
                            <c:when test="${authenticated}">
                                <span class="community-create-post-hint">
                                    <spring:message code="communities.detail.joinToPost"/>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <a class="btn-primary community-create-post-btn" href="${fn:escapeXml(communitiesLoginUrl)}">
                                    <spring:message code="communities.hero.primaryAction.anonymous"/>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <c:if test="${not empty postCards}">
                    <form method="get" action="${fn:escapeXml(communityDetailUrl)}" class="community-sort-form">
                        <label class="community-sort-label" for="communitySortSelect">
                            <spring:message code="communities.feed.sort"/>
                        </label>
                        <select id="communitySortSelect" name="sort" class="community-sort-select" data-auto-submit="true">
                            <option value="recent" ${currentSort eq 'recent' ? 'selected' : ''}>
                                <spring:message code="communities.feed.sort.recent"/>
                            </option>
                            <option value="helpful" ${currentSort eq 'helpful' ? 'selected' : ''}>
                                <spring:message code="communities.feed.sort.helpful"/>
                            </option>
                            <option value="commented" ${currentSort eq 'commented' ? 'selected' : ''}>
                                <spring:message code="communities.feed.sort.commented"/>
                            </option>
                        </select>
                        <noscript>
                            <button type="submit" class="btn-secondary community-sort-submit">
                                <spring:message code="common.action.apply"/>
                            </button>
                        </noscript>
                    </form>
                </c:if>

                <div class="community-post-list">
                    <c:forEach var="postCard" items="${postCards}">
                        <c:url var="postCardHref" value="${postCard.href}">
                            <c:param name="redirect" value="${communityPostsReturnUrl}"/>
                        </c:url>
                        <pa:community-post-card
                                href="${postCardHref}"
                                author="${postCard.author}"
                                timeText="${postCard.timeText}"
                                title="${postCard.title}"
                                body="${postCard.body}"
                                imageUrls="${postCard.imageUrls}"
                                helpfulCount="${postCard.helpfulCount}"
                                commentCount="${postCard.commentCount}"/>
                    </c:forEach>
                </div>

                <c:if test="${postsTotalPages > 1}">
                    <jsp:useBean id="communityPostsPaginationParams" class="java.util.LinkedHashMap"/>
                    <c:if test="${not empty currentSort}">
                        <c:set target="${communityPostsPaginationParams}" property="sort" value="${currentSort}"/>
                    </c:if>
                    <spring:message var="communityPostsPaginationAria" code="communities.posts.pagination.aria"/>
                    <pa:pagination currentPage="${postsCurrentPage}"
                                   totalPages="${postsTotalPages}"
                                   baseUrl="${communityDetailUrl}"
                                   extraParams="${communityPostsPaginationParams}"
                                   fragment="communityFeedTitle"
                                   ariaLabel="${communityPostsPaginationAria}"/>
                </c:if>
            </section>

            <aside class="community-sidebar">
                <section class="community-sidebar-card">
                    <p class="community-sidebar-eyebrow"><spring:message code="communities.sidebar.about.eyebrow"/></p>
                    <p class="community-sidebar-copy"><c:out value="${communityDetail.community.description}"/></p>
                    <c:if test="${not empty communityDetail.topics}">
                        <div class="community-sidebar-topics">
                            <c:forEach var="topic" items="${communityDetail.topics}">
                                <span class="community-sidebar-topic">#<c:out value="${topic.code}"/></span>
                            </c:forEach>
                        </div>
                    </c:if>
                </section>

                <section class="community-sidebar-card community-sidebar-stats" aria-label="${fn:escapeXml(communityStatsAria)}">
                    <div class="community-stat">
                        <strong><c:out value="${communityDetail.memberCount}"/></strong>
                        <span><spring:message code="communities.sidebar.stats.members"/></span>
                    </div>
                    <div class="community-stat">
                        <strong><c:out value="${communityDetail.weeklyPostCount}"/></strong>
                        <span><spring:message code="communities.sidebar.stats.posts"/></span>
                    </div>
                </section>
            </aside>
        </div>
    </main>

    <c:if test="${authenticated and communityDetail.joined and not viewerIsCreator}">
        <pa:confirmation-modal id="leaveCommunityConfirmModal"
                               titleCode="communities.leave.confirm.title"
                               bodyCode="communities.leave.confirm.body"
                               confirmCode="communities.leave.confirm.action"
                               confirmCssClass="btn-primary"/>
        <pa:script src="/js/shared/confirmation-modal.js"/>
    </c:if>
    <c:if test="${viewerIsCreator}">
        <pa:confirmation-modal id="leaveCreatorConfirmModal"
                               titleCode="communities.leave.creator.title"
                               bodyCode="communities.leave.creator.body"
                               confirmCode="communities.leave.creator.action"
                               confirmCssClass="btn-primary"/>
        <pa:script src="/js/shared/confirmation-modal.js"/>
    </c:if>
    <pa:image-lightbox/>
    <pa:script src="/js/shared/image-lightbox.js" defer="true"/>
    <pa:script src="/js/communities/community-sort.js" defer="true"/>

    <pa:footer/>
</body>
</html>
