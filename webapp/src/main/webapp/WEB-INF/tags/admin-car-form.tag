<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.Collection" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.Collection" %>
<%@ attribute name="mode" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="adminMode" value="${mode eq 'admin'}"/>
<c:url var="carCreateUrl" value="/cars"/>
<c:url var="adminBaseUrl" value="/admin"/>
<spring:message var="brandSelectLabel" code="cars.form.brand.select"/>
<spring:message var="bodyTypeSelectLabel" code="cars.form.bodyType.select"/>
<spring:message var="carModelPlaceholder" code="cars.form.placeholder.model"/>
<spring:message var="carYearPlaceholder" code="cars.form.placeholder.year2026"/>
<spring:message var="carDescriptionPlaceholder" code="cars.form.placeholder.description"/>
<spring:message var="carHorsepowerPlaceholder" code="cars.form.placeholder.horsepower"/>
<spring:message var="carAirbagsPlaceholder" code="cars.form.placeholder.airbags"/>
<spring:message var="carConsumptionPlaceholder" code="cars.form.placeholder.consumption"/>
<spring:message var="carSpeedPlaceholder" code="cars.form.placeholder.speed"/>
<spring:message var="carPricePlaceholder" code="cars.form.placeholder.price25000"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="previousImageLabel" code="cars.image.previous"/>
<spring:message var="nextImageLabel" code="cars.image.next"/>

