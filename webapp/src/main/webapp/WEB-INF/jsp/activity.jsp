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
    <title><spring:message code="activity.title"/></title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/activity.css'/>">
</head>
<body>
    <pa:nav activePage="activity"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
    <c:url var="activityFollowingLoginUrl" value="/login">
        <c:param name="redirect" value="/activity"/>
        <c:param name="intent" value="activity-following"/>
    </c:url>
    <c:url var="activityFavoritesLoginUrl" value="/login">
        <c:param name="redirect" value="/activity"/>
        <c:param name="intent" value="activity-favorites"/>
    </c:url>
    <spring:message var="activityFiltersAria" code="activity.filters.aria"/>
    <spring:message var="activityLatestAria" code="activity.latest.aria"/>
    <spring:message var="activityFollowingAria" code="activity.following.aria"/>
    <spring:message var="activityFavoritesAria" code="activity.favorites.aria"/>

    <main class="activity-page" data-activity-tabs>
        <div class="activity-tabs-list" role="tablist" aria-label="${activityFiltersAria}">
            <button type="button"
                    id="activityNewsTab"
                    class="activity-tab"
                    role="tab"
                    aria-selected="true"
                    aria-controls="activityNewsPanel"
                    data-activity-tab-target="activityNewsPanel">
                <span><spring:message code="activity.kicker"/></span>
                <strong><c:out value="${fn:length(latestActivityReviews)}"/></strong>
            </button>
            <c:choose>
                <c:when test="${authenticated}">
                    <button type="button"
                            id="activityFollowingTab"
                            class="activity-tab"
                            role="tab"
                            aria-selected="false"
                            aria-controls="activityFollowingPanel"
                            data-activity-tab-target="activityFollowingPanel">
                        <span><spring:message code="activity.tab.following"/></span>
                        <strong><c:out value="${fn:length(followedActivityReviews)}"/></strong>
                    </button>
                    <button type="button"
                            id="activityFavoritesTab"
                            class="activity-tab"
                            role="tab"
                            aria-selected="false"
                            aria-controls="activityFavoritesPanel"
                            data-activity-tab-target="activityFavoritesPanel">
                        <span><spring:message code="activity.tab.favorites"/></span>
                        <strong><c:out value="${fn:length(favoriteCarActivityReviews)}"/></strong>
                    </button>
                </c:when>
                <c:otherwise>
                    <a href="${activityFollowingLoginUrl}"
                       id="activityFollowingTab"
                       class="activity-tab activity-tab-login"
                       data-activity-login-tab>
                        <span><spring:message code="activity.tab.following"/></span>
                        <strong>0</strong>
                    </a>
                    <a href="${activityFavoritesLoginUrl}"
                       id="activityFavoritesTab"
                       class="activity-tab activity-tab-login"
                       data-activity-login-tab>
                        <span><spring:message code="activity.tab.favorites"/></span>
                        <strong>0</strong>
                    </a>
                </c:otherwise>
            </c:choose>
        </div>

        <section id="activityNewsPanel"
                 class="activity-tab-panel"
                 role="tabpanel"
                 aria-labelledby="activityNewsTab">
            <c:choose>
                <c:when test="${empty latestActivityReviews}">
                    <div class="activity-empty-state">
                        <p><spring:message code="activity.empty.all"/></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="activity-feed" aria-label="${activityLatestAria}">
                        <c:forEach var="activityReview" items="${latestActivityReviews}">
                            <pa:activity-review-card reviewCard="${activityReview}"/>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <c:if test="${authenticated}">
            <section id="activityFollowingPanel"
                     class="activity-tab-panel"
                     role="tabpanel"
                     aria-labelledby="activityFollowingTab">
                <c:choose>
                    <c:when test="${empty followedActivityReviews}">
                        <div class="activity-empty-state">
                            <p><spring:message code="activity.empty.following"/></p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="activity-feed" aria-label="${activityFollowingAria}">
                            <c:forEach var="activityReview" items="${followedActivityReviews}">
                                <pa:activity-review-card reviewCard="${activityReview}"/>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section id="activityFavoritesPanel"
                     class="activity-tab-panel"
                     role="tabpanel"
                     aria-labelledby="activityFavoritesTab">
                <c:choose>
                    <c:when test="${empty favoriteCarActivityReviews}">
                        <div class="activity-empty-state">
                            <p><spring:message code="activity.empty.favorites"/></p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="activity-feed" aria-label="${activityFavoritesAria}">
                            <c:forEach var="activityReview" items="${favoriteCarActivityReviews}">
                                <pa:activity-review-card reviewCard="${activityReview}"/>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:if>
    </main>

    <script src="<c:url value='/js/activity.js?v=2'/>"></script>
</body>
</html>
