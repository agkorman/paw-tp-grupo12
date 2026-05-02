<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="resolvedCarFormMode" value="${empty carFormMode ? 'create' : carFormMode}"/>
<c:set var="adminCarFormMode" value="${resolvedCarFormMode ne 'create'}"/>
<c:set var="catalogRequestLinksEnabled" value="${empty showCatalogRequestLinks ? not adminCarFormMode : showCatalogRequestLinks}"/>
<spring:message var="defaultFormKicker" code="cars.add.kicker"/>
<spring:message var="defaultFormTitle" code="cars.add.heading"/>
<spring:message var="defaultFormSubtitle" code="cars.add.subtitle"/>
<spring:message var="defaultSubmitLabel" code="cars.form.confirm"/>
<spring:message var="defaultRejectLabel" code="common.action.reject"/>
<c:set var="resolvedFormKicker" value="${empty formKicker ? defaultFormKicker : formKicker}"/>
<c:set var="resolvedFormTitle" value="${empty formTitle ? defaultFormTitle : formTitle}"/>
<c:set var="resolvedFormSubtitle" value="${empty formSubtitle ? defaultFormSubtitle : formSubtitle}"/>
<c:set var="resolvedSubmitLabel" value="${empty submitLabel ? defaultSubmitLabel : submitLabel}"/>
<c:set var="resolvedRejectLabel" value="${empty rejectLabel ? defaultRejectLabel : rejectLabel}"/>
<c:url var="resolvedFormAction" value="${empty formAction ? '/cars' : formAction}"/>
<c:url var="resolvedCancelUrl" value="${empty cancelUrl ? '/cars' : cancelUrl}"/>
<c:if test="${not empty rejectAction}">
    <c:url var="resolvedRejectAction" value="${rejectAction}"/>
</c:if>
<c:set var="resolvedExistingImageUrls" value=""/>
<c:set var="resolvedExistingImageIds" value=""/>
<c:if test="${not empty existingImageUrls}">
    <c:forEach var="existingImageUrl" items="${existingImageUrls}">
        <c:if test="${not empty existingImageUrl}">
            <c:url var="resolvedExistingImageUrl" value="${existingImageUrl}"/>
            <c:set var="resolvedExistingImageUrls" value="${resolvedExistingImageUrls}${empty resolvedExistingImageUrls ? '' : '|'}${resolvedExistingImageUrl}"/>
        </c:if>
    </c:forEach>
</c:if>
<c:if test="${not empty existingImageIds}">
    <c:forEach var="existingImageId" items="${existingImageIds}">
        <c:set var="resolvedExistingImageIds" value="${resolvedExistingImageIds}${empty resolvedExistingImageIds ? '' : '|'}${existingImageId}"/>
    </c:forEach>
</c:if>
<spring:message var="brandSelectLabel" code="cars.form.brand.select"/>
<spring:message var="bodyTypeSelectLabel" code="cars.form.bodyType.select"/>
<spring:message var="carModelPlaceholder" code="cars.form.placeholder.model"/>
<spring:message var="carYearPlaceholder" code="cars.form.placeholder.year"/>
<spring:message var="carDescriptionPlaceholder" code="cars.form.placeholder.description"/>
<spring:message var="carHorsepowerPlaceholder" code="cars.form.placeholder.horsepower"/>
<spring:message var="carAirbagsPlaceholder" code="cars.form.placeholder.airbags"/>
<spring:message var="carConsumptionPlaceholder" code="cars.form.placeholder.consumption"/>
<spring:message var="carSpeedPlaceholder" code="cars.form.placeholder.speed"/>
<spring:message var="carPricePlaceholder" code="cars.form.placeholder.price"/>
<spring:message var="previousImageLabel" code="cars.image.previous"/>
<spring:message var="nextImageLabel" code="cars.image.next"/>
<spring:message var="removeImageLabel" code="cars.form.image.remove"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${resolvedFormTitle}"/> | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css?v=5'/>">
    <link rel="stylesheet" href="<c:url value='/css/form-pages.css'/>">
