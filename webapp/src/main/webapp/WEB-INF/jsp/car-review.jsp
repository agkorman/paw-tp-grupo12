<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="review.page.title" styles="/css/reviews.css|/css/car-image-carousel.css|/css/review-tags.css|/css/profile-modal.css"/>
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
                        <spring:message var="carOptionsLabel" code="cars.actionMenu.open"/>
                        <pa:action-menu label="${carOptionsLabel}">
                            <a href="${selectedCarEditUrl}">
                                <spring:message code="common.action.edit"/>
                            </a>
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
                            <c:url var="yearVariantUrl" value="/reviews/car/${yearVariant.carId}"/>
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
            </div>

            <div class="review-side-column">
                <pa:review-car-info selectedCar="${selectedCar}" averageRating="${averageRating}"/>
            </div>
        </section>

        <section class="review-description-bar">
            <c:url var="newReviewUrl" value="/reviews/new">
                <c:param name="carId" value="${selectedCar.id}"/>
            </c:url>
            <c:url var="newReviewLoginUrl" value="/login">
                <c:param name="redirect" value="/reviews/new?carId=${selectedCar.id}"/>
                <c:param name="intent" value="create-review"/>
            </c:url>
            <spring:message var="createReviewAuthAction" code="review.authRequired.createAction"/>
            <div class="review-description-content">
                <span class="car-info-label"><spring:message code="review.carInfo.description"/></span>
                <p class="car-info-description">
                    <c:choose>
                        <c:when test="${not empty selectedCar.description}"><c:out value="${selectedCar.description}"/></c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </p>
            </div>
            <c:choose>
                <c:when test="${not empty pageContext.request.userPrincipal}">
                    <a id="openReviewFormBtn"
                       href="${newReviewUrl}"
                       class="btn-primary add-review-btn"
                       data-auth-resume-intent="create-review">
                        <spring:message code="review.carInfo.addReview"/>
                    </a>
                </c:when>
                <c:otherwise>
                    <a id="openReviewFormBtn"
                       href="${newReviewLoginUrl}"
                       class="btn-primary add-review-btn"
                       data-auth-required="true"
                       data-auth-required-action="${fn:escapeXml(createReviewAuthAction)}"
                       data-auth-required-intent="create-review">
                        <spring:message code="review.carInfo.addReview"/>
                    </a>
                </c:otherwise>
            </c:choose>
        </section>

        <pa:reviews-feed reviews="${reviews}" reviewThreads="${reviewThreads}" carId="${selectedCar.id}"
                         currentSort="${currentSort}"
                         currentPage="${currentPage}" totalPages="${totalPages}" totalItems="${totalItems}"
                         currentUserId="${currentUserId}"/>
    </main>

    <pa:auth-required-modal/>
    <c:choose>
        <c:when test="${not empty param.reviewCreated}">
            <pa:toast messageCode="review.create.toast.success"/>
        </c:when>
        <c:otherwise>
            <pa:toast/>
        </c:otherwise>
    </c:choose>
    <sec:authorize access="isAuthenticated()">
        <pa:confirmation-modal id="deleteReviewConfirmModal"
                               titleCode="review.delete.title"
                               bodyCode="review.delete.body"
                               confirmCode="common.action.delete"
                               confirmCssClass="btn-primary"/>
    </sec:authorize>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:review-hide-modal/>
        <pa:car-delete-modal/>
    </sec:authorize>

    <pa:script src="/js/shared/action-menu.js"/>
    <pa:script src="/js/cars/car-image-carousel.js"/>
    <pa:script src="/js/reviews/review-anchor-highlight.js"/>
    <pa:script src="/js/reviews/reply-validation.js"/>
    <pa:script src="/js/reviews/review-tag-chips.js" defer="true"/>
    <pa:script src="/js/shared/modal-utils.js"/>
    <pa:script src="/js/auth/auth-required-modal.js"/>
    <pa:script src="/js/shared/toast.js"/>
    <sec:authorize access="isAuthenticated()">
        <pa:script src="/js/shared/confirmation-modal.js"/>
        <pa:script src="/js/reviews/reply-edit.js"/>
    </sec:authorize>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:script src="/js/cars/car-admin.js"/>
        <pa:script src="/js/reviews/review-moderation.js"/>
    </sec:authorize>
    <pa:script src="/js/shared/form-submit-lock.js"/>
    <pa:footer/>
</body>
</html>
