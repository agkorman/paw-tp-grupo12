<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="reviewFormTitleText" code="${editMode ? 'review.form.title.edit' : 'review.form.title.new'}"/>
<spring:message var="appNameTitleText" code="app.name"/>
<c:set var="reviewFormPageTitle" value="${reviewFormTitleText} | ${appNameTitleText}"/>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${reviewFormPageTitle}" styles="/css/reviews.css|/css/rating-controls.css|/css/review-tags.css|/css/car-image-upload.css|/css/form-pages.css"/>
<body>
    <pa:nav activePage="reviews"/>
    <c:url var="reviewCancelUrl" value="/reviews/car/${selectedCar.id}"/>
    <c:url var="reviewCreateUrl" value="/reviews"/>
    <c:url var="profileUrl" value="/profile"/>
    <c:url var="reviewUpdateUrl" value="/reviews/${reviewId}"/>
    <c:set var="reviewFormAction" value="${editMode ? reviewUpdateUrl : reviewCreateUrl}"/>
    <c:set var="reviewFormCancelUrl" value="${editMode ? (not empty editRedirect ? editRedirect : profileUrl) : reviewCancelUrl}"/>
    <spring:message var="reviewTitlePlaceholder" code="review.form.placeholder.title"/>
    <spring:message var="reviewBodyPlaceholder" code="review.form.placeholder.body"/>
    <spring:message var="reviewMileagePlaceholder" code="review.form.placeholder.mileage"/>
    <spring:message var="jsMsgRequiredGeneric" code="js.form.required.generic"/>
    <spring:message var="jsMsgRequiredTitle" code="js.review.required.title"/>
    <spring:message var="jsMsgRequiredBody" code="js.review.required.body"/>
    <spring:message var="jsMsgRequiredMileage" code="js.review.required.mileage"/>
    <spring:message var="jsMsgRequiredRating" code="js.review.required.rating"/>
    <spring:message var="jsMsgMileageNumeric" code="js.review.mileage.numeric"/>
    <spring:message var="jsMsgMileageRange" code="js.review.mileage.range"/>
    <spring:message var="jsMsgRatingNone" code="js.review.rating.none"/>
    <spring:message var="jsMsgRatingBad" code="js.review.rating.bad"/>
    <spring:message var="jsMsgRatingFair" code="js.review.rating.fair"/>
    <spring:message var="jsMsgRatingGood" code="js.review.rating.good"/>
    <spring:message var="jsMsgRatingVeryGood" code="js.review.rating.veryGood"/>
    <spring:message var="jsMsgRatingExcellent" code="js.review.rating.excellent"/>

    <main class="form-page">
        <section id="createReviewFormPage" class="form-page-panel" data-default-car-id="${selectedCar.id}" aria-labelledby="createReviewTitle">
            <div class="modal-header">
                <div>
                    <span class="modal-kicker" data-modal-kicker><spring:message code="${editMode ? 'review.form.title.edit' : 'review.form.title.new'}"/></span>
                    <h1 id="createReviewTitle" data-modal-title>
                        <c:choose>
                            <c:when test="${editMode}"><spring:message code="review.form.heading.edit"/> <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></c:when>
                            <c:otherwise><spring:message code="review.form.heading.new"/> <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></c:otherwise>
                        </c:choose>
                    </h1>
                </div>
            </div>

            <form:form id="createReviewForm" cssClass="modal-form" modelAttribute="reviewForm"
                       method="post" action="${reviewFormAction}"
                       enctype="multipart/form-data"
                       data-create-action="${reviewCreateUrl}"
                       data-submit-lock="true"
                       data-msg-required-generic="${fn:escapeXml(jsMsgRequiredGeneric)}"
                       data-msg-required-modal-title="${fn:escapeXml(jsMsgRequiredTitle)}"
                       data-msg-required-modal-body="${fn:escapeXml(jsMsgRequiredBody)}"
                       data-msg-required-modal-mileage-km="${fn:escapeXml(jsMsgRequiredMileage)}"
                       data-msg-required-rating="${fn:escapeXml(jsMsgRequiredRating)}"
                       data-msg-mileage-numeric="${fn:escapeXml(jsMsgMileageNumeric)}"
                       data-msg-mileage-range="${fn:escapeXml(jsMsgMileageRange)}"
                       data-rating-none="${fn:escapeXml(jsMsgRatingNone)}"
                       data-rating-bad="${fn:escapeXml(jsMsgRatingBad)}"
                       data-rating-fair="${fn:escapeXml(jsMsgRatingFair)}"
                       data-rating-good="${fn:escapeXml(jsMsgRatingGood)}"
                       data-rating-very-good="${fn:escapeXml(jsMsgRatingVeryGood)}"
                       data-rating-excellent="${fn:escapeXml(jsMsgRatingExcellent)}"
                       novalidate="novalidate">
                <form:errors cssClass="alert alert-error" element="div"/>
                <c:if test="${not empty error}">
                    <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <input id="modalReviewId" name="reviewId" type="hidden" value="">
                <form:hidden id="modalCarId" path="carId"/>
                <c:if test="${not empty editRedirect}">
                    <input type="hidden" name="redirect" value="${fn:escapeXml(editRedirect)}"/>
                </c:if>

                <p class="modal-subtitle" data-modal-subtitle>
                    <c:choose>
                        <c:when test="${editMode}"><spring:message code="review.form.subtitle.edit"/></c:when>
                        <c:otherwise><spring:message code="review.form.subtitle.new"/></c:otherwise>
                    </c:choose>
                </p>

                <div class="modal-grid">
                    <div class="modal-field modal-field-wide">
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

                    <div class="modal-field modal-field-wide">
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

                    <div class="modal-field modal-field-wide">
                        <label for="modalTitle"><spring:message code="review.form.titleField"/></label>
                        <form:input id="modalTitle" path="title" type="text" maxlength="200"
                                    required="required"
                                    placeholder="${reviewTitlePlaceholder}"/>
                        <form:errors path="title" cssClass="form-error" element="span"/>
                    </div>
                    <div class="modal-field modal-field-wide">
                        <label for="modalBody"><spring:message code="review.form.body"/></label>
                        <form:textarea id="modalBody" path="body" rows="4" maxlength="2000"
                                       required="required"
                                       placeholder="${reviewBodyPlaceholder}"/>
                        <form:errors path="body" cssClass="form-error" element="span"/>
                    </div>
                    <div class="modal-field">
                        <label for="modalMileageKm"><spring:message code="review.form.mileage"/></label>
                        <form:input id="modalMileageKm" path="mileageKm" type="text"
                                    inputmode="numeric" autocomplete="off"
                                    required="required" placeholder="${reviewMileagePlaceholder}"/>
                        <form:errors path="mileageKm" cssClass="form-error" element="span"/>
                    </div>

                    <div class="modal-field modal-field-wide">
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

                    <div class="modal-field modal-field-wide">
                        <pa:review-tag-chips mode="edit"
                                             tagsBySentiment="${reviewTagsBySentiment}"
                                             selectedTagIds="${reviewForm.tagIds}"/>
                        <form:errors path="tagIds" cssClass="form-error" element="span"/>
                    </div>

                    <c:if test="${editMode and not empty existingReviewImageIds}">
                        <div class="modal-field modal-field-wide">
                            <span class="car-image-label"><spring:message code="review.form.images.existing"/></span>
                            <div class="review-form-existing-images">
                                <c:forTokens var="imgId" items="${existingReviewImageIds}" delims=",">
                                    <label class="review-form-existing-image">
                                        <input type="checkbox" name="retainedImageIds" value="${imgId}" checked>
                                        <img src="<c:url value='/reviews/${reviewId}/images/${imgId}'/>"
                                             alt="<spring:message code='review.image.alt'/>"
                                             class="review-form-existing-image-thumb"/>
                                        <span class="review-form-existing-image-keep"><spring:message code="review.form.images.keep"/></span>
                                    </label>
                                </c:forTokens>
                            </div>
                        </div>
                    </c:if>

                    <div class="modal-field modal-field-wide">
                        <pa:image-upload
                            namePrefix="review"
                            inputName="files"
                            required="false"
                            mode="${editMode ? 'edit' : 'create'}"
                            labelKey="review.form.images"
                            titleCreateKey="review.form.image.uploadTitle"
                            titleEditKey="review.form.image.addMore"
                            helpKey="review.form.image.help"/>
                    </div>
                </div>

                <div class="modal-actions">
                    <a id="reviewModalCancelButton" href="${reviewFormCancelUrl}" class="btn-secondary"><spring:message code="common.action.cancel"/></a>
                    <button id="reviewModalSubmitButton" type="submit" class="btn-primary"><spring:message code="${editMode ? 'review.form.submit.edit' : 'review.form.submit.new'}"/></button>
                </div>
            </form:form>
        </section>
    </main>

    <pa:script src="/js/reviews/review-form.js"/>
    <pa:script src="/js/reviews/review-tag-chips.js" defer="true"/>
    <pa:script src="/js/reviews/review-image-upload.js" defer="true"/>
    <pa:script src="/js/shared/form-submit-lock.js"/>
    <pa:footer/>
</body>
</html>
