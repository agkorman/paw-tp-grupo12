<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <spring:message var="activityTitle" code="activity.title"/>
    <title><c:out value="${activityTitle}"/></title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=5'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-tags.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/activity.css'/>">
</head>
<body>
    <pa:nav activePage="activity"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
    <c:url var="activityBaseUrl" value="/activity"/>
    <c:url var="activityLatestTabUrl" value="/activity">
        <c:param name="tab" value="latest"/>
    </c:url>
    <c:url var="activityFollowingTabUrl" value="/activity">
        <c:param name="tab" value="following"/>
    </c:url>
    <c:url var="activityFavoritesTabUrl" value="/activity">
        <c:param name="tab" value="favorites"/>
    </c:url>
    <c:url var="activityFollowingLoginUrl" value="/login">
        <c:param name="redirect" value="/activity"/>
        <c:param name="intent" value="activity-following"/>
    </c:url>
    <c:url var="activityFavoritesLoginUrl" value="/login">
        <c:param name="redirect" value="/activity"/>
        <c:param name="intent" value="activity-favorites"/>
    </c:url>
    <spring:message var="activityKicker" code="activity.kicker"/>
    <spring:message var="activityFollowingLabel" code="activity.tab.following"/>
    <spring:message var="activityFavoritesLabel" code="activity.tab.favorites"/>
    <spring:message var="activityFiltersAria" code="activity.filters.aria"/>
    <spring:message var="activityLatestAria" code="activity.latest.aria"/>
    <spring:message var="activityFollowingAria" code="activity.following.aria"/>
    <spring:message var="activityFavoritesAria" code="activity.favorites.aria"/>
    <spring:message var="activityPreviewAria" code="activity.preview.aria"/>

    <main class="activity-page" data-activity-tabs>
        <c:choose>
            <c:when test="${authenticated}">
                <pa:subtabs tabCount="3"
                            labels="${activityKicker}|${activityFollowingLabel}|${activityFavoritesLabel}"
                            hrefs="${activityLatestTabUrl}|${activityFollowingTabUrl}|${activityFavoritesTabUrl}"
                            counts="${latestCount}|${followedCount}|${favoriteCount}"
                            values="latest|following|favorites"
                            activeValue="${activeTab}"
                            ariaLabel="${activityFiltersAria}"/>
            </c:when>
            <c:otherwise>
                <pa:subtabs tabCount="3"
                            labels="${activityKicker}|${activityFollowingLabel}|${activityFavoritesLabel}"
                            hrefs="${activityLatestTabUrl}|${activityFollowingLoginUrl}|${activityFavoritesLoginUrl}"
                            counts="${latestCount}|0|0"
                            values="latest|following|favorites"
                            activeValue="${activeTab}"
                            ariaLabel="${activityFiltersAria}"/>
            </c:otherwise>
        </c:choose>

        <c:choose>
            <c:when test="${activeTab eq 'following'}">
                <section id="activityFollowingPanel" class="activity-tab-panel">
                    <c:choose>
                        <c:when test="${empty activityReviews}">
                            <div class="activity-empty-state">
                                <p><spring:message code="activity.empty.following"/></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="activity-panel-layout">
                                <div class="activity-feed" aria-label="${activityFollowingAria}">
                                    <c:forEach var="activityReview" items="${activityReviews}" varStatus="status">
                                        <pa:activity-review-card reviewCard="${activityReview}"
                                                                 idPrefix="activityFollowingReviewPreview-${activityCurrentPage}-${status.index}"/>
                                    </c:forEach>
                                    <c:if test="${activityCurrentPage < activityTotalPages}">
                                        <c:url var="activityShowMoreUrl" value="/activity">
                                            <c:param name="tab" value="following"/>
                                            <c:param name="page" value="${activityCurrentPage + 1}"/>
                                        </c:url>
                                        <div class="reviews-feed-more profile-show-more">
                                            <a href="${activityShowMoreUrl}"
                                               data-review-show-more="true"
                                               data-fragment-url="${activityBaseUrl}"
                                               data-target="#activityFollowingPanel"
                                               data-list-selector=".activity-feed"
                                               data-item-selector=".activity-feed > .activity-review-card"
                                               data-preview-list-selector=".activity-preview-column"
                                               data-preview-item-selector=".activity-preview-column > .activity-review-preview-panel">
                                                <span class="visually-hidden"><spring:message code="common.action.showMoreReviews"/></span>
                                            </a>
                                        </div>
                                    </c:if>
                                </div>
                                <aside class="activity-preview-column" aria-label="${activityPreviewAria}">
                                    <c:forEach var="activityReview" items="${activityReviews}" varStatus="status">
                                        <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                                          idPrefix="activityFollowingReviewPreview-${activityCurrentPage}-${status.index}"/>
                                    </c:forEach>
                                </aside>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>
            <c:when test="${activeTab eq 'favorites'}">
                <section id="activityFavoritesPanel" class="activity-tab-panel">
                    <c:choose>
                        <c:when test="${empty activityReviews}">
                            <div class="activity-empty-state">
                                <p><spring:message code="activity.empty.favorites"/></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="activity-panel-layout">
                                <div class="activity-feed" aria-label="${activityFavoritesAria}">
                                    <c:forEach var="activityReview" items="${activityReviews}" varStatus="status">
                                        <pa:activity-review-card reviewCard="${activityReview}"
                                                                 idPrefix="activityFavoriteReviewPreview-${activityCurrentPage}-${status.index}"/>
                                    </c:forEach>
                                    <c:if test="${activityCurrentPage < activityTotalPages}">
                                        <c:url var="activityShowMoreUrl" value="/activity">
                                            <c:param name="tab" value="favorites"/>
                                            <c:param name="page" value="${activityCurrentPage + 1}"/>
                                        </c:url>
                                        <div class="reviews-feed-more profile-show-more">
                                            <a href="${activityShowMoreUrl}"
                                               data-review-show-more="true"
                                               data-fragment-url="${activityBaseUrl}"
                                               data-target="#activityFavoritesPanel"
                                               data-list-selector=".activity-feed"
                                               data-item-selector=".activity-feed > .activity-review-card"
                                               data-preview-list-selector=".activity-preview-column"
                                               data-preview-item-selector=".activity-preview-column > .activity-review-preview-panel">
                                                <span class="visually-hidden"><spring:message code="common.action.showMoreReviews"/></span>
                                            </a>
                                        </div>
                                    </c:if>
                                </div>
                                <aside class="activity-preview-column" aria-label="${activityPreviewAria}">
                                    <c:forEach var="activityReview" items="${activityReviews}" varStatus="status">
                                        <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                                          idPrefix="activityFavoriteReviewPreview-${activityCurrentPage}-${status.index}"/>
                                    </c:forEach>
                                </aside>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>
            <c:otherwise>
                <section id="activityNewsPanel" class="activity-tab-panel">
                    <c:choose>
                        <c:when test="${empty activityReviews}">
                            <div class="activity-empty-state">
                                <p><spring:message code="activity.empty.all"/></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="activity-panel-layout">
                                <div class="activity-feed" aria-label="${activityLatestAria}">
                                    <c:forEach var="activityReview" items="${activityReviews}" varStatus="status">
                                        <pa:activity-review-card reviewCard="${activityReview}"
                                                                 idPrefix="activityNewsReviewPreview-${activityCurrentPage}-${status.index}"/>
                                    </c:forEach>
                                    <c:if test="${activityCurrentPage < activityTotalPages}">
                                        <c:url var="activityShowMoreUrl" value="/activity">
                                            <c:param name="tab" value="latest"/>
                                            <c:param name="page" value="${activityCurrentPage + 1}"/>
                                        </c:url>
                                        <div class="reviews-feed-more profile-show-more">
                                            <a href="${activityShowMoreUrl}"
                                               data-review-show-more="true"
                                               data-fragment-url="${activityBaseUrl}"
                                               data-target="#activityNewsPanel"
                                               data-list-selector=".activity-feed"
                                               data-item-selector=".activity-feed > .activity-review-card"
                                               data-preview-list-selector=".activity-preview-column"
                                               data-preview-item-selector=".activity-preview-column > .activity-review-preview-panel">
                                                <span class="visually-hidden"><spring:message code="common.action.showMoreReviews"/></span>
                                            </a>
                                        </div>
                                    </c:if>
                                </div>
                                <aside class="activity-preview-column" aria-label="${activityPreviewAria}">
                                    <c:forEach var="activityReview" items="${activityReviews}" varStatus="status">
                                        <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                                          idPrefix="activityNewsReviewPreview-${activityCurrentPage}-${status.index}"/>
                                    </c:forEach>
                                </aside>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:otherwise>
        </c:choose>
    </main>

    <script src="<c:url value='/js/enhanced-filters.js?v=7'/>"></script>
    <script src="<c:url value='/js/activity.js?v=3'/>"></script>
    <pa:footer/>
</body>
</html>
