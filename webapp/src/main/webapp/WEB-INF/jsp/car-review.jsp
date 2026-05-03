<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="review.page.title" styles="/css/reviews.css|/css/car-image-carousel.css|/css/review-tags.css"/>
<body>
    <pa:nav activePage="reviews"/>

    <main class="reviews-page">
        <section class="review-hero">
            <div class="review-hero-inner">
                <div>
                    <h1>
                        <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/>
                    </h1>
                    <p class="subtitle"><spring:message code="review.page.subtitle"/></p>
                </div>
                <div class="review-hero-actions">
                    <c:if test="${not empty averageRating}">
                        <spring:message var="ratingAria" code="review.rating.aria" arguments="${averageRating}"/>
                        <div class="hero-stars-row" aria-label="${fn:escapeXml(ratingAria)}">
                            <pa:rating-stars rating="${averageRating}" size="32" idPrefix="reviewHeroStar"/>
                        </div>
                    </c:if>
                    <a href="#reviewsFeed" class="btn-secondary hero-see-reviews-btn">
                        <spring:message code="review.page.seeReviews"/>
                        <pa:icon name="arrow-down" size="14"/>
                    </a>
                    <sec:authorize access="hasRole('ADMIN')">
                        <c:url var="selectedCarEditUrl" value="/admin/cars/${selectedCar.id}/edit"/>
                        <c:url var="selectedCarDeleteUrl" value="/admin/cars/${selectedCar.id}/delete"/>
                        <c:url var="selectedCarImageUrl" value="/car-image">
                            <c:param name="carId" value="${selectedCar.id}"/>
                        </c:url>
                        <spring:message var="carOptionsLabel" code="cars.actionMenu.open"/>
                        <pa:action-menu label="${carOptionsLabel}">
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
                                <spring:message code="common.action.edit"/>
                            </button>
                            <button
                                    type="button"
                                    class="action-menu-danger"
                                    data-open-delete-car-modal
                                    data-car-delete-action="${fn:escapeXml(selectedCarDeleteUrl)}"
                                    data-car-title="${fn:escapeXml(selectedCar.brandName)} ${fn:escapeXml(selectedCar.model)}${not empty selectedCar.year ? ' ' : ''}${fn:escapeXml(selectedCar.year)}">
                                <spring:message code="common.action.delete"/>
                            </button>
                        </pa:action-menu>
                    </sec:authorize>
                </div>
            </div>
            <c:if test="${fn:length(yearVariants) gt 1}">
                <spring:message var="availableYearsLabel" code="review.page.years"/>
                <nav class="car-year-switcher" aria-label="${fn:escapeXml(availableYearsLabel)}">
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
                                    <c:otherwise><spring:message code="review.page.noYear"/></c:otherwise>
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
                                  likeCount="${latestReviewLikeCount}"/>
            </div>

            <div class="review-side-column">
                <pa:review-car-info selectedCar="${selectedCar}" averageRating="${averageRating}"/>
            </div>
        </section>

        <pa:reviews-feed reviews="${reviews}" reviewThreads="${reviewThreads}" carId="${selectedCar.id}"
                         currentSort="${currentSort}"
                         currentPage="${currentPage}" totalPages="${totalPages}" totalItems="${totalItems}"
                         currentUserId="${currentUserId}"/>
    </main>

    <pa:auth-required-modal/>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:toast/>
        <pa:review-hide-modal/>
        <pa:car-delete-modal/>
    </sec:authorize>

    <pa:script src="/js/reactions.js"/>
    <pa:script src="/js/review-replies.js"/>
    <pa:script src="/js/action-menu.js"/>
    <pa:script src="/js/enhanced-filters.js"/>
    <pa:script src="/js/car-image-carousel.js"/>
    <pa:script src="/js/review-anchor-highlight.js"/>
    <pa:script src="/js/review-tag-chips.js" defer="true"/>
    <pa:script src="/js/auth-required-modal.js"/>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:script src="/js/car-admin.js"/>
        <pa:script src="/js/toast.js"/>
        <pa:script src="/js/review-moderation.js"/>
    </sec:authorize>
    <pa:script src="/js/form-submit-lock.js"/>
    <pa:footer/>
</body>
</html>
