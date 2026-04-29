<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="deleteCarModal" class="review-modal" hidden>
    <div class="review-modal-overlay" data-close-delete-car-modal></div>
    <section class="review-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="deleteCarTitle">
        <div class="review-modal-header">
            <div>
                <span class="review-modal-kicker">Catálogo</span>
                <h2 id="deleteCarTitle">Eliminar auto</h2>
            </div>
            <button type="button" class="review-modal-close" data-close-delete-car-modal aria-label="Cerrar modal">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false"><line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/></svg>
            </button>
        </div>
        <form id="deleteCarForm" class="car-delete-form" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <p>Se eliminarán también todas las reviews asociadas a este auto.</p>
            <strong data-delete-car-title></strong>
            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-delete-car-modal>Cancelar</button>
                <button type="submit" class="btn-primary car-delete-confirm-button">Eliminar</button>
            </div>
        </form>
    </section>
</div>
