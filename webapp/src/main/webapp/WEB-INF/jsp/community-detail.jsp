<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="es">
<spring:message var="pageTitle" code="common.pageTitle.suffix" arguments="${pageTitleValue}"/>
<pa:page-head title="${pageTitle}" styles="/css/cars.css|/css/community-detail.css|/css/communities-responsive.css|/css/profile-modal.css|/css/image-lightbox.css|/css/reposted-review-card.css|/css/review-tags.css"/>
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
                                      enctype="multipart/form-data"
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
                                              enctype="multipart/form-data"
                                              class="community-banner-join-form"
                                              data-confirm-modal="leaveCommunityConfirmModal">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="redirect" value="${fn:escapeXml(communityPostsReturnBaseUrl)}">
                                            <button type="submit"
                                                    class="${communityJoinButtonClass}"
                                                    aria-pressed="true">
                                                <spring:message code="communities.hero.primaryAction.joined"/>
                                            </button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form action="${fn:escapeXml(communityJoinUrl)}" method="post"
                                              enctype="multipart/form-data"
                                              class="community-banner-join-form">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <input type="hidden" name="redirect" value="${fn:escapeXml(communityPostsReturnBaseUrl)}">
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
                    <form method="get" action="${fn:escapeXml(communityDetailUrl)}" class="community-sort-form"
                          enctype="multipart/form-data">
                        <label class="community-sort-label" for="communitySortSelect">
                            <spring:message code="communities.feed.sort"/>
                        </label>
                        <div class="cars-toolbar-field">
                            <span class="cars-toolbar-field-ui" aria-hidden="true">
                                <span class="cars-toolbar-field-copy">
                                    <span class="cars-toolbar-value" data-toolbar-select-value="sort">
                                        <c:choose>
                                            <c:when test="${currentSort eq 'helpful'}"><spring:message code="communities.feed.sort.helpful"/></c:when>
                                            <c:when test="${currentSort eq 'commented'}"><spring:message code="communities.feed.sort.commented"/></c:when>
                                            <c:otherwise><spring:message code="communities.feed.sort.recent"/></c:otherwise>
                                        </c:choose>
                                    </span>
                                </span>
                                <span class="cars-toolbar-chevron">
                                    <pa:icon name="chevron-down" size="12"/>
                                </span>
                            </span>
                            <select id="communitySortSelect" name="sort" class="cars-toolbar-select cars-toolbar-select-overlay" data-auto-submit="true">
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
                        </div>
                        <noscript>
                            <button type="submit" class="btn-secondary community-sort-submit">
                                <spring:message code="common.action.apply"/>
                            </button>
                        </noscript>
                    </form>
                </c:if>

                <c:set var="communityPostsRedirectBase" value="/communities/${communityDetail.community.slug}"/>
                <c:if test="${not empty currentSort}">
                    <c:set var="communityPostsRedirectBase" value="${communityPostsRedirectBase}?sort=${currentSort}"/>
                </c:if>
                <c:if test="${postsCurrentPage > 1 and not empty currentSort}">
                    <c:set var="communityPostsRedirectBase" value="${communityPostsRedirectBase}&page=${postsCurrentPage}"/>
                </c:if>
                <c:if test="${postsCurrentPage > 1 and empty currentSort}">
                    <c:set var="communityPostsRedirectBase" value="${communityPostsRedirectBase}?page=${postsCurrentPage}"/>
                </c:if>

                <div id="communityPostList" class="community-post-list">
                    <c:forEach var="postCard" items="${postCards}">
                        <c:url var="postCardHref" value="/communities/${postCard.communitySlug}/posts/${postCard.postSlug}">
                            <c:param name="redirect" value="${communityPostsReturnUrl}"/>
                        </c:url>
                        <c:url var="postCardHelpfulAction" value="/communities/${postCard.communitySlug}/posts/${postCard.postSlug}/helpful"/>
                        <c:set var="postCardImageUrls" value=""/>
                        <c:forEach var="postCardImageId" items="${postCard.imageIds}">
                            <c:set var="postCardImageUrls" value="${postCardImageUrls}${empty postCardImageUrls ? '' : '|'}/communities/${postCard.communitySlug}/posts/${postCard.postSlug}/images/${postCardImageId}"/>
                        </c:forEach>
                        <pa:community-post-card
                                href="${postCardHref}"
                                author="${postCard.author}"
                                createdAt="${postCard.createdAt}"
                                title="${postCard.title}"
                                body="${postCard.body}"
                                imageUrlsJoined="${postCardImageUrls}"
                                helpfulCount="${postCard.helpfulCount}"
                                commentCount="${postCard.commentCount}"
                                postId="${postCard.postId}"
                                helpfulAction="${postCardHelpfulAction}"
                                helpfulByCurrentUser="${postCard.helpfulByCurrentUser}"
                                helpfulRedirect="${communityPostsRedirectBase}#post-${postCard.postId}"
                                repostReview="${postCard.repostReview}"
                                editable="${postCard.editable}"
                                hideable="${postCard.hideable}"
                                communitySlug="${postCard.communitySlug}"
                                postSlug="${postCard.postSlug}"
                                actionRedirect="${communityPostsRedirectBase}#post-${postCard.postId}"/>
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
    <sec:authorize access="isAuthenticated()">
        <pa:confirmation-modal id="deletePostConfirmModal"
                               titleCode="communities.post.delete.title"
                               bodyCode="communities.post.delete.body"
                               confirmCode="communities.post.deleteAction"
                               confirmCssClass="btn-primary"/>
        <pa:community-hide-modal id="hideCommunityPostModal"
                                 titleCode="communities.post.hide.title"
                                 bodyCode="communities.post.hide.body"
                                 confirmCode="communities.post.hideAction"
                                 placeholderCode="communities.post.hide.reason.placeholder"/>
        <pa:script src="/js/shared/action-menu.js"/>
        <pa:script src="/js/communities/community-moderation.js"/>
    </sec:authorize>
    <pa:image-lightbox/>
    <pa:script src="/js/shared/image-lightbox.js" defer="true"/>
    <pa:script src="/js/cars/cars-toolbar.js"/>
    <pa:script src="/js/communities/community-sort.js" defer="true"/>
    <pa:script src="/js/reviews/review-anchor-highlight.js"/>
    <pa:script src="/js/shared/card-link.js"/>

    <pa:toast messageCode="${actionToastCode}"/>
    <pa:script src="/js/shared/toast.js"/>

    <pa:footer/>
</body>
</html>
