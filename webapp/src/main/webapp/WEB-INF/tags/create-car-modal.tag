<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.Collection" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.Collection" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="createCarModal" class="car-modal" hidden>
    <div class="car-modal-overlay" data-close-car-modal></div>
    <section class="car-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="createCarModalTitle">
        <div class="car-modal-header">
            <div>
                <h2 id="createCarModalTitle">Agregar un auto</h2>
            </div>
            <button type="button" class="car-modal-close" data-close-car-modal aria-label="Cerrar modal">x</button>
        </div>

        <form id="createCarForm" class="car-modal-form" method="post" action="<c:url value='/cars'/>" novalidate>
            <c:if test="${not empty carFormError}">
                <div class="alert alert-error" role="alert"><c:out value="${carFormError}"/></div>
            </c:if>
            <p class="car-modal-subtitle">
                Completa todos los campos para publicar el auto.
            </p>

            <div class="car-modal-grid car-modal-fields">
                <div class="car-modal-field">
                    <label for="modalCarBrand">Marca</label>
                    <select id="modalCarBrand" name="brand" required>
                        <option value="">Selecciona una marca</option>
                        <c:forEach items="${brands}" var="brand">
                            <option value="<c:out value='${brand.name}'/>"><c:out value="${brand.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="car-modal-field">
                    <label for="modalCarBodyType">Tipo de carrocería</label>
                    <select id="modalCarBodyType" name="bodyType" required>
                        <option value="">Selecciona un tipo</option>
                        <c:forEach items="${bodyTypes}" var="bodyType">
                            <option value="<c:out value='${bodyType.name}'/>"><c:out value="${bodyType.name}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <div class="car-modal-field car-modal-field-wide">
                    <label for="modalCarModel">Modelo</label>
                    <input id="modalCarModel" name="model" type="text" maxlength="120" placeholder="Ej: 911 Carrera T" required>
                </div>

                <div class="car-modal-field car-modal-field-wide">
                    <label for="modalCarDescription">Descripción</label>
                    <textarea
                            id="modalCarDescription"
                            name="description"
                            rows="5"
                            maxlength="1500"
                            placeholder="Describe el auto, su propuesta y cualquier detalle relevante."></textarea>
                </div>

                <div class="car-modal-field car-modal-field-wide">
                    <label for="modalCarImageUrl">URL de imagen</label>
                    <input
                            id="modalCarImageUrl"
                            name="imageUrl"
                            type="url"
                            maxlength="500"
                            placeholder="https://ejemplo.com/auto.jpg">
                </div>
            </div>

            <div class="car-modal-actions">
                <button type="button" class="btn-secondary" data-close-car-modal>Cerrar</button>
                <button id="createCarSubmitButton" type="submit" class="btn-primary">Confirmar auto</button>
            </div>
        </form>
    </section>
</div>
