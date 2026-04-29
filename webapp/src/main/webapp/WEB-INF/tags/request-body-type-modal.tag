<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="requestBodyTypeUrl" value="/body-type-requests"/>

<div id="requestBodyTypeModal" class="review-modal catalog-request-modal" hidden>
    <div class="review-modal-overlay" data-close-catalog-request-modal></div>
    <section class="review-modal-dialog catalog-request-dialog" role="dialog" aria-modal="true" aria-labelledby="requestBodyTypeModalTitle">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker">Solicitud</span>
                <h2 id="requestBodyTypeModalTitle">Sugerir una carrocería</h2>
            </div>
            <button type="button" class="review-modal-close" data-close-catalog-request-modal aria-label="Cerrar modal">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="car-modal-subtitle">
            Decinos qué tipo de carrocería te gustaría ver disponible. Un moderador la va a revisar antes de agregarla al catálogo.
        </p>

        <form id="requestBodyTypeForm" class="catalog-request-form" method="post" action="${requestBodyTypeUrl}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestBodyTypeName">Nombre de la carrocería</label>
                <input id="requestBodyTypeName" name="name" type="text"
                       maxlength="80" required
                       placeholder="Ej: Targa">
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestBodyTypeComments">Comentarios (opcional)</label>
                <textarea id="requestBodyTypeComments" name="comments" rows="3" maxlength="500"
                          placeholder="Describí brevemente la carrocería o un ejemplo de auto..."></textarea>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-catalog-request-modal>Cancelar</button>
                <button type="submit" class="btn-primary">Enviar solicitud</button>
            </div>
        </form>
    </section>
</div>
