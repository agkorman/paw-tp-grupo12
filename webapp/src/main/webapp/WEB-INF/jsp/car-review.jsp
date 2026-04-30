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
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-tags.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-detail-modal.css'/>">
    <sec:authorize access="hasRole('ADMIN')">
        <link rel="stylesheet" href="<c:url value='/css/profile-modals.css?v=1'/>">
    </sec:authorize>
</head>
<body>
    <pa:nav activePage="reviews"/>

    <main class="reviews-page">
        <section class="review-hero">
            <div class="review-hero-inner">
                <div>
                    <h1>
                        <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/>
                    </h1>
                    <p class="subtitle">Datos sobre el vehículo y reseñas de los propietarios</p>
                </div>
                <div class="review-hero-actions">
                    <c:if test="${not empty averageRating}">
                        <div class="hero-stars-row" aria-label="${averageRating} de 5 estrellas">
                            <pa:rating-stars rating="${averageRating}" size="32" idPrefix="reviewHeroStar"/>
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
                                    data-car-year="${fn:escapeXml(selectedCar.year)}"
                                    data-car-body-type="${fn:escapeXml(selectedCar.bodyType)}"
                                    data-car-description="${fn:escapeXml(selectedCar.description)}"
                                    data-car-fuel-type="${fn:escapeXml(selectedCar.fuelType)}"
                                    data-car-horsepower="${fn:escapeXml(selectedCar.horsepower)}"
                                    data-car-airbag-count="${fn:escapeXml(selectedCar.airbagCount)}"
                                    data-car-transmission="${fn:escapeXml(selectedCar.transmission)}"
                                    data-car-fuel-consumption="${fn:escapeXml(selectedCar.fuelConsumption)}"
                                    data-car-max-speed-kmh="${fn:escapeXml(selectedCar.maxSpeedKmh)}"
                                    data-car-price-usd="${fn:escapeXml(selectedCar.priceUsd)}"
                                    data-car-image-url="${selectedCar.hasImage ? fn:escapeXml(selectedCarImageUrl) : ''}">
                                Editar
                            </button>
                            <button
                                    type="button"
                                    class="action-menu-danger"
                                    data-open-delete-car-modal
                                    data-car-delete-action="${fn:escapeXml(selectedCarDeleteUrl)}"
                                    data-car-title="${fn:escapeXml(selectedCar.brandName)} ${fn:escapeXml(selectedCar.model)}${not empty selectedCar.year ? ' ' : ''}${fn:escapeXml(selectedCar.year)}">
                                Eliminar
                            </button>
                        </pa:action-menu>
                    </sec:authorize>
                </div>
            </div>
            <c:if test="${fn:length(yearVariants) gt 1}">
                <nav class="car-year-switcher" aria-label="Años disponibles">
                    <div class="car-year-switcher-track">
                        <c:forEach var="yearVariant" items="${yearVariants}">
                            <c:url var="yearVariantUrl" value="/reviews">
                                <c:param name="carId" value="${yearVariant.carId}"/>
                            </c:url>
                            <a class="car-year-pill${yearVariant.selected ? ' is-active' : ''}"
                               href="${yearVariantUrl}"
                               <c:if test="${yearVariant.selected}">aria-current="page"</c:if>>
                                <c:choose>
                                    <c:when test="${not empty yearVariant.year}"><c:out value="${yearVariant.year}"/></c:when>
                                    <c:otherwise>Sin año</c:otherwise>
                                </c:choose>
                            </a>
                        </c:forEach>
                    </div>
                </nav>
            </c:if>
        </section>

        <section class="review-layout review-detail-layout">
            <div class="review-media-column">
                <pa:review-selected-car selectedCar="${selectedCar}"
                                        carImages="${carImages}"
                                        favorited="${selectedCarFavorited}"/>
                <pa:latest-review latestReview="${latestReview}"
                                  liked="${latestReviewLiked}"
                                  likeCount="${latestReviewLikeCount}"
                                  timeAgo="${latestReviewTimeAgo}"/>
            </div>

            <div class="review-side-column">
                <pa:review-car-info selectedCar="${selectedCar}" averageRating="${averageRating}"/>
            </div>
        </section>

        <pa:reviews-feed reviews="${reviews}" reviewThreads="${reviewThreads}" carId="${selectedCar.id}"
                         currentSort="${currentSort}"
                         currentPage="${currentPage}" totalPages="${totalPages}" totalItems="${totalItems}"/>
    </main>

    <pa:auth-required-modal/>
    <pa:review-detail-modal/>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:admin-car-form brands="${brands}" bodyTypes="${bodyTypes}" mode="admin"/>
        <pa:car-delete-modal/>
        <pa:review-delete-modal/>
        <pa:reply-delete-modal/>
    </sec:authorize>

    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/action-menu.js'/>"></script>
    <script src="<c:url value='/js/enhanced-filters.js?v=5'/>"></script>
    <script src="<c:url value='/js/car-image-carousel.js'/>"></script>
    <script src="<c:url value='/js/review-tag-chips.js'/>" defer></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <script src="<c:url value='/js/review-detail-modal.js'/>"></script>
    <sec:authorize access="hasRole('ADMIN')">
        <script src="<c:url value='/js/car-form.js?v=1'/>"></script>
        <script src="<c:url value='/js/car-admin.js?v=1'/>"></script>
        <script src="<c:url value='/js/admin-review-actions.js'/>"></script>
    </sec:authorize>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
