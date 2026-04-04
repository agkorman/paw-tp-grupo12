<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
        <c:if test="${not empty error}">
            <div class="alert alert-error" role="alert"><c:out value="${error}"/></div>
        </c:if>
        <section class="review-hero">
            <div>
                <h1><c:out value="${selectedCar.model}"/></h1>
                <p class="subtitle">Datos sobre el vehículo y reseñas de los propietarios</p>
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
                if (!validateOptionalNumericFields()) {
                    event.preventDefault();
                    form.reportValidity();
                }
            });
        })();
    </script>
</body>
</html>