</head>
<body>
    <pa:nav activePage="reviews"/>

    <main class="form-page">
        <section id="createCarFormPage"
                 class="form-page-panel car-form-page"
                 data-admin-mode="${adminCarFormMode}"
                 data-car-form-mode="${resolvedCarFormMode}"
                 data-existing-image-urls="${fn:escapeXml(resolvedExistingImageUrls)}"
                 data-existing-image-ids="${fn:escapeXml(resolvedExistingImageIds)}"
                 data-existing-image-status="${fn:escapeXml(existingImageStatus)}"
                 aria-labelledby="carFormTitle">
            <div class="review-modal-header">
                <div>
                    <span id="carFormKicker" class="review-modal-kicker"><c:out value="${resolvedFormKicker}"/></span>
                    <h1 id="carFormTitle"><c:out value="${resolvedFormTitle}"/></h1>
                </div>
            </div>

            <form:form id="createCarForm" cssClass="car-modal-form" modelAttribute="carForm"
                       method="post" action="${resolvedFormAction}"
                       enctype="multipart/form-data"
                       data-submit-lock="true"
                       novalidate="novalidate">
                <form:errors cssClass="alert alert-error" element="div"/>
                <c:if test="${not empty carFormError}">
                    <div class="alert alert-error" role="alert"><c:out value="${carFormError}"/></div>
                </c:if>

                <p id="carFormSubtitle" class="car-modal-subtitle"><c:out value="${resolvedFormSubtitle}"/></p>

                <div class="review-modal-grid car-modal-layout">
                    <div class="car-modal-column car-modal-column-details">
                        <div class="car-modal-inline-fields">
                            <div class="review-modal-field">
                                <label for="modalCarBrand"><spring:message code="cars.form.brand"/></label>
                                <form:select id="modalCarBrand" path="brand" required="required">
                                    <form:option value="" label="${brandSelectLabel}"/>
                                    <c:forEach items="${brands}" var="brand">
                                        <form:option value="${brand.name}" label="${brand.name}"/>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="brand" cssClass="form-error" element="span"/>
                                <c:if test="${catalogRequestLinksEnabled}">
                                    <button type="button" class="catalog-request-link"
                                            data-open-catalog-request="brand">
                                        <spring:message code="cars.form.brand.request"/>
                                    </button>
                                </c:if>
                            </div>

                            <div class="review-modal-field">
                                <label for="modalCarBodyType"><spring:message code="cars.form.bodyType"/></label>
                                <form:select id="modalCarBodyType" path="bodyType" required="required">
                                    <form:option value="" label="${bodyTypeSelectLabel}"/>
                                    <c:forEach items="${bodyTypes}" var="bodyType">
                                        <form:option value="${bodyType.name}" label="${bodyType.name}"/>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="bodyType" cssClass="form-error" element="span"/>
                                <c:if test="${catalogRequestLinksEnabled}">
                                    <button type="button" class="catalog-request-link"
                                            data-open-catalog-request="body-type">
                                        <spring:message code="cars.form.bodyType.request"/>
                                    </button>
                                </c:if>
                            </div>
                        </div>

                        <div class="review-modal-field review-modal-field-wide">
                            <label for="modalCarModel"><spring:message code="cars.form.model"/></label>
                            <form:input id="modalCarModel" path="model" type="text"
                                        maxlength="120" required="required"
                                        placeholder="${carModelPlaceholder}"/>
                            <form:errors path="model" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field">
                            <label for="modalCarYear"><spring:message code="cars.form.year"/></label>
                            <form:input id="modalCarYear" path="year" type="number"
                                        min="1886" max="2100"
                                        placeholder="${carYearPlaceholder}"/>
                            <form:errors path="year" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field review-modal-field-wide">
                            <label for="modalCarDescription"><spring:message code="cars.form.description"/></label>
                            <form:textarea id="modalCarDescription" path="description" rows="4" maxlength="1500"
                                           required="required"
                                           placeholder="${carDescriptionPlaceholder}"/>
                            <form:errors path="description" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field review-modal-field-wide">
                            <span class="review-modal-section-label"><spring:message code="cars.form.specs"/></span>
                        </div>

                        <div class="car-modal-spec-panel">
                            <div class="car-modal-spec-grid">
                                <div class="review-modal-field">
                                    <label for="modalCarHorsepower"><spring:message code="cars.form.horsepower"/></label>
                                    <form:input id="modalCarHorsepower" path="horsepower" type="number"
                                                min="1" max="2000" required="required"
                                                placeholder="${carHorsepowerPlaceholder}"/>
                                    <form:errors path="horsepower" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarAirbagCount"><spring:message code="cars.form.airbags"/></label>
                                    <form:input id="modalCarAirbagCount" path="airbagCount" type="number"
                                                min="0" max="30" required="required"
                                                placeholder="${carAirbagsPlaceholder}"/>
                                    <form:errors path="airbagCount" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarFuelConsumption"><spring:message code="cars.form.fuelConsumption"/></label>
                                    <form:input id="modalCarFuelConsumption" path="fuelConsumption" type="number"
                                                step="0.1" min="0" max="99.9" required="required"
                                                placeholder="${carConsumptionPlaceholder}"/>
                                    <form:errors path="fuelConsumption" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarMaxSpeed"><spring:message code="cars.form.maxSpeed"/></label>
                                    <form:input id="modalCarMaxSpeed" path="maxSpeedKmh" type="number"
                                                min="1" max="600" required="required"
                                                placeholder="${carSpeedPlaceholder}"/>
                                    <form:errors path="maxSpeedKmh" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarPrice"><spring:message code="cars.form.price"/></label>
                                    <form:input id="modalCarPrice" path="priceUsd" type="number"
                                                step="1" min="1" max="5000000"
                                                placeholder="${carPricePlaceholder}"/>
                                    <form:errors path="priceUsd" cssClass="form-error" element="span"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="car-modal-column car-modal-column-media">
                        <div class="review-modal-field">
                            <label><spring:message code="cars.form.fuelType"/></label>
                            <div class="segmented-control segmented-control-radio-group">
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="fuelType" value="combustion" required="required" <c:if test="${empty carForm.fuelType or carForm.fuelType eq 'combustion'}">checked="checked"</c:if>/>
                                    <span><spring:message code="domain.fuel.combustion"/></span>
                                </label>
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="fuelType" value="hybrid" <c:if test="${carForm.fuelType eq 'hybrid'}">checked="checked"</c:if>/>
                                    <span><spring:message code="domain.fuel.hybrid"/></span>
                                </label>
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="fuelType" value="electric" <c:if test="${carForm.fuelType eq 'electric'}">checked="checked"</c:if>/>
                                    <span><spring:message code="domain.fuel.electric"/></span>
                                </label>
                            </div>
                            <form:errors path="fuelType" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field">
                            <label><spring:message code="cars.form.transmission"/></label>
                            <div class="segmented-control segmented-control-radio-group">
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="transmission" value="manual" required="required" <c:if test="${empty carForm.transmission or carForm.transmission eq 'manual'}">checked="checked"</c:if>/>
                                    <span><spring:message code="domain.transmission.manual"/></span>
                                </label>
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="transmission" value="automatic" <c:if test="${carForm.transmission eq 'automatic'}">checked="checked"</c:if>/>
                                    <span><spring:message code="domain.transmission.automatic"/></span>
                                </label>
                            </div>
                            <form:errors path="transmission" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field review-modal-field-wide car-image-field">
                            <span class="car-image-label"><spring:message code="cars.form.images"/></span>
                            <div class="car-image-upload">
                                <c:choose>
                                    <c:when test="${adminCarFormMode}">
                                        <span id="modalCarRetainedImageInputs" hidden>
                                            <c:forEach var="existingImageId" items="${existingImageIds}">
                                                <input type="hidden" name="retainedImageIds" value="${existingImageId}">
                                            </c:forEach>
                                        </span>
                                        <form:input id="modalCarFile" path="files" type="file"
                                                    cssClass="car-image-upload-input"
                                                    accept="image/jpeg,image/png,image/webp"
                                                    multiple="multiple"
                                                    aria-describedby="modalCarFileHelp modalCarFileStatus"/>
                                    </c:when>
                                    <c:otherwise>
                                        <form:input id="modalCarFile" path="files" type="file"
                                                    cssClass="car-image-upload-input"
                                                    accept="image/jpeg,image/png,image/webp"
                                                    multiple="multiple"
                                                    required="required"
                                                    aria-describedby="modalCarFileHelp modalCarFileStatus"/>
                                    </c:otherwise>
                                </c:choose>
                                <label class="car-image-upload-card" for="modalCarFile">
                                    <span class="car-image-upload-icon" aria-hidden="true">
                                        <pa:icon name="image-upload" size="28"/>
                                    </span>
                                    <span id="modalCarImagePreview" class="car-image-upload-preview" hidden aria-hidden="true">
                                        <button id="modalCarImagePrev" class="car-image-upload-preview-nav car-image-upload-preview-prev" type="button" aria-label="${previousImageLabel}">
                                            <pa:icon name="chevron-left" size="14"/>
                                        </button>
                                        <img id="modalCarImagePreviewImg" alt="">
                                        <button id="modalCarImageRemove" class="car-image-upload-preview-remove" type="button" aria-label="${removeImageLabel}" hidden>
                                            <pa:icon name="close" size="14"/>
                                        </button>
                                        <button id="modalCarImageNext" class="car-image-upload-preview-nav car-image-upload-preview-next" type="button" aria-label="${nextImageLabel}">
                                            <pa:icon name="chevron-right" size="14"/>
                                        </button>
                                        <span id="modalCarImageCounter" class="car-image-upload-preview-counter">1 / 1</span>
                                    </span>
                                    <span class="car-image-upload-copy">
                                        <strong id="modalCarFileTitle">
                                            <c:choose>
                                                <c:when test="${resolvedCarFormMode eq 'review-request'}"><spring:message code="cars.form.image.reviewReadonly"/></c:when>
                                                <c:when test="${resolvedCarFormMode eq 'edit-car'}"><spring:message code="cars.form.image.currentPlural"/></c:when>
                                                <c:otherwise><spring:message code="cars.form.image.uploadTitle"/></c:otherwise>
                                            </c:choose>
                                        </strong>
                                        <span id="modalCarFileHelp">
                                            <c:choose>
                                                <c:when test="${adminCarFormMode}"><spring:message code="cars.form.image.galleryHelp"/></c:when>
                                                <c:otherwise><spring:message code="cars.form.image.help"/></c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span id="modalCarFileStatus" class="car-image-upload-status"><spring:message code="cars.form.image.none"/></span>
                                        <span id="modalCarImageThumbnails" class="car-image-upload-thumbnails" hidden></span>
                                    </span>
                                </label>
                            </div>
                            <form:errors path="files" cssClass="form-error" element="span"/>
                        </div>
                    </div>
                </div>

                <div class="review-modal-actions">
                    <div id="createCarCreateActions" class="review-modal-action-group">
                        <a href="${resolvedCancelUrl}" class="btn-secondary"><spring:message code="common.action.cancel"/></a>
                        <c:if test="${not empty resolvedRejectAction}">
                            <button type="submit" class="btn-secondary admin-reject-btn" form="rejectCarRequestForm">
                                <c:out value="${resolvedRejectLabel}"/>
                            </button>
                        </c:if>
                        <button id="createCarSubmitButton" type="submit" class="btn-primary">
                            <c:out value="${resolvedSubmitLabel}"/>
                        </button>
                    </div>
                </div>
            </form:form>
            <c:if test="${not empty resolvedRejectAction}">
                <form id="rejectCarRequestForm" method="post" action="${resolvedRejectAction}">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                </form>
            </c:if>
        </section>
    </main>

    <c:if test="${catalogRequestLinksEnabled}">
        <pa:request-brand-modal/>
        <pa:request-body-type-modal/>
    </c:if>

    <script src="<c:url value='/js/car-form.js?v=3'/>"></script>
    <c:if test="${catalogRequestLinksEnabled}">
        <script src="<c:url value='/js/catalog-request-modals.js'/>"></script>
    </c:if>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
    <pa:footer/>
</body>
</html>
