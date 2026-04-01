<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="true" %>
<%@ attribute name="userId" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="resolvedUserId" value="${empty userId ? 1 : userId}"/>

<div id="createReviewModal" class="review-modal" hidden>
    <div class="review-modal-overlay" data-close-modal></div>
    <section class="review-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="createReviewTitle">
        <div class="review-modal-header">
            <h2 id="createReviewTitle">Crear reseña</h2>
            <button type="button" class="review-modal-close" data-close-modal aria-label="Cerrar modal">x</button>
        </div>

        <form id="createReviewForm" class="review-modal-form">
            <input id="modalCarId" name="carId" type="hidden" value="${carId}">
            <input id="modalUserId" name="userId" type="hidden" value="${resolvedUserId}">

            <p class="review-modal-subtitle">Completa los campos de la reseña. Este formulario es solo visual por ahora.</p>

            <div class="review-modal-grid">
                <div class="review-modal-field">
                    <label for="modalRating">Puntuacion (0.0 - 5.0)</label>
                    <input id="modalRating" name="rating" type="number" min="0" max="5" step="0.5" required>
                </div>
                <div class="review-modal-field">
                    <label for="modalOwnershipStatus">Estado de propiedad</label>
                    <select id="modalOwnershipStatus" name="ownershipStatus">
                        <option value="">No especificado</option>
                        <option value="Propietario actual">Propietario actual</option>
                        <option value="Ex propietario">Ex propietario</option>
                    </select>
                </div>
                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalTitle">Titulo</label>
                    <input id="modalTitle" name="title" type="text" maxlength="200" required>
                </div>
                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalBody">Descripcion</label>
                    <textarea id="modalBody" name="body" rows="4" required></textarea>
                </div>
                <div class="review-modal-field">
                    <label for="modalModelYear">Ano del modelo</label>
                    <input id="modalModelYear" name="modelYear" type="text" inputmode="numeric" maxlength="4" placeholder="Ej: 2020">
                </div>
                <div class="review-modal-field">
                    <label for="modalMileageKm">Kilometraje (km)</label>
                    <input id="modalMileageKm" name="mileageKm" type="text" inputmode="numeric" maxlength="7" placeholder="Ej: 45000">
                </div>
                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalWouldRecommend">Lo recomendarias</label>
                    <select id="modalWouldRecommend" name="wouldRecommend">
                        <option value="">No especificado</option>
                        <option value="true">Si</option>
                        <option value="false">No</option>
                    </select>
                </div>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-modal>Cancelar</button>
                <button type="submit" class="btn-primary">Guardar resena</button>
            </div>
        </form>
    </section>
</div>
