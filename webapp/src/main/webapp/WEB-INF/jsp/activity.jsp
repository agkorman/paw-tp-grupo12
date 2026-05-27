<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="activity.title" styles="/css/reviews.css|/css/review-preview.css|/css/review-tags.css|/css/activity.css"/>
<body>
    <pa:nav activePage="activity"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
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

    <spring:message var="usersSearchPlaceholder" code="users.search.placeholder"/>
    <spring:message var="usersSearchAria" code="users.search.aria"/>
    <spring:message var="usersSearchAction" code="common.action.search"/>
    <main class="activity-page" data-activity-tabs>
        <form class="activity-users-search" method="get" action="<c:url value='/users/search'/>" role="search">
            <label class="activity-users-search-field" for="activity-users-search-input">
                <span class="activity-users-search-icon" aria-hidden="true">
                    <pa:icon name="search" size="20"/>
                </span>
                <input id="activity-users-search-input"
                       class="activity-users-search-input"
                       type="search"
                       name="q"
                       placeholder="${usersSearchPlaceholder}"
                       autocomplete="off"
                       aria-label="${usersSearchAria}">
            </label>
            <button type="submit" class="activity-users-search-submit"><c:out value="${usersSearchAction}"/></button>
        </form>
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
                <pa:activity-tab-panel panelId="activityFollowingPanel"
                                       tab="following"
                                       reviews="${activityReviews}"
                                       currentPage="${activityCurrentPage}"
                                       totalPages="${activityTotalPages}"
                                       emptyCode="activity.empty.following"
                                       feedAria="${activityFollowingAria}"
                                       previewAria="${activityPreviewAria}"
                                       idPrefix="activityFollowingReviewPreview"/>
            </c:when>
            <c:when test="${activeTab eq 'favorites'}">
                <pa:activity-tab-panel panelId="activityFavoritesPanel"
                                       tab="favorites"
                                       reviews="${activityReviews}"
                                       currentPage="${activityCurrentPage}"
                                       totalPages="${activityTotalPages}"
                                       emptyCode="activity.empty.favorites"
                                       feedAria="${activityFavoritesAria}"
                                       previewAria="${activityPreviewAria}"
                                       idPrefix="activityFavoriteReviewPreview"/>
            </c:when>
            <c:otherwise>
                <pa:activity-tab-panel panelId="activityNewsPanel"
                                       tab="latest"
                                       reviews="${activityReviews}"
                                       currentPage="${activityCurrentPage}"
                                       totalPages="${activityTotalPages}"
                                       emptyCode="activity.empty.all"
                                       feedAria="${activityLatestAria}"
                                       previewAria="${activityPreviewAria}"
                                       idPrefix="activityNewsReviewPreview"/>
            </c:otherwise>
        </c:choose>
    </main>

    <pa:script src="/js/activity/activity.js"/>
    <pa:footer/>
</body>
</html>
