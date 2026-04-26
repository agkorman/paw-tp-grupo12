<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reseñas | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css?v=4'/>">
</head>
<body>
    <pa:nav activePage="reviews"/>

    <main class="reviews-page">
        <section class="review-hero">
            <div class="review-hero-inner">
                <div>
                    <h1><c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></h1>
                    <p class="subtitle">Datos sobre el vehículo y reseñas de los propietarios</p>
                </div>
                <div class="review-hero-actions">
                    <c:if test="${not empty averageRating}">
                        <div class="hero-stars-row" aria-label="${averageRating} de 5 estrellas">
                            <c:forEach var="i" begin="1" end="5">
                                <svg viewBox="0 0 24 24" width="32" height="32" aria-hidden="true">
                                    <c:choose>
                                        <c:when test="${averageRating >= i}">
                                            <path fill="#ff5719" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                        </c:when>
                                        <c:when test="${averageRating >= i - 0.5}">
                                            <defs>
                                                <linearGradient id="hsg${i}" x1="0" x2="1" y1="0" y2="0">
                                                    <stop offset="50%" stop-color="#ff5719"/>
                                                    <stop offset="50%" stop-color="#3a3a3a"/>
                                                </linearGradient>
                                            </defs>
                                            <path fill="url(#hsg${i})" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                        </c:when>
                                        <c:otherwise>
                                            <path fill="#3a3a3a" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                        </c:otherwise>
                                    </c:choose>
                                </svg>
                            </c:forEach>
                        </div>
                    </c:if>
                    <a href="#reviewsFeed" class="btn-secondary hero-see-reviews-btn">
                        Ver reseñas
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                            <path d="M12 5v14M5 12l7 7 7-7"/>
                        </svg>
                    </a>
                    <sec:authorize access="hasRole('ADMIN')">
                        <c:url var="selectedCarEditUrl" value="/admin/cars/${selectedCar.id}"/>
                        <c:url var="selectedCarDeleteUrl" value="/admin/cars/${selectedCar.id}/delete"/>
                        <c:url var="selectedCarImageUrl" value="/car-image">
                            <c:param name="carId" value="${selectedCar.id}"/>
                        </c:url>
                        <pa:action-menu label="Abrir opciones de auto">
                            <button
                                    type="button"
                                    data-open-create-car-modal="edit-car"
                                    data-car-action="${fn:escapeXml(selectedCarEditUrl)}"
                                    data-car-id="${fn:escapeXml(selectedCar.id)}"
                                    data-car-brand="${fn:escapeXml(selectedCar.brandName)}"
                                    data-car-model="${fn:escapeXml(selectedCar.model)}"
                                    data-car-body-type="${fn:escapeXml(selectedCar.bodyType)}"
                                    data-car-description="${fn:escapeXml(selectedCar.description)}"
                                    data-car-fuel-type="${fn:escapeXml(selectedCar.fuelType)}"
                                    data-car-horsepower="${fn:escapeXml(selectedCar.horsepower)}"
                                    data-car-airbag-count="${fn:escapeXml(selectedCar.airbagCount)}"
                                    data-car-transmission="${fn:escapeXml(selectedCar.transmission)}"
                                    data-car-fuel-consumption="${fn:escapeXml(selectedCar.fuelConsumption)}"
                                    data-car-max-speed-kmh="${fn:escapeXml(selectedCar.maxSpeedKmh)}"
                                    data-car-image-url="${selectedCar.hasImage ? fn:escapeXml(selectedCarImageUrl) : ''}">
                                Editar
                            </button>
                            <button
                                    type="button"
                                    class="action-menu-danger"
                                    data-open-delete-car-modal
                                    data-car-delete-action="${fn:escapeXml(selectedCarDeleteUrl)}"
                                    data-car-title="${fn:escapeXml(selectedCar.brandName)} ${fn:escapeXml(selectedCar.model)}">
                                Eliminar
                            </button>
                        </pa:action-menu>
                    </sec:authorize>
                </div>
            </div>
        </section>

        <section class="review-layout review-detail-layout">
            <div class="review-media-column">
                <pa:review-selected-car selectedCar="${selectedCar}"
                                        carImages="${carImages}"
                                        favorited="${selectedCarFavorited}"/>
                <pa:latest-review latestReview="${latestReview}"
                                  liked="${latestReviewLiked}"
                                  likeCount="${latestReviewLikeCount}"/>
            </div>

            <div class="review-side-column">
                <pa:review-car-info selectedCar="${selectedCar}" averageRating="${averageRating}"/>
            </div>
        </section>

        <pa:reviews-feed reviews="${reviews}" reviewThreads="${reviewThreads}" carId="${selectedCar.id}" currentSort="${currentSort}"/>
    </main>

    <pa:auth-required-modal/>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:admin-car-form brands="${brands}" bodyTypes="${bodyTypes}" mode="admin"/>
        <pa:car-delete-modal/>
    </sec:authorize>

    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/action-menu.js'/>"></script>
    <script src="<c:url value='/js/enhanced-filters.js'/>"></script>
    <script src="<c:url value='/js/car-image-carousel.js'/>"></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <sec:authorize access="hasRole('ADMIN')">
        <script src="<c:url value='/js/car-form.js?v=1'/>"></script>
        <script src="<c:url value='/js/car-admin.js?v=1'/>"></script>
    </sec:authorize>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
