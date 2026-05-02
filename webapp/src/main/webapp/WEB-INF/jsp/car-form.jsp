<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<c:set var="resolvedCarFormMode" value="${empty carFormMode ? 'create' : carFormMode}"/>
<c:set var="adminCarFormMode" value="${resolvedCarFormMode ne 'create'}"/>
<c:set var="catalogRequestLinksEnabled" value="${empty showCatalogRequestLinks ? not adminCarFormMode : showCatalogRequestLinks}"/>
<c:set var="resolvedFormKicker" value="${empty formKicker ? 'Nuevo vehículo' : formKicker}"/>
<c:set var="resolvedFormTitle" value="${empty formTitle ? 'Agregá un auto' : formTitle}"/>
<c:set var="resolvedFormSubtitle" value="${empty formSubtitle ? 'Completá los datos del auto. La solicitud quedará asociada a tu cuenta.' : formSubtitle}"/>
<c:set var="resolvedSubmitLabel" value="${empty submitLabel ? 'Confirmar auto' : submitLabel}"/>
<c:set var="resolvedRejectLabel" value="${empty rejectLabel ? 'Rechazar' : rejectLabel}"/>
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
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${resolvedFormTitle}"/> | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=2'/>">
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
                                <label for="modalCarBrand">Marca</label>
                                <form:select id="modalCarBrand" path="brand" required="required">
                                    <form:option value="" label="Seleccioná una marca"/>
                                    <c:forEach items="${brands}" var="brand">
                                        <form:option value="${brand.name}" label="${brand.name}"/>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="brand" cssClass="form-error" element="span"/>
                                <c:if test="${catalogRequestLinksEnabled}">
                                    <button type="button" class="catalog-request-link"
                                            data-open-catalog-request="brand">
                                        No encuentro la marca
                                    </button>
                                </c:if>
                            </div>

                            <div class="review-modal-field">
                                <label for="modalCarBodyType">Tipo de carrocería</label>
                                <form:select id="modalCarBodyType" path="bodyType" required="required">
                                    <form:option value="" label="Seleccioná un tipo"/>
                                    <c:forEach items="${bodyTypes}" var="bodyType">
                                        <form:option value="${bodyType.name}" label="${bodyType.name}"/>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="bodyType" cssClass="form-error" element="span"/>
                                <c:if test="${catalogRequestLinksEnabled}">
                                    <button type="button" class="catalog-request-link"
                                            data-open-catalog-request="body-type">
                                        No encuentro la carrocería
                                    </button>
                                </c:if>
                            </div>
                        </div>

                        <div class="review-modal-field review-modal-field-wide">
                            <label for="modalCarModel">Modelo</label>
                            <form:input id="modalCarModel" path="model" type="text"
                                        maxlength="120" required="required"
                                        placeholder="Ej: 911 Carrera T"/>
                            <form:errors path="model" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field">
                            <label for="modalCarYear">Año</label>
                            <form:input id="modalCarYear" path="year" type="number"
                                        min="1950" max="2026"
                                        placeholder="Ej: 2024"/>
                            <form:errors path="year" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field review-modal-field-wide">
                            <label for="modalCarDescription">Descripción</label>
                            <form:textarea id="modalCarDescription" path="description" rows="4" maxlength="1500"
                                           required="required"
                                           placeholder="Describe el auto, su propuesta y cualquier detalle relevante."/>
                            <form:errors path="description" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field review-modal-field-wide">
                            <span class="review-modal-section-label">Especificaciones técnicas</span>
                        </div>

                        <div class="car-modal-spec-panel">
                            <div class="car-modal-spec-grid">
                                <div class="review-modal-field">
                                    <label for="modalCarHorsepower">Potencia (HP)</label>
                                    <form:input id="modalCarHorsepower" path="horsepower" type="number"
                                                min="1" max="2000" required="required"
                                                placeholder="Ej: 150"/>
                                    <form:errors path="horsepower" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarAirbagCount">Airbags</label>
                                    <form:input id="modalCarAirbagCount" path="airbagCount" type="number"
                                                min="0" max="30" required="required"
                                                placeholder="Ej: 6"/>
                                    <form:errors path="airbagCount" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarFuelConsumption">Consumo (L/100km)</label>
                                    <form:input id="modalCarFuelConsumption" path="fuelConsumption" type="number"
                                                step="0.1" min="0" max="99.9" required="required"
                                                placeholder="Ej: 6.8"/>
                                    <form:errors path="fuelConsumption" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarMaxSpeed">Vel. máxima (km/h)</label>
                                    <form:input id="modalCarMaxSpeed" path="maxSpeedKmh" type="number"
                                                min="1" max="600" required="required"
                                                placeholder="Ej: 190"/>
                                    <form:errors path="maxSpeedKmh" cssClass="form-error" element="span"/>
                                </div>

                                <div class="review-modal-field">
                                    <label for="modalCarPrice">Precio (USD)</label>
                                    <form:input id="modalCarPrice" path="priceUsd" type="number"
                                                step="1" min="1" max="5000000"
                                                placeholder="Ej: 35000"/>
                                    <form:errors path="priceUsd" cssClass="form-error" element="span"/>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="car-modal-column car-modal-column-media">
                        <div class="review-modal-field">
                            <label>Motorización</label>
                            <div class="segmented-control segmented-control-radio-group">
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="fuelType" value="combustion" required="required" <c:if test="${empty carForm.fuelType or carForm.fuelType eq 'combustion'}">checked="checked"</c:if>/>
                                    <span>Combustión</span>
                                </label>
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="fuelType" value="hybrid" <c:if test="${carForm.fuelType eq 'hybrid'}">checked="checked"</c:if>/>
                                    <span>Híbrido</span>
                                </label>
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="fuelType" value="electric" <c:if test="${carForm.fuelType eq 'electric'}">checked="checked"</c:if>/>
                                    <span>Eléctrico</span>
                                </label>
                            </div>
                            <form:errors path="fuelType" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field">
                            <label>Transmisión</label>
                            <div class="segmented-control segmented-control-radio-group">
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="transmission" value="manual" required="required" <c:if test="${empty carForm.transmission or carForm.transmission eq 'manual'}">checked="checked"</c:if>/>
                                    <span>Manual</span>
                                </label>
                                <label class="segmented-control-radio-option">
                                    <input type="radio" name="transmission" value="automatic" <c:if test="${carForm.transmission eq 'automatic'}">checked="checked"</c:if>/>
                                    <span>Automática</span>
                                </label>
                            </div>
                            <form:errors path="transmission" cssClass="form-error" element="span"/>
                        </div>

                        <div class="review-modal-field review-modal-field-wide car-image-field">
                            <span class="car-image-label">Imágenes</span>
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
                                        <button id="modalCarImagePrev" class="car-image-upload-preview-nav car-image-upload-preview-prev" type="button" aria-label="Imagen anterior">
                                            <pa:icon name="chevron-left" size="14"/>
                                        </button>
                                        <img id="modalCarImagePreviewImg" alt="">
                                        <button id="modalCarImageRemove" class="car-image-upload-preview-remove" type="button" aria-label="Quitar imagen" hidden>
                                            <pa:icon name="close" size="14"/>
                                        </button>
                                        <button id="modalCarImageNext" class="car-image-upload-preview-nav car-image-upload-preview-next" type="button" aria-label="Imagen siguiente">
                                            <pa:icon name="chevron-right" size="14"/>
                                        </button>
                                        <span id="modalCarImageCounter" class="car-image-upload-preview-counter">1 / 1</span>
                                    </span>
                                    <span class="car-image-upload-copy">
                                        <strong id="modalCarFileTitle">
                                            <c:choose>
                                                <c:when test="${resolvedCarFormMode eq 'review-request'}">Imágenes enviadas por el usuario</c:when>
                                                <c:when test="${resolvedCarFormMode eq 'edit-car'}">Imágenes actuales del auto</c:when>
                                                <c:otherwise>Arrastrá o elegí imágenes del auto</c:otherwise>
                                            </c:choose>
                                        </strong>
                                        <span id="modalCarFileHelp">
                                            <c:choose>
                                                <c:when test="${adminCarFormMode}">Conservá, quitá o cargá imágenes para la galería.</c:when>
                                                <c:otherwise>JPEG, PNG o WEBP. Máximo 5 imágenes, 10 MB cada una.</c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span id="modalCarFileStatus" class="car-image-upload-status">Ninguna imagen seleccionada</span>
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
                        <a href="${resolvedCancelUrl}" class="btn-secondary">Cancelar</a>
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
</body>
</html>
