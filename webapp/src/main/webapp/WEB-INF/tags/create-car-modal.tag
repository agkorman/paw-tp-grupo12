<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.Collection" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.Collection" %>
<%@ attribute name="mode" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="createCarModal" class="review-modal" hidden <c:if test="${not empty carFormError or openCreateCarModal}">data-auto-open="true"</c:if>>
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

        <form id="createCarForm" class="car-modal-form" method="post" action="<c:url value='/cars'/>" enctype="multipart/form-data" novalidate>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <c:if test="${not empty carFormError}">
                <div class="alert alert-error" role="alert"><c:out value="${carFormError}"/></div>
            </c:if>
            <p class="car-modal-subtitle" style="padding-bottom: 1rem;">
                Completá los datos del auto. La solicitud quedará asociada a tu cuenta.
            </p>

            <div class="review-modal-grid" style="padding-bottom: 1rem;">
                <div class="review-modal-field">
                    <label for="modalCarBrand">Marca</label>
                    <select id="modalCarBrand" name="brand" required <c:if test="${adminMode}">disabled</c:if>>
                        <option value="" selected>Seleccioná una marca</option>
                        <c:forEach items="${brands}" var="brand">
                            <option value="<c:out value='${brand.name}'/>"><c:out value="${brand.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="review-modal-field">
                    <label for="modalCarBodyType">Tipo de carrocería</label>
                    <select id="modalCarBodyType" name="bodyType" required <c:if test="${adminMode}">disabled</c:if>>
                        <option value="" selected>Seleccioná un tipo</option>
                        <c:forEach items="${bodyTypes}" var="bodyType">
                            <option value="<c:out value='${bodyType.name}'/>"><c:out value="${bodyType.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarModel">Modelo</label>
                    <input id="modalCarModel" name="model" type="text" maxlength="120" placeholder="Ej: 911 Carrera T" required <c:if test="${adminMode}">readonly</c:if>>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarDescription">Descripción</label>
                    <textarea
                            id="modalCarDescription"
                            name="description"
                            rows="4"
                            maxlength="1500"
                            required
                            placeholder="Describe el auto, su propuesta y cualquier detalle relevante."
                            <c:if test="${adminMode}">readonly</c:if>></textarea>
                </div>

                <div class="review-modal-field review-modal-field-wide car-image-field">
                    <span class="car-image-label">Imagen</span>
                    <div class="car-image-upload <c:if test="${adminMode}">is-readonly</c:if>">
                        <input
                                id="modalCarFile"
                                class="car-image-upload-input"
                                name="file"
                                type="file"
                                accept="image/jpeg,image/png,image/webp"
                                <c:if test="${not adminMode}">required</c:if>
                                <c:if test="${adminMode}">disabled</c:if>
                                aria-describedby="modalCarFileHelp modalCarFileName">
                        <label class="car-image-upload-card" for="modalCarFile">
                            <span class="car-image-upload-icon" aria-hidden="true">
                                <svg width="28" height="28" viewBox="0 0 28 28" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M5.25 19.25L9.8 14.7a2 2 0 0 1 2.8 0l1.05 1.05 2.9-2.9a2 2 0 0 1 2.8 0L22.75 16.25"/>
                                    <rect x="4.5" y="5.25" width="19" height="17.5" rx="3"/>
                                    <circle cx="18.75" cy="9.75" r="1.75"/>
                                </svg>
                            </span>
                            <span class="car-image-upload-copy">
                                <strong>
                                    <c:choose>
                                        <c:when test="${adminMode}">Imagen enviada por el usuario</c:when>
                                        <c:otherwise>Arrastrá o elegí una imagen del auto</c:otherwise>
                                    </c:choose>
                                </strong>
                                <span id="modalCarFileHelp">
                                    <c:choose>
                                        <c:when test="${adminMode}">La imagen se revisa desde la tarjeta seleccionada.</c:when>
                                        <c:otherwise>JPEG, PNG o WEBP. Máximo 5 MB.</c:otherwise>
                                    </c:choose>
                                </span>
                                <span id="modalCarFileName" class="car-image-upload-filename">Ningún archivo seleccionado</span>
                            </span>
                            <span class="car-image-upload-action">
                                <c:choose>
                                    <c:when test="${adminMode}">Cargada</c:when>
                                    <c:otherwise>Buscar</c:otherwise>
                                </c:choose>
                            </span>
                        </label>
                    </div>
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
                        <button type="submit" class="btn-primary" form="acceptCarRequestForm">Confirmar auto</button>
                    </div>
                </c:if>
            </div>
        </form>
        <c:if test="${adminMode}">
            <form id="acceptCarRequestForm" method="post"></form>
            <form id="rejectCarRequestForm" method="post"></form>
        </c:if>
    </section>
</div>
