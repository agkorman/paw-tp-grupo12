<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Actividad | La Posta Autos</title>
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
    <c:url var="activityBaseUrl" value="/activity"/>
    <c:url var="activityLatestTabUrl" value="/activity">
        <c:param name="tab" value="latest"/>
    </c:url>
    <c:url var="activityFollowingLoginUrl" value="/login">
        <c:param name="redirect" value="/activity"/>
        <c:param name="intent" value="activity-following"/>
    </c:url>
    <c:url var="activityFavoritesLoginUrl" value="/login">
        <c:param name="redirect" value="/activity"/>
        <c:param name="intent" value="activity-favorites"/>
    </c:url>

    <main class="activity-page">
        <div class="activity-tabs-list" role="tablist" aria-label="Filtros de actividad">
            <a id="activityNewsTab"
               class="activity-tab"
               href="${activityLatestTabUrl}"
               role="tab"
               aria-selected="${activeTab eq 'latest'}"
               aria-controls="activityNewsPanel"
               data-activity-tab-target="activityNewsPanel">
                <span>Novedad</span>
                <strong><c:out value="${latestCount}"/></strong>
            </a>
            <c:choose>
                <c:when test="${authenticated}">
                    <c:url var="activityFollowingTabUrl" value="/activity">
                        <c:param name="tab" value="following"/>
                    </c:url>
                    <c:url var="activityFavoritesTabUrl" value="/activity">
                        <c:param name="tab" value="favorites"/>
                    </c:url>
                    <a id="activityFollowingTab"
                       class="activity-tab"
                       href="${activityFollowingTabUrl}"
                       role="tab"
                       aria-selected="${activeTab eq 'following'}"
                       aria-controls="activityFollowingPanel"
                       data-activity-tab-target="activityFollowingPanel">
                        <span>Seguidos</span>
                        <strong><c:out value="${followedCount}"/></strong>
                    </a>
                    <a id="activityFavoritesTab"
                       class="activity-tab"
                       href="${activityFavoritesTabUrl}"
                       role="tab"
                       aria-selected="${activeTab eq 'favorites'}"
                       aria-controls="activityFavoritesPanel"
                       data-activity-tab-target="activityFavoritesPanel">
                        <span>Autos favoritos</span>
                        <strong><c:out value="${favoriteCount}"/></strong>
                    </a>
                </c:when>
                <c:otherwise>
                    <a href="${activityFollowingLoginUrl}"
                       id="activityFollowingTab"
                       class="activity-tab activity-tab-login"
                       data-activity-login-tab>
                        <span>Seguidos</span>
                        <strong>0</strong>
                    </a>
                    <a href="${activityFavoritesLoginUrl}"
                       id="activityFavoritesTab"
                       class="activity-tab activity-tab-login"
                       data-activity-login-tab>
                        <span>Autos favoritos</span>
                        <strong>0</strong>
                    </a>
                </c:otherwise>
            </c:choose>
        </div>

        <c:choose>
            <c:when test="${activeTab eq 'following'}">
                <section id="activityFollowingPanel"
                         class="activity-tab-panel"
                         role="tabpanel"
                         aria-labelledby="activityFollowingTab">
                    <c:choose>
                        <c:when test="${empty activityReviews}">
                            <div class="activity-empty-state">
                                <p>No hay reseñas recientes de usuarios que sigues.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="activity-feed" aria-label="Reseñas de usuarios seguidos">
                                <c:forEach var="activityReview" items="${activityReviews}">
                                    <pa:activity-review-card reviewCard="${activityReview}"/>
                                </c:forEach>
                            </div>
                            <c:if test="${activityCurrentPage < activityTotalPages}">
                                <c:url var="activityShowMoreUrl" value="/activity">
                                    <c:param name="tab" value="following"/>
                                    <c:param name="page" value="${activityCurrentPage + 1}"/>
                                </c:url>
                                <div class="reviews-feed-more profile-show-more">
                                    <a class="btn-secondary reviews-show-more"
                                       href="${activityShowMoreUrl}"
                                       data-review-show-more="true"
                                       data-fragment-url="${activityBaseUrl}"
                                       data-target="#activityFollowingPanel"
                                       data-list-selector=".activity-feed"
                                       data-item-selector=".activity-feed > .activity-review-card">
                                        Mostrar más reseñas
                                    </a>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>
            <c:when test="${activeTab eq 'favorites'}">
                <section id="activityFavoritesPanel"
                         class="activity-tab-panel"
                         role="tabpanel"
                         aria-labelledby="activityFavoritesTab">
                    <c:choose>
                        <c:when test="${empty activityReviews}">
                            <div class="activity-empty-state">
                                <p>No hay reseñas recientes sobre tus autos favoritos.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="activity-feed" aria-label="Reseñas de autos favoritos">
                                <c:forEach var="activityReview" items="${activityReviews}">
                                    <pa:activity-review-card reviewCard="${activityReview}"/>
                                </c:forEach>
                            </div>
                            <c:if test="${activityCurrentPage < activityTotalPages}">
                                <c:url var="activityShowMoreUrl" value="/activity">
                                    <c:param name="tab" value="favorites"/>
                                    <c:param name="page" value="${activityCurrentPage + 1}"/>
                                </c:url>
                                <div class="reviews-feed-more profile-show-more">
                                    <a class="btn-secondary reviews-show-more"
                                       href="${activityShowMoreUrl}"
                                       data-review-show-more="true"
                                       data-fragment-url="${activityBaseUrl}"
                                       data-target="#activityFavoritesPanel"
                                       data-list-selector=".activity-feed"
                                       data-item-selector=".activity-feed > .activity-review-card">
                                        Mostrar más reseñas
                                    </a>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:when>
            <c:otherwise>
                <section id="activityNewsPanel"
                         class="activity-tab-panel"
                         role="tabpanel"
                         aria-labelledby="activityNewsTab">
                    <c:choose>
                        <c:when test="${empty activityReviews}">
                            <div class="activity-empty-state">
                                <p>No hay reseñas recientes para mostrar.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="activity-feed" aria-label="Últimas reseñas">
                                <c:forEach var="activityReview" items="${activityReviews}">
                                    <pa:activity-review-card reviewCard="${activityReview}"/>
                                </c:forEach>
                            </div>
                            <c:if test="${activityCurrentPage < activityTotalPages}">
                                <c:url var="activityShowMoreUrl" value="/activity">
                                    <c:param name="tab" value="latest"/>
                                    <c:param name="page" value="${activityCurrentPage + 1}"/>
                                </c:url>
                                <div class="reviews-feed-more profile-show-more">
                                    <a class="btn-secondary reviews-show-more"
                                       href="${activityShowMoreUrl}"
                                       data-review-show-more="true"
                                       data-fragment-url="${activityBaseUrl}"
                                       data-target="#activityNewsPanel"
                                       data-list-selector=".activity-feed"
                                       data-item-selector=".activity-feed > .activity-review-card">
                                        Mostrar más reseñas
                                    </a>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:otherwise>
        </c:choose>
    </main>

    <script src="<c:url value='/js/enhanced-filters.js?v=6'/>"></script>
    <script src="<c:url value='/js/activity.js?v=3'/>"></script>
</body>
</html>
