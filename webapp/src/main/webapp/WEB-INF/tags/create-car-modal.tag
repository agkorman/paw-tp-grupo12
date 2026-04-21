<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.Collection" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.Collection" %>
<%@ attribute name="mode" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="adminMode" value="${mode eq 'admin'}"/>
<c:url var="carCreateUrl" value="/cars"/>
<c:url var="adminBaseUrl" value="/admin"/>

<div id="createCarModal"
     class="review-modal"
     hidden
     data-admin-mode="${adminMode}"
     data-admin-base-url="${adminBaseUrl}"
     <c:if test="${openCarModal or openCreateCarModal or not empty carFormError}">data-auto-open="true"</c:if>>
    <div class="review-modal-overlay" data-close-car-modal></div>
    <section class="review-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="createCarModalTitle">
        <div class="review-modal-header">
            <div>
                <span id="createCarModalKicker" class="review-modal-kicker">
                    <c:choose>
                        <c:when test="${adminMode}">Solicitud pendiente</c:when>
                        <c:otherwise>Nuevo vehículo</c:otherwise>
                    </c:choose>
                </span>
                <h2 id="createCarModalTitle">
                    <c:choose>
                        <c:when test="${adminMode}">Revisar formulario</c:when>
                        <c:otherwise>Agregá un auto</c:otherwise>
                    </c:choose>
                </h2>
            </div>
            <button type="button" class="review-modal-close" data-close-car-modal aria-label="Cerrar modal">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false"><line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/></svg>
            </button>
        </div>

        <form:form id="createCarForm" cssClass="car-modal-form" modelAttribute="carForm"
                   method="post" action="${carCreateUrl}"
                   enctype="multipart/form-data"
                   data-submit-lock="true">
            <form:errors cssClass="alert alert-error" element="div"/>
            <c:if test="${not empty carFormError}">
                <div class="alert alert-error" role="alert"><c:out value="${carFormError}"/></div>
            </c:if>

            <p id="createCarModalSubtitle" class="car-modal-subtitle" style="padding-bottom: 1rem;">
                <c:choose>
                    <c:when test="${adminMode}">Revisá los datos enviados por el usuario antes de aprobar o rechazar la solicitud.</c:when>
                    <c:otherwise>Completá los datos del auto. La solicitud quedará asociada a tu cuenta.</c:otherwise>
                </c:choose>
            </p>

            <div class="review-modal-grid" style="padding-bottom: 1rem;">
                <c:if test="${adminMode}">
                    <div id="modalCarSubmitterEmailField" class="review-modal-field review-modal-field-wide" hidden>
                        <label for="modalCarSubmitterEmail">Email</label>
                        <form:input id="modalCarSubmitterEmail" path="submitterEmail" type="email"
                                    maxlength="100" disabled="true"/>
                        <form:errors path="submitterEmail" cssClass="form-error" element="span"/>
                    </div>
                </c:if>

                <div class="review-modal-field">
                    <label for="modalCarBrand">Marca</label>
                    <form:select id="modalCarBrand" path="brand" required="required">
                        <form:option value="" label="Seleccioná una marca"/>
                        <c:forEach items="${brands}" var="brand">
                            <form:option value="${brand.name}" label="${brand.name}"/>
                        </c:forEach>
                    </form:select>
                    <form:errors path="brand" cssClass="form-error" element="span"/>
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
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarModel">Modelo</label>
                    <form:input id="modalCarModel" path="model" type="text"
                                maxlength="120" required="required"
                                placeholder="Ej: 911 Carrera T"/>
                    <form:errors path="model" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarDescription">Descripción</label>
                    <form:textarea id="modalCarDescription" path="description" rows="4" maxlength="1500"
                                   required="required"
                                   placeholder="Describe el auto, su propuesta y cualquier detalle relevante."/>
                    <form:errors path="description" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide car-image-field">
                    <span class="car-image-label">Imagen</span>
                    <div class="car-image-upload <c:if test="${adminMode}">is-readonly</c:if>">
                        <c:choose>
                            <c:when test="${adminMode}">
                                <form:input id="modalCarFile" path="file" type="file"
                                            cssClass="car-image-upload-input"
                                            accept="image/jpeg,image/png,image/webp"
                                            aria-describedby="modalCarFileHelp modalCarFileStatus"/>
                            </c:when>
                            <c:otherwise>
                                <form:input id="modalCarFile" path="file" type="file"
                                            cssClass="car-image-upload-input"
                                            accept="image/jpeg,image/png,image/webp"
                                            required="required"
                                            aria-describedby="modalCarFileHelp modalCarFileStatus"/>
                            </c:otherwise>
                        </c:choose>
                        <label class="car-image-upload-card" for="modalCarFile">
                            <span class="car-image-upload-icon" aria-hidden="true">
                                <svg width="28" height="28" viewBox="0 0 28 28" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M5.25 19.25L9.8 14.7a2 2 0 0 1 2.8 0l1.05 1.05 2.9-2.9a2 2 0 0 1 2.8 0L22.75 16.25"/>
                                    <rect x="4.5" y="5.25" width="19" height="17.5" rx="3"/>
                                    <circle cx="18.75" cy="9.75" r="1.75"/>
                                </svg>
                            </span>
                            <span id="modalCarImagePreview" class="car-image-upload-preview" hidden aria-hidden="true">
                                <img id="modalCarImagePreviewImg" alt="">
                            </span>
                            <span class="car-image-upload-copy">
                                <strong id="modalCarFileTitle">
                                    <c:choose>
                                        <c:when test="${adminMode}">Imagen enviada por el usuario</c:when>
                                        <c:otherwise>Arrastrá o elegí una imagen del auto</c:otherwise>
                                    </c:choose>
                                </strong>
                                <span id="modalCarFileHelp">
                                    <c:choose>
                                        <c:when test="${adminMode}">La imagen se revisa desde la tarjeta seleccionada.</c:when>
                                        <c:otherwise>JPEG, PNG o WEBP. Máximo 10 MB.</c:otherwise>
                                    </c:choose>
                                </span>
                                <span id="modalCarFileStatus" class="car-image-upload-status">Ninguna imagen seleccionada</span>
                            </span>
                            <span id="modalCarFileAction" class="car-image-upload-action">
                                <c:choose>
                                    <c:when test="${adminMode}">Cargada</c:when>
                                    <c:otherwise>Buscar</c:otherwise>
                                </c:choose>
                            </span>
                        </label>
                    </div>
                    <form:errors path="file" cssClass="form-error" element="span"/>
                </div>
            </div>

            <div class="review-modal-actions">
                <div id="createCarCreateActions" class="review-modal-action-group" <c:if test="${adminMode}">hidden</c:if>>
                    <button type="button" class="btn-secondary" data-close-car-modal>Cancelar</button>
                    <button id="createCarSubmitButton" type="submit" class="btn-primary">Confirmar auto</button>
                </div>
                <c:if test="${adminMode}">
                    <div id="createCarReviewActions" class="review-modal-action-group">
                        <button type="submit" class="btn-secondary admin-reject-btn" form="rejectCarRequestForm">Rechazar</button>
                        <button id="createCarReviewSubmitButton" type="submit" class="btn-primary">Confirmar auto</button>
                    </div>
                    <div id="createCarEditActions" class="review-modal-action-group" hidden>
                        <button type="button" class="btn-secondary" data-close-car-modal>Cancelar</button>
                        <button id="createCarEditSubmitButton" type="submit" class="btn-primary">Guardar cambios</button>
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
