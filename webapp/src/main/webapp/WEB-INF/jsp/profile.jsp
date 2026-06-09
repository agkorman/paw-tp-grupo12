<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="profile.title" styles="/css/reviews.css|/css/profile.css|/css/profile-review-card.css|/css/profile-modal.css|/css/profile-connections.css|/css/review-tags.css|/css/catalog-request-modal.css|/css/moderator-application-modal.css|/css/cars.css|/css/community-detail.css"/>
<body>
    <pa:nav activePage="profile"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
    <c:choose>
        <c:when test="${ownProfile}"><c:set var="profileBasePath" value="/user"/></c:when>
        <c:otherwise><c:set var="profileBasePath" value="/users/${profileUserId}"/></c:otherwise>
    </c:choose>
    <spring:message var="profileStatsAria" code="profile.stats.aria"/>
    <spring:message var="profileContentAria" code="profile.content.aria"/>
    <spring:message var="profileTabsAria" code="profile.tabs.aria"/>
    <spring:message var="followUserAction" code="profile.authRequired.followAction"/>

    <main class="profile-page" data-profile-user-id="${profile.id}">
        <section class="profile-hero" aria-labelledby="profileName">
            <div class="profile-avatar" aria-hidden="true">
                <pa:icon name="user-avatar" size="24"/>
            </div>

            <div class="profile-summary">
                <h1 id="profileName"><c:out value="${profile.name}"/></h1>
                <p class="profile-email"><c:out value="${profile.email}"/></p>

                <dl class="profile-stats" aria-label="${profileStatsAria}">
                    <div>
                        <dt>
                            <span class="profile-stat-value"><c:out value="${profile.activityCount}"/></span>
                        </dt>
                        <dd><spring:message code="profile.stat.activity"/></dd>
                    </div>
                    <div>
                        <dt>
                            <c:url var="openFollowingModalUrl" value="${profileBasePath}">
                                <c:param name="modal" value="following"/>
                                <c:param name="followingPage" value="1"/>
                                <c:if test="${not empty activeTab}">
                                    <c:param name="tab" value="${activeTab}"/>
                                </c:if>
                            </c:url>
                            <a class="profile-stat-button"
                               href="${openFollowingModalUrl}#profileConnectionsModal">
                                <c:out value="${profile.followingCount}"/>
                            </a>
                        </dt>
                        <dd><spring:message code="profile.following"/></dd>
                    </div>
                    <div>
                        <dt>
                            <c:url var="openFollowersModalUrl" value="${profileBasePath}">
                                <c:param name="modal" value="followers"/>
                                <c:param name="followersPage" value="1"/>
                                <c:if test="${not empty activeTab}">
                                    <c:param name="tab" value="${activeTab}"/>
                                </c:if>
                            </c:url>
                            <a class="profile-stat-button"
                               href="${openFollowersModalUrl}#profileConnectionsModal"
                               data-profile-follower-count>
                                <c:out value="${profile.followerCount}"/>
                            </a>
                        </dt>
                        <dd><spring:message code="profile.followers"/></dd>
                    </div>
                </dl>
            </div>

            <c:choose>
                <c:when test="${ownProfile}">
                    <div class="profile-owner-actions">
                        <c:url var="profileLanguageUrl" value="/user/language"/>
                        <spring:message var="profileLanguageLabel" code="profile.language.label"/>
                        <form class="profile-language-form" method="post" action="${profileLanguageUrl}" novalidate="novalidate">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <label for="profileLanguage"><c:out value="${profileLanguageLabel}"/></label>
                            <div class="profile-language-control">
                                <select id="profileLanguage" name="lang" aria-label="${fn:escapeXml(profileLanguageLabel)}">
                                    <option value="es" ${profile.preferredLocale eq 'es' ? 'selected' : ''}>
                                        <spring:message code="profile.language.es"/>
                                    </option>
                                    <option value="en" ${profile.preferredLocale eq 'en' ? 'selected' : ''}>
                                        <spring:message code="profile.language.en"/>
                                    </option>
                                </select>
                                <button type="submit" class="btn-secondary profile-language-button">
                                    <spring:message code="profile.language.save"/>
                                </button>
                            </div>
                            <c:if test="${not empty profileLanguageErrorCode}">
                                <p class="profile-language-error"><spring:message code="${profileLanguageErrorCode}"/></p>
                            </c:if>
                        </form>
                        <button type="button" class="btn-primary profile-action-button" data-open-edit-profile-modal><spring:message code="profile.settings"/></button>
                        <c:url var="logoutUrl" value="/logout"/>
                        <form class="profile-logout-form" method="post" action="${logoutUrl}" data-confirm-modal="logoutConfirmModal">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit" class="btn-secondary profile-logout-button"><spring:message code="profile.edit.logout"/></button>
                        </form>
                        <c:if test="${canRequestModerator}">
                            <button type="button" class="profile-moderator-link" data-open-request-admin-modal>
                                <spring:message code="profile.requestAdmin"/>
                            </button>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <c:url var="profileFollowUrl" value="/users/${profile.id}/follow"/>
                    <c:choose>
                        <c:when test="${authenticated}">
                            <form class="profile-action-form profile-follow-form"
                                  method="post"
                                  action="${profileFollowUrl}"
                                  data-auth-resume-intent="follow-profile-${profile.id}">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button
                                        type="submit"
                                        class="btn-primary profile-action-button profile-follow-button ${followingProfile ? 'is-following' : ''}"
                                        aria-pressed="${followingProfile}">
                                    <c:choose>
                                        <c:when test="${followingProfile}"><spring:message code="common.label.following"/></c:when>
                                        <c:otherwise><spring:message code="common.label.follow"/></c:otherwise>
                                    </c:choose>
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <c:url var="profileFollowLoginUrl" value="/login">
                                <c:param name="redirect" value="/users/${profile.id}"/>
                                <c:param name="intent" value="follow-profile-${profile.id}"/>
                            </c:url>
                            <a href="${profileFollowLoginUrl}"
                               class="btn-primary profile-action-button profile-follow-button"
                               data-auth-resume-intent="follow-profile-${profile.id}"
                               data-auth-required="true"
                               data-auth-required-action="${followUserAction}"
                               data-auth-required-intent="follow-profile-${profile.id}">
                                <spring:message code="common.label.follow"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="profile-tabs" aria-label="${profileContentAria}">
            <c:url var="profileActivityTabUrl" value="${profileBasePath}">
                <c:param name="tab" value="activity"/>
            </c:url>
            <c:url var="profileFavoritesTabUrl" value="${profileBasePath}">
                <c:param name="tab" value="favorites"/>
            </c:url>
            <c:url var="profileLikesTabUrl" value="${profileBasePath}">
                <c:param name="tab" value="likes"/>
            </c:url>

            <spring:message var="profileActivityLabel" code="profile.tab.activity"/>
            <spring:message var="profileActivityHeadingLabel" code="profile.tab.activity.heading"/>
            <spring:message var="profileFavoritesLabel" code="profile.tab.favorites"/>
            <spring:message var="profileLikesLabel" code="profile.tab.likes"/>
            <c:if test="${ownProfile}">
                <pa:subtabs tabCount="3"
                            labels="${profileActivityLabel}|${profileFavoritesLabel}|${profileLikesLabel}"
                            hrefs="${profileActivityTabUrl}|${profileFavoritesTabUrl}|${profileLikesTabUrl}"
                            counts="${activityCount}|${favoriteCarCount}|${likedCount}"
                            values="activity|favorites|likes"
                            activeValue="${activeTab}"
                            ariaLabel="${profileTabsAria}"/>
            </c:if>
            <c:if test="${not ownProfile}">
                <header class="profile-section-heading">
                    <h2 id="profileActivityTitle"><c:out value="${profileActivityHeadingLabel}"/></h2>
                    <span><c:out value="${activityCount}"/></span>
                </header>
            </c:if>

            <c:choose>
                <c:when test="${activeTab eq 'favorites'}">
                    <section id="profileFavoritesPanel"
                             class="profile-tab-panel profile-favorites-section"
                             aria-labelledby="profileFavoritesTab">

                        <c:choose>
                            <c:when test="${empty favoriteCars}">
                                <div class="profile-empty-state">
                                    <p><spring:message code="profile.empty.favorites"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="profile-favorites-grid">
                                    <c:forEach var="favoriteCar" items="${favoriteCars}">
                                        <c:url var="favoriteReviewUrl" value="/reviews/car/${favoriteCar.id}"/>
                                        <pa:car-card
                                                model="${favoriteCar.brandName} ${favoriteCar.model}"
                                                bodyType="${favoriteCar.bodyType}"
                                                carId="${favoriteCar.id}"
                                                hasImage="${favoriteCar.hasImage}"
                                                href="${favoriteReviewUrl}"
                                                averageRating="${reviewStatsByCarId[favoriteCar.id].averageRating}"
                                                reviewCount="${reviewStatsByCarId[favoriteCar.id].reviewCount}"/>
                                    </c:forEach>
                                </div>
                                <c:if test="${favoriteCarsTotalPages > 1}">
                                    <jsp:useBean id="favoriteCarsPaginationParams" class="java.util.LinkedHashMap"/>
                                    <c:set target="${favoriteCarsPaginationParams}" property="tab" value="favorites"/>
                                    <spring:message var="favoriteCarsPaginationAria" code="profile.favorites.pagination.aria"/>
                                    <pa:pagination currentPage="${favoriteCarsCurrentPage}"
                                                   totalPages="${favoriteCarsTotalPages}"
                                                   baseUrl="${profileBasePath}"
                                                   extraParams="${favoriteCarsPaginationParams}"
                                                   fragment="profileFavoritesPanel"
                                                   ariaLabel="${favoriteCarsPaginationAria}"/>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:when>
                <c:when test="${activeTab eq 'likes'}">
                    <section id="profileLikesPanel"
                             class="profile-tab-panel profile-liked-section"
                             aria-labelledby="profileLikesTab">

                        <c:choose>
                            <c:when test="${empty likesEntries}">
                                <div class="profile-empty-state">
                                    <p><spring:message code="profile.empty.likes"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:set var="likesActionRedirectBase" value="${profileBasePath}?tab=likes"/>
                                <c:if test="${not empty likesCurrentPage and likesCurrentPage > 1}">
                                    <c:set var="likesActionRedirectBase" value="${likesActionRedirectBase}&page=${likesCurrentPage}"/>
                                </c:if>
                                <div class="profile-activity-list">
                                    <c:forEach var="entry" items="${likesEntries}">
                                        <c:choose>
                                            <c:when test="${entry.isReview}">
                                                <pa:profile-review-card
                                                        reviewCard="${entry.reviewCard}"
                                                        editable="${entry.reviewCard.ownedByCurrentUser}"
                                                        hideable="${entry.reviewCard.hideable}"
                                                        actionRedirect="${likesActionRedirectBase}#review-${entry.reviewCard.review.id}"/>
                                            </c:when>
                                            <c:when test="${entry.isPost}">
                                                <c:url var="likedPostHref" value="/communities/${entry.postCard.communitySlug}/posts/${entry.postCard.postSlug}">
                                                    <c:param name="redirect" value="${likesActionRedirectBase}#post-${entry.postCard.postId}"/>
                                                </c:url>
                                                <c:url var="likedPostHelpfulAction" value="/communities/${entry.postCard.communitySlug}/posts/${entry.postCard.postSlug}/helpful"/>
                                                <pa:community-post-card
                                                        author="${entry.postCard.authorName}"
                                                        createdAt="${entry.postCard.createdAt}"
                                                        title="${entry.postCard.title}"
                                                        body="${entry.postCard.body}"
                                                        helpfulCount="${entry.postCard.helpfulCount}"
                                                        commentCount="${entry.postCard.commentCount}"
                                                        communityName="${entry.postCard.communityName}"
                                                        href="${likedPostHref}"
                                                        postId="${entry.postCard.postId}"
                                                        helpfulAction="${likedPostHelpfulAction}"
                                                        helpfulByCurrentUser="${entry.postCard.helpfulByCurrentUser}"
                                                        helpfulRedirect="${likesActionRedirectBase}#post-${entry.postCard.postId}"
                                                        editable="${entry.postCard.ownedByCurrentUser}"
                                                        hideable="${entry.postCard.hideable}"
                                                        communitySlug="${entry.postCard.communitySlug}"
                                                        postSlug="${entry.postCard.postSlug}"
                                                        actionRedirect="${likesActionRedirectBase}#post-${entry.postCard.postId}"/>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                                <c:if test="${likesTotalPages > 1}">
                                    <jsp:useBean id="likesPaginationParams" class="java.util.LinkedHashMap"/>
                                    <c:set target="${likesPaginationParams}" property="tab" value="likes"/>
                                    <spring:message var="likesPaginationAria" code="profile.likes.pagination.aria"/>
                                    <pa:pagination currentPage="${likesCurrentPage}"
                                                   totalPages="${likesTotalPages}"
                                                   baseUrl="${profileBasePath}"
                                                   extraParams="${likesPaginationParams}"
                                                   fragment="profileLikesPanel"
                                                   ariaLabel="${likesPaginationAria}"/>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:when>
                <c:otherwise>
                    <section id="profileActivityPanel"
                             class="profile-tab-panel profile-reviews-section"
                             aria-labelledby="${ownProfile ? 'profileActivityTab' : 'profileActivityTitle'}">

                        <c:choose>
                            <c:when test="${empty activityEntries}">
                                <div class="profile-empty-state">
                                    <p><spring:message code="profile.empty.activity"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:set var="activityActionRedirectBase" value="${profileBasePath}?tab=activity"/>
                                <c:if test="${not empty activityCurrentPage and activityCurrentPage > 1}">
                                    <c:set var="activityActionRedirectBase" value="${activityActionRedirectBase}&page=${activityCurrentPage}"/>
                                </c:if>
                                <div class="profile-activity-list">
                                    <c:forEach var="entry" items="${activityEntries}">
                                        <c:choose>
                                            <c:when test="${entry.isReview}">
                                                <pa:profile-review-card
                                                        reviewCard="${entry.reviewCard}"
                                                        editable="${entry.reviewCard.ownedByCurrentUser}"
                                                        hideable="${entry.reviewCard.hideable}"
                                                        actionRedirect="${activityActionRedirectBase}#review-${entry.reviewCard.review.id}"/>
                                            </c:when>
                                            <c:when test="${entry.isPost}">
                                                <c:url var="activityPostHref" value="/communities/${entry.postCard.communitySlug}/posts/${entry.postCard.postSlug}">
                                                    <c:param name="redirect" value="${activityActionRedirectBase}#post-${entry.postCard.postId}"/>
                                                </c:url>
                                                <c:url var="activityPostHelpfulAction" value="/communities/${entry.postCard.communitySlug}/posts/${entry.postCard.postSlug}/helpful"/>
                                                <pa:community-post-card
                                                        author="${entry.postCard.authorName}"
                                                        createdAt="${entry.postCard.createdAt}"
                                                        title="${entry.postCard.title}"
                                                        body="${entry.postCard.body}"
                                                        helpfulCount="${entry.postCard.helpfulCount}"
                                                        commentCount="${entry.postCard.commentCount}"
                                                        communityName="${entry.postCard.communityName}"
                                                        href="${activityPostHref}"
                                                        postId="${entry.postCard.postId}"
                                                        helpfulAction="${activityPostHelpfulAction}"
                                                        helpfulByCurrentUser="${entry.postCard.helpfulByCurrentUser}"
                                                        helpfulRedirect="${activityActionRedirectBase}#post-${entry.postCard.postId}"
                                                        editable="${entry.postCard.ownedByCurrentUser}"
                                                        hideable="${entry.postCard.hideable}"
                                                        communitySlug="${entry.postCard.communitySlug}"
                                                        postSlug="${entry.postCard.postSlug}"
                                                        actionRedirect="${activityActionRedirectBase}#post-${entry.postCard.postId}"/>
                                            </c:when>
                                        </c:choose>
                                    </c:forEach>
                                </div>
                                <c:if test="${activityTotalPages > 1}">
                                    <jsp:useBean id="activityPaginationParams" class="java.util.LinkedHashMap"/>
                                    <c:set target="${activityPaginationParams}" property="tab" value="activity"/>
                                    <spring:message var="activityPaginationAria" code="profile.activity.pagination.aria"/>
                                    <pa:pagination currentPage="${activityCurrentPage}"
                                                   totalPages="${activityTotalPages}"
                                                   baseUrl="${profileBasePath}"
                                                   extraParams="${activityPaginationParams}"
                                                   fragment="profileActivityPanel"
                                                   ariaLabel="${activityPaginationAria}"/>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <pa:confirmation-modal id="deleteReviewConfirmModal"
                           titleCode="review.delete.title"
                           bodyCode="review.delete.body"
                           confirmCode="common.action.delete"
                           confirmCssClass="btn-primary"/>
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
    <pa:review-hide-modal/>
    <pa:edit-profile-modal profile="${profile}"/>
    <pa:profile-connections-modal followingUsers="${followingUsers}"
                                  followerUsers="${followerUsers}"
                                  activeKind="${connectionsModal}"
                                  pagination="${connectionsPagination}"
                                  profileBasePath="${profileBasePath}"
                                  activeTab="${activeTab}"/>
    <pa:confirmation-modal id="logoutConfirmModal"
                           titleCode="profile.logout.confirm.title"
                           bodyCode="profile.logout.confirm.body"
                           confirmCode="profile.edit.logout"
                           confirmCssClass="btn-primary"/>
    <pa:auth-required-modal/>
    <c:if test="${ownProfile}">
        <pa:moderator-application-modal/>
    </c:if>
    <c:set var="profileToastCode" value="${not empty actionToastCode ? actionToastCode : (not empty profileLanguageSuccessCode ? profileLanguageSuccessCode : submittedToastMessageCode)}"/>
    <pa:toast messageCode="${profileToastCode}"/>
    <pa:script src="/js/shared/action-menu.js"/>
    <pa:script src="/js/shared/modal-utils.js"/>
    <pa:script src="/js/auth/auth-required-modal.js"/>
    <pa:script src="/js/shared/confirmation-modal.js"/>
    <pa:script src="/js/profile/profile.js"/>
    <pa:script src="/js/communities/community-moderation.js"/>
    <pa:script src="/js/reviews/review-moderation.js"/>
    <pa:script src="/js/reviews/review-anchor-highlight.js"/>
    <c:if test="${ownProfile}">
        <pa:script src="/js/profile/moderator-application-modal.js"/>
    </c:if>
    <pa:script src="/js/shared/toast.js"/>
    <pa:footer/>
</body>
</html>
