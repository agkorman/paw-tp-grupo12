<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${editMode ? 'Editar reseña' : 'Nueva reseña'}"/> | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css?v=4'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-tags.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/form-pages.css'/>">
</head>
<body>
    <pa:nav activePage="reviews"/>
    <c:url var="reviewCancelUrl" value="/reviews">
        <c:param name="carId" value="${selectedCar.id}"/>
    </c:url>
    <c:url var="reviewCreateUrl" value="/reviews"/>
    <c:url var="profileUrl" value="/profile"/>
    <c:url var="reviewUpdateUrl" value="/reviews/${reviewId}"/>
    <c:set var="reviewFormAction" value="${editMode ? reviewUpdateUrl : reviewCreateUrl}"/>
    <c:set var="reviewFormCancelUrl" value="${editMode ? profileUrl : reviewCancelUrl}"/>

    <main class="form-page">
        <section id="createReviewFormPage" class="form-page-panel" data-default-car-id="${selectedCar.id}" aria-labelledby="createReviewTitle">
            <div class="review-modal-header">
                <div>
                    <span class="review-modal-kicker" data-review-modal-kicker><c:out value="${editMode ? 'Editar reseña' : 'Nueva reseña'}"/></span>
                    <h1 id="createReviewTitle" data-review-modal-title>
                        <c:choose>
                            <c:when test="${editMode}">Editá tu experiencia con el <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></c:when>
                            <c:otherwise>Compartí tu experiencia con el <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></c:otherwise>
                        </c:choose>
                    </h1>
                </div>
            </div>

            <form:form id="createReviewForm" cssClass="review-modal-form" modelAttribute="reviewForm"
                       method="post" action="${reviewFormAction}"
                       data-create-action="${reviewCreateUrl}"
                       data-submit-lock="true"
                       novalidate="novalidate">
                <form:errors cssClass="alert alert-error" element="div"/>
                <c:if test="${not empty error}">
                    <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
                </c:if>

                <input id="modalReviewId" name="reviewId" type="hidden" value="">
                <form:hidden id="modalCarId" path="carId"/>

                <p class="review-modal-subtitle" data-review-modal-subtitle>
                    <c:choose>
                        <c:when test="${editMode}">Modificá los datos de la reseña y confirmá los cambios.</c:when>
                        <c:otherwise>Completá los campos de la reseña. La publicación quedará asociada a tu cuenta.</c:otherwise>
                    </c:choose>
                </p>

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
                                    maxlength="4" required="required" placeholder="Ej: 2020"/>
                        <form:errors path="modelYear" cssClass="form-error" element="span"/>
                    </div>
                    <div class="review-modal-field">
                        <label for="modalMileageKm">Kilometraje (km)</label>
                        <form:input id="modalMileageKm" path="mileageKm" type="text" inputmode="numeric"
                                    maxlength="7" required="required" placeholder="Ej: 45000"/>
                        <form:errors path="mileageKm" cssClass="form-error" element="span"/>
                    </div>

                    <div class="review-modal-field review-modal-field-wide">
                        <label>¿Lo recomendarías?</label>
                        <div class="toggle-group">
                            <label class="toggle-option">
                                <c:choose>
                                    <c:when test="${reviewForm.wouldRecommend eq null}">
                                        <form:radiobutton path="wouldRecommend" value="" checked="checked"/>
                                    </c:when>
                                    <c:otherwise>
                                        <form:radiobutton path="wouldRecommend" value=""/>
                                    </c:otherwise>
                                </c:choose>
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

                    <div class="review-modal-field review-modal-field-wide">
                        <pa:review-tag-chips mode="edit"
                                             tagsBySentiment="${reviewTagsBySentiment}"
                                             selectedTagIds="${reviewForm.tagIds}"/>
                        <form:errors path="tagIds" cssClass="form-error" element="span"/>
                    </div>
                </div>

                <div class="review-modal-actions">
                    <a id="reviewModalCancelButton" href="${reviewFormCancelUrl}" class="btn-secondary">Cancelar</a>
                    <button id="reviewModalSubmitButton" type="submit" class="btn-primary"><c:out value="${editMode ? 'Guardar cambios' : 'Guardar reseña'}"/></button>
                </div>
            </form:form>
        </section>
    </main>

    <script src="<c:url value='/js/review-form.js?v=1'/>"></script>
    <script src="<c:url value='/js/review-tag-chips.js'/>" defer></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
