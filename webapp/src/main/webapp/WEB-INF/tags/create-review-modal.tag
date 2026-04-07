<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="createReviewModal" class="review-modal" hidden>
    <div class="review-modal-overlay" data-close-modal></div>
    <section class="review-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="createReviewTitle">
        <div class="review-modal-header">
            <div>
                <span class="review-modal-kicker">Nueva reseña</span>
                <h2 id="createReviewTitle">Compartí tu experiencia</h2>
            </div>
            <button type="button" class="review-modal-close" data-close-modal aria-label="Cerrar modal">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false"><line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/></svg>
            </button>
        </div>

        <form id="createReviewForm" class="review-modal-form" method="post" action="<c:url value='/reviews'/>" novalidate>
            <input id="modalCarId" name="carId" type="hidden" value="${carId}">

            <p class="review-modal-subtitle">Completá los campos de la reseña. Tu email se guardará para vincular esta reseña a una cuenta más adelante.</p>

            <div class="review-modal-grid">
                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalReviewerEmail">Email</label>
                    <input id="modalReviewerEmail" name="reviewerEmail" type="email" maxlength="100" required placeholder="tu@email.com">
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label id="ratingLabel">Puntuación</label>
                    <div class="star-rating" role="slider" aria-labelledby="ratingLabel" aria-valuemin="0" aria-valuemax="5" aria-valuenow="0" tabindex="0">
                        <input id="modalRating" name="rating" type="hidden" value="" required>
                        <div class="star-rating-stars">
                            <c:forEach var="i" begin="1" end="5">
                                <div class="star-slot" data-star="${i}">
                                    <svg viewBox="0 0 24 24" width="36" height="36">
                                        <defs>
                                            <linearGradient id="starGrad${i}">
                                                <stop offset="0%" stop-color="#2e2e2e"/>
                                                <stop offset="100%" stop-color="#2e2e2e"/>
                                            </linearGradient>
                                        </defs>
                                        <path fill="url(#starGrad${i})" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                    </svg>
                                    <button type="button" class="star-hit star-hit-left" data-star="${i}" data-half="true" aria-label="${i - 1} y media estrellas"></button>
                                    <button type="button" class="star-hit star-hit-right" data-star="${i}" data-half="false" aria-label="${i} estrellas"></button>
                                </div>
                            </c:forEach>
                        </div>
                        <span class="star-rating-value" aria-live="polite">Sin puntuación</span>
                    </div>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label>Estado de propiedad</label>
                    <div class="toggle-group">
                        <label class="toggle-option">
                            <input type="radio" name="ownershipStatus" value="" checked>
                            <span>No especificado</span>
                        </label>
                        <label class="toggle-option">
                            <input type="radio" name="ownershipStatus" value="Propietario actual">
                            <span>Propietario actual</span>
                        </label>
                        <label class="toggle-option">
                            <input type="radio" name="ownershipStatus" value="Ex propietario">
                            <span>Ex propietario</span>
                        </label>
                    </div>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalTitle">Título</label>
                    <input id="modalTitle" name="title" type="text" maxlength="200" required placeholder="Resumí tu experiencia en una frase">
                </div>
                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalBody">Descripción</label>
                    <textarea id="modalBody" name="body" rows="4" required placeholder="Contanos qué te pareció el auto, qué destacarías y qué mejorarías."></textarea>
                </div>
                <div class="review-modal-field">
                    <label for="modalModelYear">Año del modelo</label>
                    <input id="modalModelYear" name="modelYear" type="text" inputmode="numeric" maxlength="4" placeholder="Ej: 2020">
                </div>
                <div class="review-modal-field">
                    <label for="modalMileageKm">Kilometraje (km)</label>
                    <input id="modalMileageKm" name="mileageKm" type="text" inputmode="numeric" maxlength="7" placeholder="Ej: 45000">
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label>¿Lo recomendarías?</label>
                    <div class="toggle-group">
                        <label class="toggle-option">
                            <input type="radio" name="wouldRecommend" value="" checked>
                            <span>No especificado</span>
                        </label>
                        <label class="toggle-option toggle-option--yes">
                            <input type="radio" name="wouldRecommend" value="true">
                            <span>Sí</span>
                        </label>
                        <label class="toggle-option toggle-option--no">
                            <input type="radio" name="wouldRecommend" value="false">
                            <span>No</span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-modal>Cancelar</button>
                <button type="submit" class="btn-primary">Guardar reseña</button>
            </div>
        </form>
    </section>
</div>
