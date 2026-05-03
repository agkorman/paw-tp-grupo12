<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="reviewFormTitleText" code="${editMode ? 'review.form.title.edit' : 'review.form.title.new'}"/>
<spring:message var="appNameTitleText" code="app.name"/>
<c:set var="reviewFormPageTitle" value="${reviewFormTitleText} | ${appNameTitleText}"/>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${reviewFormPageTitle}" styles="/css/reviews.css|/css/review-form-controls.css|/css/review-tags.css|/css/form-pages.css"/>
<body>
    <pa:nav activePage="reviews"/>
    <c:url var="reviewCancelUrl" value="/reviews">
        <c:param name="carId" value="${selectedCar.id}"/>
    </c:url>
    <c:url var="reviewCreateUrl" value="/reviews"/>
    <c:url var="profileUrl" value="/profile"/>
    <c:url var="reviewUpdateUrl" value="/reviews/${reviewId}"/>
    <c:set var="reviewFormAction" value="${editMode ? reviewUpdateUrl : reviewCreateUrl}"/>
    <c:set var="reviewFormCancelUrl" value="${editMode ? profileUrl : reviewCancelUrl}"/>
    <spring:message var="reviewTitlePlaceholder" code="review.form.placeholder.title"/>
    <spring:message var="reviewBodyPlaceholder" code="review.form.placeholder.body"/>
    <spring:message var="reviewYearPlaceholder" code="review.form.placeholder.year"/>
    <spring:message var="reviewMileagePlaceholder" code="review.form.placeholder.mileage"/>

    <main class="form-page">
        <section id="createReviewFormPage" class="form-page-panel" data-default-car-id="${selectedCar.id}" aria-labelledby="createReviewTitle">
            <div class="review-modal-header">
                <div>
                    <span class="review-modal-kicker" data-review-modal-kicker><spring:message code="${editMode ? 'review.form.title.edit' : 'review.form.title.new'}"/></span>
                    <h1 id="createReviewTitle" data-review-modal-title>
                        <c:choose>
                            <c:when test="${editMode}"><spring:message code="review.form.heading.edit"/> <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></c:when>
                            <c:otherwise><spring:message code="review.form.heading.new"/> <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></c:otherwise>
                        </c:choose>
                    </h1>
                </div>
            </div>

            <form:form id="createReviewForm" cssClass="review-modal-form" modelAttribute="reviewForm"
                       method="post" action="${reviewFormAction}"
                       data-create-action="${reviewCreateUrl}"
                       data-submit-lock="true"
                       novalidate="novalidate">
                <form:errors cssClass="alert alert-error" element="div"/>
                <c:if test="${not empty error}">
                    <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <input id="modalReviewId" name="reviewId" type="hidden" value="">
                <form:hidden id="modalCarId" path="carId"/>

                <p class="review-modal-subtitle" data-review-modal-subtitle>
                    <c:choose>
                        <c:when test="${editMode}"><spring:message code="review.form.subtitle.edit"/></c:when>
                        <c:otherwise><spring:message code="review.form.subtitle.new"/></c:otherwise>
                    </c:choose>
                </p>

                <div class="review-modal-grid">
                    <div class="review-modal-field review-modal-field-wide">
                        <label id="ratingLabel"><spring:message code="review.form.rating"/></label>
                        <div class="star-rating" role="slider" aria-labelledby="ratingLabel" aria-valuemin="0" aria-valuemax="5" aria-valuenow="0" tabindex="0">
                            <form:hidden id="modalRating" path="rating"/>
                            <div class="star-rating-stars">
                                <c:forEach var="i" begin="1" end="5">
                                    <div class="star-slot" data-star="${i}">
                                        <pa:star-icon size="36" gradientId="starGrad${i}" fillPercent="0"/>
                                        <spring:message var="halfStarLabel" code="review.rating.halfStars.aria" arguments="${i - 1}"/>
                                        <spring:message var="fullStarLabel" code="review.rating.fullStars.aria" arguments="${i}"/>
                                        <button type="button" class="star-hit star-hit-left" data-star="${i}" data-half="true" aria-label="${halfStarLabel}"></button>
                                        <button type="button" class="star-hit star-hit-right" data-star="${i}" data-half="false" aria-label="${fullStarLabel}"></button>
                                    </div>
                                </c:forEach>
                            </div>
                            <span class="star-rating-value" aria-live="polite"><spring:message code="review.form.rating.none"/></span>
                        </div>
                        <form:errors path="rating" cssClass="form-error" element="span"/>
                    </div>

                    <div class="review-modal-field review-modal-field-wide">
                        <label><spring:message code="review.form.ownership"/></label>
                        <div class="toggle-group">
                            <label class="toggle-option">
                                <form:radiobutton path="ownershipStatus" value=""/>
                                <span><spring:message code="common.value.none"/></span>
                            </label>
                            <label class="toggle-option">
                                <form:radiobutton path="ownershipStatus" value="Propietario actual"/>
                                <span><spring:message code="review.form.ownership.current"/></span>
                            </label>
                            <label class="toggle-option">
                                <form:radiobutton path="ownershipStatus" value="Ex propietario"/>
                                <span><spring:message code="review.form.ownership.previous"/></span>
                            </label>
                        </div>
                        <form:errors path="ownershipStatus" cssClass="form-error" element="span"/>
                    </div>

                    <div class="review-modal-field review-modal-field-wide">
                        <label for="modalTitle"><spring:message code="review.form.titleField"/></label>
                        <form:input id="modalTitle" path="title" type="text" maxlength="200"
                                    required="required"
                                    placeholder="${reviewTitlePlaceholder}"/>
                        <form:errors path="title" cssClass="form-error" element="span"/>
                    </div>
                    <div class="review-modal-field review-modal-field-wide">
                        <label for="modalBody"><spring:message code="review.form.body"/></label>
                        <form:textarea id="modalBody" path="body" rows="4" maxlength="2000"
                                       required="required"
                                       placeholder="${reviewBodyPlaceholder}"/>
                        <form:errors path="body" cssClass="form-error" element="span"/>
                    </div>
                    <div class="review-modal-field">
                        <label for="modalModelYear"><spring:message code="review.form.modelYear"/></label>
                        <form:input id="modalModelYear" path="modelYear" type="text" inputmode="numeric"
                                    maxlength="4" required="required" placeholder="${reviewYearPlaceholder}"/>
                        <form:errors path="modelYear" cssClass="form-error" element="span"/>
                    </div>
                    <div class="review-modal-field">
                        <label for="modalMileageKm"><spring:message code="review.form.mileage"/></label>
                        <form:input id="modalMileageKm" path="mileageKm" type="text" inputmode="numeric"
                                    maxlength="7" required="required" placeholder="${reviewMileagePlaceholder}"/>
                        <form:errors path="mileageKm" cssClass="form-error" element="span"/>
                    </div>

                    <div class="review-modal-field review-modal-field-wide">
                        <label><spring:message code="review.form.recommend"/></label>
                        <div class="toggle-group">
                            <label class="toggle-option">
                                <c:choose>
                                    <c:when test="${reviewForm.wouldRecommend eq null}">
                                        <form:radiobutton path="wouldRecommend" value="" checked="checked"/>
                                    </c:when>
                                    <c:otherwise>
                                        <form:radiobutton path="wouldRecommend" value=""/>
                                    </c:otherwise>
                                </c:choose>
                                <span><spring:message code="common.value.none"/></span>
                            </label>
                            <label class="toggle-option toggle-option--yes">
                                <form:radiobutton path="wouldRecommend" value="true"/>
                                <span><spring:message code="common.boolean.yes"/></span>
                            </label>
                            <label class="toggle-option toggle-option--no">
                                <form:radiobutton path="wouldRecommend" value="false"/>
                                <span><spring:message code="common.boolean.no"/></span>
                            </label>
                        </div>
                        <form:errors path="wouldRecommend" cssClass="form-error" element="span"/>
                    </div>

                    <div class="review-modal-field review-modal-field-wide">
                        <pa:review-tag-chips mode="edit"
                                             tagsBySentiment="${reviewTagsBySentiment}"
                                             selectedTagIds="${reviewForm.tagIds}"/>
                        <form:errors path="tagIds" cssClass="form-error" element="span"/>
                    </div>
                </div>

                <div class="review-modal-actions">
                    <a id="reviewModalCancelButton" href="${reviewFormCancelUrl}" class="btn-secondary"><spring:message code="common.action.cancel"/></a>
                    <button id="reviewModalSubmitButton" type="submit" class="btn-primary"><spring:message code="${editMode ? 'review.form.submit.edit' : 'review.form.submit.new'}"/></button>
                </div>
            </form:form>
        </section>
    </main>

    <pa:script src="/js/review-form.js"/>
    <pa:script src="/js/review-tag-chips.js" defer="true"/>
    <pa:script src="/js/form-submit-lock.js"/>
    <pa:footer/>
</body>
</html>
