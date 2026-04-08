<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reseñas | La Posta Autos</title>
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
        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
        </c:if>
        <section class="review-hero">
            <div class="review-hero-inner">
                <div>
                    <h1><c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></h1>
                    <p class="subtitle">Datos sobre el vehículo y reseñas de los propietarios</p>
                </div>
                <div class="review-hero-actions">
                    <c:if test="${not empty averageRating}">
                        <div class="hero-stars-row" aria-label="${averageRating} de 5 estrellas">
                            <c:forEach var="i" begin="1" end="5">
                                <svg viewBox="0 0 24 24" width="32" height="32" aria-hidden="true">
                                    <c:choose>
                                        <c:when test="${averageRating >= i}">
                                            <path fill="#ff5719" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                        </c:when>
                                        <c:when test="${averageRating >= i - 0.5}">
                                            <defs>
                                                <linearGradient id="hsg${i}" x1="0" x2="1" y1="0" y2="0">
                                                    <stop offset="50%" stop-color="#ff5719"/>
                                                    <stop offset="50%" stop-color="#3a3a3a"/>
                                                </linearGradient>
                                            </defs>
                                            <path fill="url(#hsg${i})" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                        </c:when>
                                        <c:otherwise>
                                            <path fill="#3a3a3a" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                        </c:otherwise>
                                    </c:choose>
                                </svg>
                            </c:forEach>
                        </div>
                    </c:if>
                    <a href="#reviewsFeed" class="btn-secondary hero-see-reviews-btn">
                        Ver reseñas
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                            <path d="M12 5v14M5 12l7 7 7-7"/>
                        </svg>
                    </a>
                </div>
            </div>
        </section>

        <section class="review-layout review-detail-layout">
            <pa:review-selected-car selectedCar="${selectedCar}"/>

            <div class="review-side-column">
                <pa:review-car-info selectedCar="${selectedCar}" averageRating="${averageRating}"/>
                <pa:latest-review latestReview="${latestReview}"/>
            </div>
        </section>

        <pa:reviews-feed reviews="${reviews}" carId="${selectedCar.id}" currentSort="${currentSort}"/>
    </main>

    <pa:create-review-modal carId="${selectedCar.id}"/>

    <pa:footer/>
    <script src="<c:url value='/js/enhanced-filters.js'/>"></script>

    <script>
        (function () {
            function $(id) { return document.getElementById(id); }
            function $$(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }

            var openButton = $('openReviewModalBtn');
            var modal      = $('createReviewModal');
            var form       = $('createReviewForm');
            if (!openButton || !modal || !form) return;

            /* ================================================================
               STAR RATING — Interactive half-star (0.5 step) rating widget.
               Each star SVG uses a <linearGradient> with two <stop> elements.
               To show a half star we set stop 1 = filled at 50%, stop 2 = empty
               at 50%. Full star = both stops filled. Empty = both stops empty.
               Click the left half of a star for N-0.5, right half for N.
               Clicking the same value again resets to 0.
               ================================================================ */

            /* -- Star colors ------------------------------------------------ */
            var STAR_FILLED = '#ff5719';
            var STAR_EMPTY  = '#2e2e2e';

            /* -- Star label lookup ------------------------------------------ */
            function starTextFor(v) {
                if (v === 0) return 'Sin puntuación';
                if (v <= 1)  return 'Malo';
                if (v <= 2)  return 'Regular';
                if (v <= 3)  return 'Bueno';
                if (v <= 4)  return 'Muy bueno';
                return 'Excelente';
            }

            /* -- Paint a single star's gradient to reflect the rating ------- */
            function paintStar(slot, starNum, rating) {
                var stops = slot.querySelector('linearGradient').querySelectorAll('stop');

                if (rating >= starNum) {
                    /* Full star */
                    stops[0].setAttribute('stop-color', STAR_FILLED);
                    stops[0].setAttribute('offset', '100%');
                    stops[1].setAttribute('stop-color', STAR_FILLED);
                    stops[1].setAttribute('offset', '100%');
                } else if (rating >= starNum - 0.5) {
                    /* Half star — left half filled, right half empty */
                    stops[0].setAttribute('stop-color', STAR_FILLED);
                    stops[0].setAttribute('offset', '50%');
                    stops[1].setAttribute('stop-color', STAR_EMPTY);
                    stops[1].setAttribute('offset', '50%');
                } else {
                    /* Empty star */
                    stops[0].setAttribute('stop-color', STAR_EMPTY);
                    stops[0].setAttribute('offset', '0%');
                    stops[1].setAttribute('stop-color', STAR_EMPTY);
                    stops[1].setAttribute('offset', '100%');
                }
            }

            /* -- Render all 5 stars for a given rating ---------------------- */
            var starSlots = $$('.star-slot', modal);

            function renderStars(rating) {
                starSlots.forEach(function (slot, i) { paintStar(slot, i + 1, rating); });
            }

            /* -- Set / reset the rating value ------------------------------- */
            var starInput   = $('modalRating');
            var starWrap    = modal.querySelector('.star-rating');
            var starLabel   = modal.querySelector('.star-rating-value');
            var currentRating = 0;

            function setRating(value) {
                currentRating = value;
                starInput.value = value;
                starLabel.textContent = value + '/5 — ' + starTextFor(value);
                starLabel.style.color = '';
                starWrap.setAttribute('aria-valuenow', value);
                renderStars(value);
            }

            function resetRating() {
                currentRating = 0;
                starInput.value = '';
                starLabel.textContent = starTextFor(0);
                starLabel.style.color = '';
                starWrap.setAttribute('aria-valuenow', 0);
                renderStars(0);
            }

            /* -- Click: left half = N-0.5, right half = N. Toggle off on re-click */
            $$('.star-hit', modal).forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var star   = parseInt(btn.getAttribute('data-star'), 10);
                    var isHalf = btn.getAttribute('data-half') === 'true';
                    var value  = isHalf ? star - 0.5 : star;
                    if (value === currentRating) resetRating(); else setRating(value);
                });
            });

            /* -- Keyboard: arrows step by 0.5 ------------------------------- */
            starWrap.addEventListener('keydown', function (e) {
                if (e.key === 'ArrowRight' || e.key === 'ArrowUp') {
                    e.preventDefault();
                    setRating(Math.min(5, currentRating + 0.5));
                } else if (e.key === 'ArrowLeft' || e.key === 'ArrowDown') {
                    e.preventDefault();
                    if (currentRating <= 0.5) resetRating(); else setRating(currentRating - 0.5);
                }
            });

            /* -- Validate: requires a rating before submit ------------------ */
            function validateRating() {
                if (!starInput.value) {
                    starLabel.textContent = 'Seleccioná una puntuación';
                    starLabel.style.color = '#ef9a9a';
                    starWrap.focus();
                    return false;
                }
                return true;
            }

            renderStars(0);

            /* ================================================================
               MODAL OPEN / CLOSE
               ================================================================ */
            var closeEls = $$('[data-close-modal]', modal);

            function closeModal() {
                modal.setAttribute('hidden', 'hidden');
                document.body.classList.remove('modal-open');
                openButton.focus();
            }

            function openModal() {
                modal.removeAttribute('hidden');
                document.body.classList.add('modal-open');
                var first = $('modalReviewerEmail');
                if (first) first.focus();
            }

            openButton.addEventListener('click', openModal);
            closeEls.forEach(function (el) { el.addEventListener('click', closeModal); });
            document.addEventListener('keydown', function (e) {
                if (e.key === 'Escape' && !modal.hasAttribute('hidden')) closeModal();
            });

            /* ================================================================
               NUMERIC FIELD VALIDATION (model year & mileage)
               ================================================================ */
            var modelYearInput = $('modalModelYear');
            var mileageInput   = $('modalMileageKm');
            var requiredMessages = {
                modalReviewerEmail: 'Ingresá tu email.',
                modalTitle: 'Ingresá un título.',
                modalBody: 'Ingresá una descripción.'
            };

            function isInt(s) { return /^\d+$/.test(s); }
            function clearV(inp) { if (inp) inp.setCustomValidity(''); }

            function validateRequiredField(inp) {
                if (!inp) return true;

                clearV(inp);
                if (inp.required && (!inp.value || inp.value.trim() === '')) {
                    inp.setCustomValidity(requiredMessages[inp.id] || 'Completá este campo.');
                    return false;
                }
                if (inp.validity.typeMismatch && inp.type === 'email') {
                    inp.setCustomValidity('Ingresá un email válido.');
                    return false;
                }
                return inp.checkValidity();
            }

            function validateRequiredFields() {
                return Array.prototype.slice.call(form.querySelectorAll('[required]')).reduce(function (isValid, inp) {
                    return validateRequiredField(inp) && isValid;
                }, true);
            }

            function validateNumerics() {
                var maxYear = new Date().getFullYear() + 1;
                var ok = true;
                clearV(modelYearInput);
                clearV(mileageInput);

                if (modelYearInput) {
                    var y = modelYearInput.value.trim();
                    modelYearInput.value = y;
                    if (y.length > 0) {
                        if (!isInt(y)) {
                            modelYearInput.setCustomValidity('El año del modelo debe ser numérico.');
                            ok = false;
                        } else {
                            var py = Number(y);
                            if (py < 1886 || py > maxYear) {
                                modelYearInput.setCustomValidity('Ingresá un año entre 1886 y ' + maxYear + '.');
                                ok = false;
                            }
                        }
                    }
                }
                if (mileageInput) {
                    var m = mileageInput.value.trim();
                    mileageInput.value = m;
                    if (m.length > 0) {
                        if (!isInt(m)) {
                            mileageInput.setCustomValidity('El kilometraje debe ser numérico.');
                            ok = false;
                        } else {
                            var pm = Number(m);
                            if (pm < 0 || pm > 2000000) {
                                mileageInput.setCustomValidity('Ingresá un kilometraje entre 0 y 2.000.000 km.');
                                ok = false;
                            }
                        }
                    }
                }
                return ok;
            }

            if (modelYearInput) modelYearInput.addEventListener('input', function () { clearV(modelYearInput); });
            if (mileageInput)   mileageInput.addEventListener('input', function () { clearV(mileageInput); });
            Array.prototype.slice.call(form.querySelectorAll('[required]')).forEach(function (inp) {
                inp.addEventListener('input', function () { validateRequiredField(inp); });
            });

            /* ================================================================
               FORM SUBMIT — chain all validations
               ================================================================ */
            form.addEventListener('submit', function (e) {
                if (!validateRating()) { e.preventDefault(); return; }
                if (!validateRequiredFields()) { e.preventDefault(); form.reportValidity(); return; }
                if (!validateNumerics()) { e.preventDefault(); form.reportValidity(); }
            });
        })();
    </script>
</body>
</html>
