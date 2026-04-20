<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.Collection" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.Collection" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div id="createCarModal" class="review-modal" hidden <c:if test="${openCarModal}">data-auto-open="true"</c:if>>
    <div class="review-modal-overlay" data-close-car-modal></div>
    <section class="review-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="createCarModalTitle">
        <div class="review-modal-header">
            <div>
                <span class="review-modal-kicker">Nuevo vehículo</span>
                <h2 id="createCarModalTitle">Agregá un auto</h2>
            </div>
            <button type="button" class="review-modal-close" data-close-car-modal aria-label="Cerrar modal">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false"><line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/></svg>
            </button>
        </div>

        <form:form id="createCarForm" cssClass="car-modal-form" modelAttribute="carForm"
                   method="post" action="${pageContext.request.contextPath}/cars"
                   enctype="multipart/form-data"
                   data-submit-lock="true">
            <form:errors cssClass="alert alert-error" element="div" />

            <p class="car-modal-subtitle" style="padding-bottom: 1rem;">
                Completá los datos del auto. Tu email se guardará para revisar esta solicitud más adelante.
            </p>

            <div class="review-modal-grid" style="padding-bottom: 1rem;">
                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarSubmitterEmail">Email</label>
                    <form:input id="modalCarSubmitterEmail" path="submitterEmail" type="email"
                                maxlength="100" placeholder="tu@email.com"/>
                    <form:errors path="submitterEmail" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field">
                    <label for="modalCarBrand">Marca</label>
                    <form:select id="modalCarBrand" path="brand">
                        <form:option value="" label="Seleccioná una marca"/>
                        <c:forEach items="${brands}" var="brand">
                            <form:option value="${brand.name}">${brand.name}</form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="brand" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field">
                    <label for="modalCarBodyType">Tipo de carrocería</label>
                    <form:select id="modalCarBodyType" path="bodyType">
                        <form:option value="" label="Seleccioná un tipo"/>
                        <c:forEach items="${bodyTypes}" var="bodyType">
                            <form:option value="${bodyType.name}">${bodyType.name}</form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="bodyType" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarModel">Modelo</label>
                    <form:input id="modalCarModel" path="model" type="text"
                                maxlength="120" placeholder="Ej: 911 Carrera T"/>
                    <form:errors path="model" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarDescription">Descripción</label>
                    <form:textarea id="modalCarDescription" path="description" rows="4" maxlength="1500"
                                   placeholder="Describe el auto, su propuesta y cualquier detalle relevante."/>
                    <form:errors path="description" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide car-image-field">
                    <span class="car-image-label">Imagen</span>
                    <div class="car-image-upload">
                        <form:input id="modalCarFile" path="file" type="file"
                                    cssClass="car-image-upload-input"
                                    accept="image/jpeg,image/png,image/webp"
                                    aria-describedby="modalCarFileHelp modalCarFileName"/>
                        <label class="car-image-upload-card" for="modalCarFile">
                            <span class="car-image-upload-icon" aria-hidden="true">
                                <svg width="28" height="28" viewBox="0 0 28 28" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M5.25 19.25L9.8 14.7a2 2 0 0 1 2.8 0l1.05 1.05 2.9-2.9a2 2 0 0 1 2.8 0L22.75 16.25"/>
                                    <rect x="4.5" y="5.25" width="19" height="17.5" rx="3"/>
                                    <circle cx="18.75" cy="9.75" r="1.75"/>
                                </svg>
                            </span>
                            <span class="car-image-upload-copy">
                                <strong>Arrastrá o elegí una imagen del auto</strong>
                                <span id="modalCarFileHelp">JPEG, PNG o WEBP. Máximo 10 MB.</span>
                                <span id="modalCarFileName" class="car-image-upload-filename">Ningún archivo seleccionado</span>
                            </span>
                            <span class="car-image-upload-action">Buscar</span>
                        </label>
                    </div>
                    <form:errors path="file" cssClass="form-error" element="span"/>
                </div>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-car-modal>Cancelar</button>
                <button id="createCarSubmitButton" type="submit" class="btn-primary">Confirmar auto</button>
            </div>
        </form:form>
    </section>
</div>
