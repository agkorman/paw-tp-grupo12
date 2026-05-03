<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="profile.title"/></title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=6'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css?v=4'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile.css?v=10'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-review-card.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-modals.css?v=5'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-connections.css?v=1'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-tags.css'/>">
</head>
<body>
    <pa:nav activePage="profile"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
    <spring:message var="profileStatsAria" code="profile.stats.aria"/>
    <spring:message var="profileContentAria" code="profile.content.aria"/>
    <spring:message var="profileTabsAria" code="profile.tabs.aria"/>
    <spring:message var="followUserAction" code="profile.authRequired.followAction"/>
    <spring:message var="followingConnectionsTitle" code="profile.following"/>
    <spring:message var="followersConnectionsTitle" code="profile.followers"/>
    <spring:message var="followLabel" code="common.label.follow"/>
    <spring:message var="followingLabel" code="common.label.following"/>

    <main class="profile-page" data-profile-user-id="${profile.id}">
        <section class="profile-hero" aria-labelledby="profileName">
            <div class="profile-avatar" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.65" aria-hidden="true" focusable="false">
                    <circle cx="12" cy="8" r="4"></circle>
                    <path d="M4 21c1.6-4 4.2-6 8-6s6.4 2 8 6"></path>
                </svg>
            </div>

            <div class="profile-summary">
                <h1 id="profileName"><c:out value="${profile.name}"/></h1>
                <p class="profile-email"><c:out value="${profile.email}"/></p>

                <dl class="profile-stats" aria-label="${profileStatsAria}">
                    <div>
                        <dt>
                            <span class="profile-stat-value"><c:out value="${profile.reviewCount}"/></span>
                        </dt>
                        <dd><spring:message code="common.label.reviews"/></dd>
                    </div>
                    <div>
                        <dt>
                            <button
                                    type="button"
                                    class="profile-stat-button"
                                    data-open-connections-modal
                                    data-connections-kind="following"
                                    data-connections-title="${followingConnectionsTitle}">
                                <c:out value="${profile.followingCount}"/>
                            </button>
                        </dt>
                        <dd><spring:message code="profile.following"/></dd>
                    </div>
                    <div>
                        <dt>
                            <button
                                    type="button"
                                    class="profile-stat-button"
                                    data-open-connections-modal
                                    data-connections-kind="followers"
                                    data-connections-title="${followersConnectionsTitle}"
                                    data-profile-follower-count>
                                <c:out value="${profile.followerCount}"/>
                            </button>
                        </dt>
                        <dd><spring:message code="profile.followers"/></dd>
                    </div>
                </dl>
            </div>

            <c:choose>
                <c:when test="${ownProfile}">
                    <div class="profile-owner-actions">
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
                    <c:url var="profileFollowUrl" value="/profiles/${profile.id}/follow"/>
                    <c:choose>
                        <c:when test="${authenticated}">
                            <form class="profile-action-form profile-follow-form"
                                  method="post"
                                  action="${profileFollowUrl}"
                                  data-enhanced-follow="true"
                                  data-follow-user-id="${profile.id}"
                                  data-auth-resume-intent="follow-profile-${profile.id}">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button
                                        type="submit"
                                        class="btn-primary profile-action-button profile-follow-button ${followingProfile ? 'is-following' : ''}"
                                        aria-pressed="${followingProfile}"
                                        data-follow-toggle
                                        data-follow-user-id="${profile.id}"
                                        data-follow-label="${fn:escapeXml(followLabel)}"
                                        data-following-label="${fn:escapeXml(followingLabel)}">
                                    <c:choose>
                                        <c:when test="${followingProfile}"><spring:message code="common.label.following"/></c:when>
                                        <c:otherwise><spring:message code="common.label.follow"/></c:otherwise>
                                    </c:choose>
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <c:url var="profileFollowLoginUrl" value="/login">
                                <c:param name="redirect" value="/profiles/${profile.id}"/>
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
            <c:url var="profileBaseUrl" value="${profileBasePath}"/>
            <c:url var="profileReviewsTabUrl" value="${profileBasePath}">
                <c:param name="tab" value="reviews"/>
            </c:url>
            <c:url var="profileFavoritesTabUrl" value="${profileBasePath}">
                <c:param name="tab" value="favorites"/>
            </c:url>
            <c:url var="profileLikedTabUrl" value="${profileBasePath}">
                <c:param name="tab" value="liked"/>
            </c:url>

            <spring:message var="profileMyReviewsLabel" code="profile.tab.myReviews"/>
            <spring:message var="profileReviewsLabel" code="common.label.reviews"/>
            <spring:message var="profileFavoritesLabel" code="profile.tab.favorites"/>
            <spring:message var="profileLikedLabel" code="profile.tab.liked"/>
            <c:if test="${ownProfile}">
                <pa:subtabs tabCount="3"
                            labels="${profileMyReviewsLabel}|${profileFavoritesLabel}|${profileLikedLabel}"
                            hrefs="${profileReviewsTabUrl}|${profileFavoritesTabUrl}|${profileLikedTabUrl}"
                            counts="${profileReviewCount}|${favoriteCarCount}|${likedActivityCount}"
                            values="reviews|favorites|liked"
                            activeValue="${activeTab}"
                            ariaLabel="${profileTabsAria}"/>
            </c:if>
            <c:if test="${not ownProfile}">
                <header class="profile-section-heading">
                    <h2 id="profileReviewsTitle"><c:out value="${profileReviewsLabel}"/></h2>
                    <span><c:out value="${profileReviewCount}"/></span>
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
                                        <c:url var="favoriteReviewUrl" value="/reviews">
                                            <c:param name="carId" value="${favoriteCar.id}"/>
                                        </c:url>
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
                                <c:if test="${favoriteCarsCurrentPage < favoriteCarsTotalPages}">
                                    <c:url var="favoriteCarsShowMoreUrl" value="${profileBasePath}">
                                        <c:param name="tab" value="favorites"/>
                                        <c:param name="page" value="${favoriteCarsCurrentPage + 1}"/>
                                    </c:url>
                                    <div class="reviews-feed-more profile-show-more">
                                        <a class="btn-secondary reviews-show-more"
                                           href="${favoriteCarsShowMoreUrl}"
                                           data-review-show-more="true"
                                           data-fragment-url="${profileBaseUrl}"
                                           data-target="#profileFavoritesPanel"
                                           data-list-selector=".profile-favorites-grid"
                                           data-item-selector=".profile-favorites-grid > .car-card-shell">
                                            <spring:message code="common.action.showMoreCars"/>
                                        </a>
                                    </div>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:when>
                <c:when test="${activeTab eq 'liked'}">
                    <section id="profileLikedPanel"
                             class="profile-tab-panel profile-liked-section"
                             aria-labelledby="profileLikedTab">

                        <c:choose>
                            <c:when test="${empty likedReviews}">
                                <div class="profile-empty-state">
                                    <p><spring:message code="profile.empty.liked"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="profile-review-list">
                                    <c:forEach var="likedReview" items="${likedReviews}">
                                        <pa:profile-review-card
                                                reviewCard="${likedReview}"
                                                editable="${likedReview.ownedByCurrentUser}"/>
                                    </c:forEach>
                                </div>
                                <c:if test="${likedReviewsCurrentPage < likedReviewsTotalPages}">
                                    <c:url var="likedReviewsShowMoreUrl" value="${profileBasePath}">
                                        <c:param name="tab" value="liked"/>
                                        <c:param name="page" value="${likedReviewsCurrentPage + 1}"/>
                                    </c:url>
                                    <div class="reviews-feed-more profile-show-more">
                                        <a class="btn-secondary reviews-show-more"
                                           href="${likedReviewsShowMoreUrl}"
                                           data-review-show-more="true"
                                           data-fragment-url="${profileBaseUrl}"
                                           data-target="#profileLikedPanel"
                                           data-list-selector=".profile-review-list"
                                           data-item-selector=".profile-review-list > .profile-review-card">
                                            <spring:message code="common.action.showMoreReviews"/>
                                        </a>
                                    </div>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:when>
                <c:otherwise>
                    <section id="profileReviewsPanel"
                             class="profile-tab-panel profile-reviews-section"
                             aria-labelledby="${ownProfile ? 'profileReviewsTab' : 'profileReviewsTitle'}">

                        <c:choose>
                            <c:when test="${empty profileReviews}">
                                <div class="profile-empty-state">
                                    <p><spring:message code="profile.empty.reviews"/></p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="profile-review-list">
                                    <c:forEach var="profileReview" items="${profileReviews}">
                                        <pa:profile-review-card
                                                reviewCard="${profileReview}"
                                                editable="${profileReview.ownedByCurrentUser}"/>
                                    </c:forEach>
                                </div>
                                <c:if test="${profileReviewsCurrentPage < profileReviewsTotalPages}">
                                    <c:url var="profileReviewsShowMoreUrl" value="${profileBasePath}">
                                        <c:param name="tab" value="reviews"/>
                                        <c:param name="page" value="${profileReviewsCurrentPage + 1}"/>
                                    </c:url>
                                    <div class="reviews-feed-more profile-show-more">
                                        <a class="btn-secondary reviews-show-more"
                                           href="${profileReviewsShowMoreUrl}"
                                           data-review-show-more="true"
                                           data-fragment-url="${profileBaseUrl}"
                                           data-target="#profileReviewsPanel"
                                           data-list-selector=".profile-review-list"
                                           data-item-selector=".profile-review-list > .profile-review-card">
                                            <spring:message code="common.action.showMoreReviews"/>
                                        </a>
                                    </div>
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
    <pa:edit-profile-modal profile="${profile}"/>
    <pa:profile-connections-modal followingUsers="${followingUsers}" followerUsers="${followerUsers}"/>
    <pa:confirmation-modal id="logoutConfirmModal"
                           titleCode="profile.logout.confirm.title"
                           bodyCode="profile.logout.confirm.body"
                           confirmCode="profile.edit.logout"
                           confirmCssClass="btn-primary"/>
    <pa:auth-required-modal/>
    <c:if test="${ownProfile}">
        <pa:request-admin-modal/>
    </c:if>
    <pa:toast messageCode="${submittedToastMessageCode}"/>
    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/enhanced-filters.js?v=6'/>"></script>
    <script src="<c:url value='/js/action-menu.js'/>"></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <script src="<c:url value='/js/confirmation-modal.js?v=1'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
    <script src="<c:url value='/js/profile.js?v=9'/>"></script>
    <c:if test="${ownProfile}">
        <script src="<c:url value='/js/admin-request-modal.js'/>"></script>
    </c:if>
    <script src="<c:url value='/js/toast.js'/>"></script>
    <script src="<c:url value='/js/review-delete.js?v=2'/>"></script>
    <pa:footer/>
</body>
</html>