<div id="createCarModal"
     class="review-modal"
     hidden
     data-admin-mode="${adminMode}"
     data-admin-base-url="${adminBaseUrl}"
     <c:if test="${openCarModal or openCreateCarModal or not empty carFormError}">data-auto-open="true"</c:if>>
    <div class="review-modal-overlay" data-close-car-modal></div>
    <section class="review-modal-dialog car-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="createCarModalTitle">
        <div class="review-modal-header">
            <div>
                <span id="createCarModalKicker" class="review-modal-kicker">
                    <c:choose>
                        <c:when test="${adminMode}"><spring:message code="common.status.pending"/></c:when>
                        <c:otherwise><spring:message code="cars.add.kicker"/></c:otherwise>
                    </c:choose>
                </span>
                <h2 id="createCarModalTitle">
                    <c:choose>
                        <c:when test="${adminMode}"><spring:message code="cars.form.mode.review.title"/></c:when>
                        <c:otherwise><spring:message code="cars.add.heading"/></c:otherwise>
                    </c:choose>
                </h2>
            </div>
            <button type="button" class="review-modal-close" data-close-car-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </div>

        <form:form id="createCarForm" cssClass="car-modal-form" modelAttribute="carForm"
                   method="post" action="${carCreateUrl}"
                   enctype="multipart/form-data"
                   data-submit-lock="true"
                   novalidate="novalidate">
            <form:errors cssClass="alert alert-error" element="div"/>
            <c:if test="${not empty carFormError}">
                <div class="alert alert-error" role="alert"><c:out value="${carFormError}"/></div>
            </c:if>

            <p id="createCarModalSubtitle" class="car-modal-subtitle">
                <c:choose>
                    <c:when test="${adminMode}"><spring:message code="cars.form.mode.review.subtitle"/></c:when>
                    <c:otherwise><spring:message code="cars.add.subtitle"/></c:otherwise>
                </c:choose>
            </p>

            <div class="review-modal-grid car-modal-layout">
                <div class="car-modal-column car-modal-column-details">
                    <c:if test="${adminMode}">
                        <div id="modalCarSubmitterEmailField" class="review-modal-field review-modal-field-wide" hidden>
                            <label for="modalCarSubmitterEmail"><spring:message code="common.form.email"/></label>
                            <form:input id="modalCarSubmitterEmail" path="submitterEmail" type="email"
                                        maxlength="100" disabled="true"/>
                            <form:errors path="submitterEmail" cssClass="form-error" element="span"/>
                        </div>
                    </c:if>

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
                            <c:if test="${not adminMode}">
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
                            <c:if test="${not adminMode}">
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

                    <div class="review-modal-field review-modal-field-wide">
                        <label for="modalCarYear"><spring:message code="cars.form.modelYear"/></label>
                        <form:input id="modalCarYear" path="year" type="number"
                                    min="1886" max="2100"
                                    placeholder="${carYearPlaceholder}" readonly="${adminMode}"/>
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
                                            placeholder="${carHorsepowerPlaceholder}" readonly="${adminMode}"/>
                                <form:errors path="horsepower" cssClass="form-error" element="span"/>
                            </div>

                            <div class="review-modal-field">
                                <label for="modalCarAirbagCount"><spring:message code="cars.form.airbags"/></label>
                                <form:input id="modalCarAirbagCount" path="airbagCount" type="number"
                                            min="0" max="30" required="required"
                                            placeholder="${carAirbagsPlaceholder}" readonly="${adminMode}"/>
                                <form:errors path="airbagCount" cssClass="form-error" element="span"/>
                            </div>

                            <div class="review-modal-field">
                                <label for="modalCarFuelConsumption"><spring:message code="cars.form.fuelConsumption"/></label>
                                <form:input id="modalCarFuelConsumption" path="fuelConsumption" type="number"
                                            step="0.1" min="0" max="99.9" required="required"
                                            placeholder="${carConsumptionPlaceholder}" readonly="${adminMode}"/>
                                <form:errors path="fuelConsumption" cssClass="form-error" element="span"/>
                            </div>

                            <div class="review-modal-field">
                                <label for="modalCarMaxSpeed"><spring:message code="cars.form.maxSpeed"/></label>
                                <form:input id="modalCarMaxSpeed" path="maxSpeedKmh" type="number"
                                            min="1" max="600" required="required"
                                            placeholder="${carSpeedPlaceholder}" readonly="${adminMode}"/>
                                <form:errors path="maxSpeedKmh" cssClass="form-error" element="span"/>
                            </div>

                            <div class="review-modal-field">
                                <label for="modalCarPriceUsd"><spring:message code="cars.form.priceZeroKm"/></label>
                                <form:input id="modalCarPriceUsd" path="priceUsd" type="number"
                                            step="0.01" min="0.01" max="5000000"
                                            placeholder="${carPricePlaceholder}" readonly="${adminMode}"/>
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
                        <div class="car-image-upload <c:if test="${adminMode}">is-readonly</c:if>">
                            <c:choose>
                                <c:when test="${adminMode}">
                                    <form:input id="modalCarFile" path="files" type="file"
                                                cssClass="car-image-upload-input"
                                                accept="image/jpeg,image/png,image/webp"
                                                multiple="multiple"
                                                disabled="true"
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
                                    <button id="modalCarImageNext" class="car-image-upload-preview-nav car-image-upload-preview-next" type="button" aria-label="${nextImageLabel}">
                                        <pa:icon name="chevron-right" size="14"/>
                                    </button>
                                    <span id="modalCarImageCounter" class="car-image-upload-preview-counter">1 / 1</span>
                                </span>
                                <span class="car-image-upload-copy">
                                    <strong id="modalCarFileTitle">
                                        <c:choose>
                                            <c:when test="${adminMode}"><spring:message code="cars.form.image.reviewReadonly"/></c:when>
                                            <c:otherwise><spring:message code="cars.form.image.uploadTitle"/></c:otherwise>
                                        </c:choose>
                                    </strong>
                                    <span id="modalCarFileHelp">
                                        <c:choose>
                                            <c:when test="${adminMode}"><spring:message code="cars.form.image.reviewHelp"/></c:when>
                                            <c:otherwise><spring:message code="cars.form.image.help"/></c:otherwise>
                                        </c:choose>
                                    </span>
                                    <span id="modalCarFileStatus" class="car-image-upload-status"><spring:message code="cars.form.image.none"/></span>
                                    <span id="modalCarImageThumbnails" class="car-image-upload-thumbnails" hidden></span>
                                </span>
                                <span id="modalCarFileAction" class="car-image-upload-action">
                                    <c:choose>
                                        <c:when test="${adminMode}"><spring:message code="cars.form.image.loaded"/></c:when>
                                        <c:otherwise><spring:message code="cars.form.image.search"/></c:otherwise>
                                    </c:choose>
                                </span>
                            </label>
                        </div>
                        <form:errors path="files" cssClass="form-error" element="span"/>
                    </div>
                </div>
            </div>

            <div class="review-modal-actions">
                <div id="createCarCreateActions" class="review-modal-action-group" <c:if test="${adminMode}">hidden</c:if>>
                    <button type="button" class="btn-secondary" data-close-car-modal><spring:message code="common.action.cancel"/></button>
                    <button id="createCarSubmitButton" type="submit" class="btn-primary"><spring:message code="cars.form.confirm"/></button>
                </div>
                <c:if test="${adminMode}">
                    <div id="createCarReviewActions" class="review-modal-action-group">
                        <button type="submit" class="btn-secondary admin-reject-btn" form="rejectCarRequestForm"><spring:message code="common.action.reject"/></button>
                        <button id="createCarReviewSubmitButton" type="submit" class="btn-primary"><spring:message code="cars.form.confirm"/></button>
                    </div>
                    <div id="createCarEditActions" class="review-modal-action-group" hidden>
                        <button type="button" class="btn-secondary" data-close-car-modal><spring:message code="common.action.cancel"/></button>
                        <button id="createCarEditSubmitButton" type="submit" class="btn-primary"><spring:message code="cars.form.save"/></button>
                    </div>
                </c:if>
            </div>
        </form:form>
        <c:if test="${adminMode}">
            <form id="rejectCarRequestForm" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            </form>
        </c:if>
    </section>
</div>

<c:if test="${not adminMode}">
    <pa:request-brand-modal/>
    <pa:request-body-type-modal/>
</c:if>
