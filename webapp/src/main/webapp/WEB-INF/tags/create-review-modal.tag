<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="false" %>
<%@ attribute name="autoOpen" required="false" type="java.lang.Boolean" %>
<%@ attribute name="pageMode" required="false" type="java.lang.Boolean" %>
<%@ attribute name="cancelHref" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:url var="reviewCreateUrl" value="/reviews"/>

<div id="createReviewModal"
     class="review-modal ${pageMode ? 'form-page-embedded' : ''}"
     <c:if test="${not pageMode}">hidden</c:if>
     data-default-car-id="${carId}"
     <c:if test="${autoOpen or openReviewModal}">data-auto-open="true"</c:if>>
    <c:if test="${not pageMode}">
        <div class="review-modal-overlay" data-close-modal></div>
    </c:if>
    <section class="review-modal-dialog" <c:if test="${not pageMode}">role="dialog" aria-modal="true"</c:if> aria-labelledby="createReviewTitle">
        <div class="review-modal-header">
            <div>
                <span class="review-modal-kicker" data-review-modal-kicker>Nueva reseña</span>
                <h2 id="createReviewTitle" data-review-modal-title>Compartí tu experiencia con el <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></h2>
            </div>
            <c:choose>
                <c:when test="${pageMode}">
                    <a class="review-modal-close" href="${cancelHref}" aria-label="Cancelar">
                        <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false"><line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/></svg>
                    </a>
                </c:when>
                <c:otherwise>
                    <button type="button" class="review-modal-close" data-close-modal aria-label="Cerrar modal">
                        <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false"><line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/></svg>
                    </button>
                </c:otherwise>
            </c:choose>
        </div>

        <form:form id="createReviewForm" cssClass="review-modal-form" modelAttribute="reviewForm"
                   method="post" action="${reviewCreateUrl}"
                   data-create-action="${reviewCreateUrl}"
                   data-submit-lock="true">
            <form:errors cssClass="alert alert-error" element="div"/>
            <c:if test="${not empty error}">
                <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
            </c:if>

            <input id="modalReviewId" name="reviewId" type="hidden" value="">
            <form:hidden id="modalCarId" path="carId"/>

            <p class="review-modal-subtitle" data-review-modal-subtitle>Completá los campos de la reseña. La publicación quedará asociada a tu cuenta.</p>

            <div class="review-modal-grid">
                <div class="review-modal-field review-modal-field-wide">
                    <label id="ratingLabel">Puntuación</label>
                    <div class="star-rating" role="slider" aria-labelledby="ratingLabel" aria-valuemin="0" aria-valuemax="5" aria-valuenow="0" tabindex="0">
                        <form:hidden id="modalRating" path="rating"/>
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
                    <form:errors path="rating" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label>Estado de propiedad</label>
                    <div class="toggle-group">
                        <label class="toggle-option">
                            <form:radiobutton path="ownershipStatus" value=""/>
                            <span>No especificado</span>
                        </label>
                        <label class="toggle-option">
                            <form:radiobutton path="ownershipStatus" value="Propietario actual"/>
                            <span>Propietario actual</span>
                        </label>
                        <label class="toggle-option">
                            <form:radiobutton path="ownershipStatus" value="Ex propietario"/>
                            <span>Ex propietario</span>
                        </label>
                    </div>
                    <form:errors path="ownershipStatus" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalTitle">Título</label>
                    <form:input id="modalTitle" path="title" type="text" maxlength="200"
                                required="required"
                                placeholder="Resumí tu experiencia en una frase"/>
                    <form:errors path="title" cssClass="form-error" element="span"/>
                </div>
                <div class="review-modal-field review-modal-field-wide">
                    <label for="modalBody">Descripción</label>
                    <form:textarea id="modalBody" path="body" rows="4"
                                   required="required"
                                   placeholder="Contanos qué te pareció el auto, qué destacarías y qué mejorarías."/>
                    <form:errors path="body" cssClass="form-error" element="span"/>
                </div>
                <div class="review-modal-field">
                    <label for="modalModelYear">Año del modelo</label>
                    <form:input id="modalModelYear" path="modelYear" type="text" inputmode="numeric"
                                maxlength="4" placeholder="Ej: 2020"/>
                    <form:errors path="modelYear" cssClass="form-error" element="span"/>
                </div>
                <div class="review-modal-field">
                    <label for="modalMileageKm">Kilometraje (km)</label>
                    <form:input id="modalMileageKm" path="mileageKm" type="text" inputmode="numeric"
                                maxlength="7" placeholder="Ej: 45000"/>
                    <form:errors path="mileageKm" cssClass="form-error" element="span"/>
                </div>

                <div class="review-modal-field review-modal-field-wide">
                    <label>¿Lo recomendarías?</label>
                    <div class="toggle-group">
                        <label class="toggle-option">
                            <form:radiobutton path="wouldRecommend" value=""/>
                            <span>No especificado</span>
                        </label>
                        <label class="toggle-option toggle-option--yes">
                            <form:radiobutton path="wouldRecommend" value="true"/>
                            <span>Sí</span>
                        </label>
                        <label class="toggle-option toggle-option--no">
                            <form:radiobutton path="wouldRecommend" value="false"/>
                            <span>No</span>
                        </label>
                    </div>
                    <form:errors path="wouldRecommend" cssClass="form-error" element="span"/>
                </div>
            </div>

            <div class="review-modal-actions">
                <c:choose>
                    <c:when test="${pageMode}">
                        <a id="reviewModalCancelButton" href="${cancelHref}" class="btn-secondary">Cancelar</a>
                    </c:when>
                    <c:otherwise>
                        <button id="reviewModalCancelButton" type="button" class="btn-secondary" data-close-modal>Cancelar</button>
                    </c:otherwise>
                </c:choose>
                <button id="reviewModalSubmitButton" type="submit" class="btn-primary">Guardar reseña</button>
            </div>
        </form:form>
    </section>
</div>
