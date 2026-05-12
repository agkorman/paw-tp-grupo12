<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="profile.title" styles="/css/reviews.css|/css/profile.css|/css/profile-review-card.css|/css/profile-modal.css|/css/profile-connections.css|/css/review-tags.css|/css/catalog-request-modal.css|/css/moderator-application-modal.css|/css/cars.css"/>
<body>
    <pa:nav activePage="profile"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
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
                            <span class="profile-stat-value"><c:out value="${profile.reviewCount}"/></span>
                        </dt>
                        <dd><spring:message code="common.label.reviews"/></dd>
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
                        <c:url var="profileLanguageUrl" value="/profile/language"/>
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
                    <c:url var="profileFollowUrl" value="/profiles/${profile.id}/follow"/>
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
                                <c:url var="likedDeleteRedirect" value="${profileBasePath}">
                                    <c:param name="tab" value="liked"/>
                                    <c:if test="${not empty likedReviewsCurrentPage and likedReviewsCurrentPage > 1}">
                                        <c:param name="page" value="${likedReviewsCurrentPage}"/>
                                    </c:if>
                                </c:url>
                                <c:set var="likedDeleteRedirect" value="${likedDeleteRedirect}#profileLikedPanel"/>
                                <div class="profile-review-list">
                                    <c:forEach var="likedReview" items="${likedReviews}">
                                        <pa:profile-review-card
                                                reviewCard="${likedReview}"
                                                editable="${likedReview.ownedByCurrentUser}"
                                                deleteRedirect="${likedDeleteRedirect}"/>
                                    </c:forEach>
                                </div>
                                <c:if test="${likedReviewsTotalPages > 1}">
                                    <jsp:useBean id="likedReviewsPaginationParams" class="java.util.LinkedHashMap"/>
                                    <c:set target="${likedReviewsPaginationParams}" property="tab" value="liked"/>
                                    <spring:message var="likedReviewsPaginationAria" code="profile.liked.pagination.aria"/>
                                    <pa:pagination currentPage="${likedReviewsCurrentPage}"
                                                   totalPages="${likedReviewsTotalPages}"
                                                   baseUrl="${profileBasePath}"
                                                   extraParams="${likedReviewsPaginationParams}"
                                                   fragment="profileLikedPanel"
                                                   ariaLabel="${likedReviewsPaginationAria}"/>
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
                                <c:url var="profileReviewsDeleteRedirect" value="${profileBasePath}">
                                    <c:param name="tab" value="reviews"/>
                                    <c:if test="${not empty profileReviewsCurrentPage and profileReviewsCurrentPage > 1}">
                                        <c:param name="page" value="${profileReviewsCurrentPage}"/>
                                    </c:if>
                                </c:url>
                                <c:set var="profileReviewsDeleteRedirect" value="${profileReviewsDeleteRedirect}#profileReviewsPanel"/>
                                <div class="profile-review-list">
                                    <c:forEach var="profileReview" items="${profileReviews}">
                                        <pa:profile-review-card
                                                reviewCard="${profileReview}"
                                                editable="${profileReview.ownedByCurrentUser}"
                                                deleteRedirect="${profileReviewsDeleteRedirect}"/>
                                    </c:forEach>
                                </div>
                                <c:if test="${profileReviewsTotalPages > 1}">
                                    <jsp:useBean id="profileReviewsPaginationParams" class="java.util.LinkedHashMap"/>
                                    <c:set target="${profileReviewsPaginationParams}" property="tab" value="reviews"/>
                                    <spring:message var="profileReviewsPaginationAria" code="profile.reviews.pagination.aria"/>
                                    <pa:pagination currentPage="${profileReviewsCurrentPage}"
                                                   totalPages="${profileReviewsTotalPages}"
                                                   baseUrl="${profileBasePath}"
                                                   extraParams="${profileReviewsPaginationParams}"
                                                   fragment="profileReviewsPanel"
                                                   ariaLabel="${profileReviewsPaginationAria}"/>
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
    <c:set var="profileToastCode" value="${not empty profileLanguageSuccessCode ? profileLanguageSuccessCode : submittedToastMessageCode}"/>
    <pa:toast messageCode="${profileToastCode}"/>
    <pa:script src="/js/shared/action-menu.js"/>
    <pa:script src="/js/shared/modal-utils.js"/>
    <pa:script src="/js/auth/auth-required-modal.js"/>
    <pa:script src="/js/shared/confirmation-modal.js"/>
    <pa:script src="/js/shared/form-submit-lock.js"/>
    <pa:script src="/js/profile/profile.js"/>
    <c:if test="${ownProfile}">
        <pa:script src="/js/profile/moderator-application-modal.js"/>
    </c:if>
    <pa:script src="/js/shared/toast.js"/>
    <pa:footer/>
</body>
</html>
