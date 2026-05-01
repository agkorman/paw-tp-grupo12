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
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-tags.css'/>">
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

    <main class="activity-page" data-activity-tabs>
        <div class="activity-tabs-list" role="tablist" aria-label="Filtros de actividad">
            <button type="button"
                    id="activityNewsTab"
                    class="activity-tab"
                    role="tab"
                    aria-selected="true"
                    aria-controls="activityNewsPanel"
                    data-activity-tab-target="activityNewsPanel">
                <span>Novedad</span>
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
                        <span>Seguidos</span>
                        <strong><c:out value="${fn:length(followedActivityReviews)}"/></strong>
                    </button>
                    <button type="button"
                            id="activityFavoritesTab"
                            class="activity-tab"
                            role="tab"
                            aria-selected="false"
                            aria-controls="activityFavoritesPanel"
                            data-activity-tab-target="activityFavoritesPanel">
                        <span>Autos favoritos</span>
                        <strong><c:out value="${fn:length(favoriteCarActivityReviews)}"/></strong>
                    </button>
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

        <section id="activityNewsPanel"
                 class="activity-tab-panel"
                 role="tabpanel"
                 aria-labelledby="activityNewsTab">
            <c:choose>
                <c:when test="${empty latestActivityReviews}">
                    <div class="activity-empty-state">
                        <p>No hay reseñas recientes para mostrar.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="activity-panel-layout">
                        <div class="activity-feed" aria-label="Últimas reseñas">
                            <c:forEach var="activityReview" items="${latestActivityReviews}" varStatus="status">
                                <pa:activity-review-card reviewCard="${activityReview}"
                                                         idPrefix="activityNewsReviewPreview-${status.index}"/>
                            </c:forEach>
                        </div>
                        <aside class="activity-preview-column" aria-label="Vista previa de reseña">
                            <c:forEach var="activityReview" items="${latestActivityReviews}" varStatus="status">
                                <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                                  idPrefix="activityNewsReviewPreview-${status.index}"/>
                            </c:forEach>
                        </aside>
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
                            <p>No hay reseñas recientes de usuarios que sigues.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="activity-panel-layout">
                            <div class="activity-feed" aria-label="Reseñas de usuarios seguidos">
                                <c:forEach var="activityReview" items="${followedActivityReviews}" varStatus="status">
                                    <pa:activity-review-card reviewCard="${activityReview}"
                                                             idPrefix="activityFollowingReviewPreview-${status.index}"/>
                                </c:forEach>
                            </div>
                            <aside class="activity-preview-column" aria-label="Vista previa de reseña">
                                <c:forEach var="activityReview" items="${followedActivityReviews}" varStatus="status">
                                    <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                                      idPrefix="activityFollowingReviewPreview-${status.index}"/>
                                </c:forEach>
                            </aside>
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
                            <p>No hay reseñas recientes sobre tus autos favoritos.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="activity-panel-layout">
                            <div class="activity-feed" aria-label="Reseñas de autos favoritos">
                                <c:forEach var="activityReview" items="${favoriteCarActivityReviews}" varStatus="status">
                                    <pa:activity-review-card reviewCard="${activityReview}"
                                                             idPrefix="activityFavoriteReviewPreview-${status.index}"/>
                                </c:forEach>
                            </div>
                            <aside class="activity-preview-column" aria-label="Vista previa de reseña">
                                <c:forEach var="activityReview" items="${favoriteCarActivityReviews}" varStatus="status">
                                    <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                                      idPrefix="activityFavoriteReviewPreview-${status.index}"/>
                                </c:forEach>
                            </aside>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:if>
    </main>

    <script src="<c:url value='/js/activity.js?v=3'/>"></script>
</body>
</html>
