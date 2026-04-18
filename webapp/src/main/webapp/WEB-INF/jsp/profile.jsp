<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Perfil | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile.css'/>">
</head>
<body>
    <pa:nav activePage="profile"/>

    <main class="profile-page">
        <section class="profile-hero" aria-labelledby="profileName">
            <div class="profile-avatar" aria-hidden="true">
                <span>JR</span>
            </div>

            <div class="profile-summary">
                <h1 id="profileName"><c:out value="${profile.name}"/></h1>
                <p class="profile-email"><c:out value="${profile.email}"/></p>

                <dl class="profile-stats" aria-label="Estadísticas del perfil">
                    <div>
                        <dt><c:out value="${profile.reviewCount}"/></dt>
                        <dd>Reviews</dd>
                    </div>
                    <div>
                        <dt><c:out value="${profile.followingCount}"/></dt>
                        <dd>Seguidos</dd>
                    </div>
                    <div>
                        <dt><c:out value="${profile.followerCount}"/></dt>
                        <dd>Seguidores</dd>
                    </div>
                </dl>
            </div>

            <c:choose>
                <c:when test="${ownProfile}">
                    <button type="button" class="btn-primary profile-action-button">Editar perfil</button>
                </c:when>
                <c:otherwise>
                    <button
                            type="button"
                            class="btn-primary profile-action-button profile-follow-button ${followingProfile ? 'is-following' : ''}"
                            data-follow-toggle
                            data-following="${followingProfile}"
                            aria-pressed="${followingProfile}">
                        <c:choose>
                            <c:when test="${followingProfile}">Seguido</c:when>
                            <c:otherwise>Seguir</c:otherwise>
                        </c:choose>
                    </button>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="profile-reviews-section" aria-labelledby="profileReviewsTitle">
            <h2 id="profileReviewsTitle">
                <c:choose>
                    <c:when test="${ownProfile}">Mis reviews</c:when>
                    <c:otherwise>Reviews</c:otherwise>
                </c:choose>
            </h2>

            <c:choose>
                <c:when test="${empty profileReviews}">
                    <div class="profile-empty-state">
                        <p>Todavía no hay reviews publicadas.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="profile-review-list">
                        <c:forEach var="profileReview" items="${profileReviews}">
                            <pa:profile-review-card reviewCard="${profileReview}"/>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <pa:footer/>
    <script src="<c:url value='/js/profile.js'/>"></script>
</body>
</html>
