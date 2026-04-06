<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.Collection" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.Collection" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="createCarModal" class="review-modal" hidden>
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

        <form id="createCarForm" class="car-modal-form" method="post" action="<c:url value='/cars'/>" enctype="multipart/form-data" novalidate>
            <c:if test="${not empty carFormError}">
                <div class="alert alert-error" role="alert"><c:out value="${carFormError}"/></div>
            </c:if>
            <p class="car-modal-subtitle">
                Completa los datos del auto y, si querés, sumá una imagen.
            </p>

            <div class="review-modal-grid">
                <div class="review-modal-field">
                    <label for="modalCarBrand">Marca</label>
                    <select id="modalCarBrand" name="brand" required>
                        <option value="">Seleccioná una marca</option>
                        <c:forEach items="${brands}" var="brand">
                            <option value="<c:out value='${brand.name}'/>"><c:out value="${brand.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="review-modal-field">
                    <label for="modalCarBodyType">Tipo de carrocería</label>
                    <select id="modalCarBodyType" name="bodyType" required>
                        <option value="">Seleccioná un tipo</option>
                        <c:forEach items="${bodyTypes}" var="bodyType">
                            <option value="<c:out value='${bodyType.name}'/>"><c:out value="${bodyType.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarModel">Modelo</label>
                    <input id="modalCarModel" name="model" type="text" maxlength="120" placeholder="Ej: 911 Carrera T" required>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalCarDescription">Descripción</label>
                    <textarea
                            id="modalCarDescription"
                            name="description"
                            rows="4"
                            maxlength="1500"
                            placeholder="Describe el auto, su propuesta y cualquier detalle relevante."></textarea>
                </div>

                <div class="car-modal-field car-modal-field-wide">
                    <label for="modalCarFile">Imagen</label>
                    <input
                            id="modalCarFile"
                            name="file"
                            type="file"
                            accept="image/jpeg,image/png,image/webp">
                </div>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-car-modal>Cancelar</button>
                <button id="createCarSubmitButton" type="submit" class="btn-primary">Confirmar auto</button>
            </div>
        </form>
    </section>
</div>
