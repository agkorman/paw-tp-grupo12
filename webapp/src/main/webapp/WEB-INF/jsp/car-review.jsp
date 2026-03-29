<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reviews | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
</head>
<body>
    <pa:nav activePage="reviews"/>

    <main class="reviews-page">
        <section class="review-hero">
            <div>
                <h1><c:out value="${selectedCar.model}"/></h1>
                <p class="subtitle">Datos sobre el vehículo y reseñas de los propietarios</p>
            </div>
        </section>

        <section class="review-layout review-detail-layout">
            <article class="selected-car-panel">
                <h2>Imagen del auto</h2>
                <div class="selected-car-image">
                    <c:choose>
                        <c:when test="${not empty selectedCar.imageUrl}">
                            <img src="${fn:escapeXml(selectedCar.imageUrl)}" alt="${fn:escapeXml(selectedCar.model)}" loading="lazy">
                        </c:when>
                        <c:otherwise>
                            <div class="img-placeholder">
                                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                                    <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                                </svg>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </article>

            <div class="review-side-column">
                <aside class="review-form-panel car-info-panel">
                    <h2>Datos del auto</h2>
                    <div class="car-info-list">
                        <div class="car-info-row">
                            <span class="car-info-label">Nombre</span>
                            <span class="car-info-value"><c:out value="${selectedCar.model}"/></span>
                        </div>
                        <div class="car-info-row">
                            <span class="car-info-label">Marca</span>
                            <span class="car-info-value">Marca #<c:out value="${selectedCar.brandId}"/></span>
                        </div>
                        <div class="car-info-row">
                            <span class="car-info-label">Tipo</span>
                            <span class="car-info-value">
                                <c:choose>
                                    <c:when test="${not empty selectedCar.bodyType}"><c:out value="${selectedCar.bodyType}"/></c:when>
                                    <c:otherwise>N/A</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="car-info-row">
                            <span class="car-info-label">Rating</span>
                            <span class="car-info-value">
                                <c:choose>
                                    <c:when test="${not empty averageRating}">
                                        <c:out value="${averageRating}"/>/5.0
                                    </c:when>
                                    <c:otherwise>Sin reviews</c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                    </div>

                    <button id="openReviewModalBtn" type="button" class="btn-primary add-review-btn">Agregar reseña</button>
                </aside>

                <section class="latest-review-section" aria-label="Ultima review">
                    <h2>Ultima review</h2>
                    <c:choose>
                        <c:when test="${empty reviews}">
                            <div class="last-review-empty">
                                Todavia no hay reviews para este auto.
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:set var="latestReview" value="${reviews[0]}"/>
                            <article class="last-review-item">
                                <div class="last-review-top">
                                    <strong><c:out value="${latestReview.title}"/></strong>
                                    <span class="rating-pill"><c:out value="${latestReview.rating}"/>/5.0</span>
                                </div>
                                <p class="last-review-body"><c:out value="${latestReview.body}"/></p>
                                <div class="review-meta last-review-meta">
                                    <span>Usuario #${latestReview.userId}</span>
                                    <span>${latestReview.createdAt}</span>
                                </div>
                            </article>
                        </c:otherwise>
                    </c:choose>
                </section>
            </div>
        </section>

        <section class="reviews-feed">
            <div class="feed-header">
                <h2>Reviews</h2>
                <div class="feed-header-actions">
                    <span class="count-label">${fn:length(reviews)} entradas</span>
                    <button type="button" class="filter-chip review-filter-btn">Filtrar</button>
                </div>
            </div>
            <c:choose>
                <c:when test="${empty reviews}">
                    <div class="empty-state">
                        <p>Todavia no hay reviews para este auto.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="review-list">
                        <c:forEach var="review" items="${reviews}">
                            <article class="review-item">
                                <div class="review-item-top">
                                    <strong><c:out value="${review.title}"/></strong>
                                    <span class="rating-pill">${review.rating}/5.0</span>
                                </div>
                                <p class="review-body"><c:out value="${review.body}"/></p>
                                <div class="review-meta">
                                    <span>Usuario #${review.userId}</span>
                                    <span>${review.createdAt}</span>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <div id="createReviewModal" class="review-modal" hidden>
        <div class="review-modal-overlay" data-close-modal></div>
        <section class="review-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="createReviewTitle">
            <div class="review-modal-header">
                <h2 id="createReviewTitle">Crear reseña</h2>
                <button type="button" class="review-modal-close" data-close-modal aria-label="Cerrar modal">x</button>
            </div>

            <form id="createReviewForm" class="review-modal-form">
                <input id="modalCarId" name="carId" type="hidden" value="${selectedCar.id}">
                <input id="modalUserId" name="userId" type="hidden" value="1">

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

    <pa:footer/>

    <script>
        (function () {
            var openButton = document.getElementById('openReviewModalBtn');
            var modal = document.getElementById('createReviewModal');
            var form = document.getElementById('createReviewForm');
            var modelYearInput = document.getElementById('modalModelYear');
            var mileageInput = document.getElementById('modalMileageKm');

            if (!openButton || !modal || !form) {
                return;
            }

            var closeElements = Array.prototype.slice.call(modal.querySelectorAll('[data-close-modal]'));

            var closeModal = function () {
                modal.setAttribute('hidden', 'hidden');
                document.body.classList.remove('modal-open');
                openButton.focus();
            };

            var openModal = function () {
                modal.removeAttribute('hidden');
                document.body.classList.add('modal-open');
                var firstInput = modal.querySelector('#modalRating');
                if (firstInput) {
                    firstInput.focus();
                }
            };

            openButton.addEventListener('click', openModal);

            closeElements.forEach(function (element) {
                element.addEventListener('click', closeModal);
            });

            document.addEventListener('keydown', function (event) {
                if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
                    closeModal();
                }
            });

            var isIntegerString = function (value) {
                return /^\d+$/.test(value);
            };

            var clearValidation = function (input) {
                if (input) {
                    input.setCustomValidity('');
                }
            };

            var validateOptionalNumericFields = function () {
                var maxYear = new Date().getFullYear() + 1;
                var isValid = true;

                clearValidation(modelYearInput);
                clearValidation(mileageInput);

                if (modelYearInput) {
                    var rawYear = modelYearInput.value.trim();
                    modelYearInput.value = rawYear;
                    if (rawYear.length > 0) {
                        if (!isIntegerString(rawYear)) {
                            modelYearInput.setCustomValidity('El ano del modelo debe ser numerico.');
                            isValid = false;
                        } else {
                            var parsedYear = Number(rawYear);
                            if (parsedYear < 1886 || parsedYear > maxYear) {
                                modelYearInput.setCustomValidity('Ingresa un ano del modelo entre 1886 y ' + maxYear + '.');
                                isValid = false;
                            }
                        }
                    }
                }

                if (mileageInput) {
                    var rawMileage = mileageInput.value.trim();
                    mileageInput.value = rawMileage;
                    if (rawMileage.length > 0) {
                        if (!isIntegerString(rawMileage)) {
                            mileageInput.setCustomValidity('El kilometraje debe ser numerico.');
                            isValid = false;
                        } else {
                            var parsedMileage = Number(rawMileage);
                            if (parsedMileage < 0 || parsedMileage > 2000000) {
                                mileageInput.setCustomValidity('Ingresa un kilometraje entre 0 y 2000000 km.');
                                isValid = false;
                            }
                        }
                    }
                }

                return isValid;
            };

            if (modelYearInput) {
                modelYearInput.addEventListener('input', function () {
                    clearValidation(modelYearInput);
                });
            }

            if (mileageInput) {
                mileageInput.addEventListener('input', function () {
                    clearValidation(mileageInput);
                });
            }

            form.addEventListener('submit', function (event) {
                event.preventDefault();
                if (!validateOptionalNumericFields()) {
                    form.reportValidity();
                    return;
                }
                closeModal();
            });
        })();
    </script>
</body>
</html>
