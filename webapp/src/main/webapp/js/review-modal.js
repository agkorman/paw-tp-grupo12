(function () {
    function $(id) { return document.getElementById(id); }
    function $$(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }

    var modal = $('createReviewModal');
    var form = $('createReviewForm');
    if (!modal || !form) {
        return;
    }

    var STAR_FILLED = '#ff5719';
    var STAR_EMPTY = '#2e2e2e';
    var lastTrigger = null;

    var starSlots = $$('.star-slot', modal);
    var starInput = $('modalRating');
    var starWrap = modal.querySelector('.star-rating');
    var starLabel = modal.querySelector('.star-rating-value');
    var currentRating = 0;

    var modalKicker = modal.querySelector('[data-review-modal-kicker]');
    var modalTitle = modal.querySelector('[data-review-modal-title]');
    var modalSubtitle = modal.querySelector('[data-review-modal-subtitle]');
    var reviewIdInput = $('modalReviewId');
    var carIdInput = $('modalCarId');
    var titleInput = $('modalTitle');
    var bodyInput = $('modalBody');
    var modelYearInput = $('modalModelYear');
    var mileageInput = $('modalMileageKm');
    var submitButton = $('reviewModalSubmitButton');
    var cancelButton = $('reviewModalCancelButton');

    var createTexts = {
        kicker: 'Nueva reseña',
        title: 'Compartí tu experiencia',
        subtitle: 'Completá los campos de la reseña. La publicación quedará asociada a tu cuenta.',
        submit: 'Guardar reseña',
        cancel: 'Cancelar'
    };
    var editTexts = {
        kicker: 'Editar reseña',
        title: 'Editá tu experiencia',
        subtitle: 'Modificá los datos de la reseña y confirmá los cambios.',
        submit: 'Aceptar',
        cancel: 'Cancelar'
    };
    var requiredMessages = {
        modalTitle: 'Ingresá un título.',
        modalBody: 'Ingresá una descripción.'
    };

    function setText(node, value) {
        if (node) {
            node.textContent = value;
        }
    }

    function starTextFor(value) {
        if (value === 0) return 'Sin puntuación';
        if (value <= 1) return 'Malo';
        if (value <= 2) return 'Regular';
        if (value <= 3) return 'Bueno';
        if (value <= 4) return 'Muy bueno';
        return 'Excelente';
    }

    function paintStar(slot, starNum, rating) {
        var gradient = slot.querySelector('linearGradient');
        if (!gradient) {
            return;
        }
        var stops = gradient.querySelectorAll('stop');
        if (stops.length < 2) {
            return;
        }

        if (rating >= starNum) {
            stops[0].setAttribute('stop-color', STAR_FILLED);
            stops[0].setAttribute('offset', '100%');
            stops[1].setAttribute('stop-color', STAR_FILLED);
            stops[1].setAttribute('offset', '100%');
        } else if (rating >= starNum - 0.5) {
            stops[0].setAttribute('stop-color', STAR_FILLED);
            stops[0].setAttribute('offset', '50%');
            stops[1].setAttribute('stop-color', STAR_EMPTY);
            stops[1].setAttribute('offset', '50%');
        } else {
            stops[0].setAttribute('stop-color', STAR_EMPTY);
            stops[0].setAttribute('offset', '0%');
            stops[1].setAttribute('stop-color', STAR_EMPTY);
            stops[1].setAttribute('offset', '100%');
        }
    }

    function renderStars(rating) {
        starSlots.forEach(function (slot, index) {
            paintStar(slot, index + 1, rating);
        });
    }

    function setRating(value) {
        currentRating = value;
        if (starInput) {
            starInput.value = value;
        }
        if (starLabel) {
            starLabel.textContent = value + '/5 - ' + starTextFor(value);
            starLabel.style.color = '';
        }
        if (starWrap) {
            starWrap.setAttribute('aria-valuenow', value);
        }
        renderStars(value);
    }

    function resetRating() {
        currentRating = 0;
        if (starInput) {
            starInput.value = '';
        }
        if (starLabel) {
            starLabel.textContent = starTextFor(0);
            starLabel.style.color = '';
        }
        if (starWrap) {
            starWrap.setAttribute('aria-valuenow', 0);
        }
        renderStars(0);
    }

    function setRadioValue(name, value) {
        var normalized = value === null || typeof value === 'undefined' ? '' : String(value);
        var radios = form.querySelectorAll('input[name="' + name + '"]');
        for (var i = 0; i < radios.length; i += 1) {
            radios[i].checked = radios[i].value === normalized;
        }
    }

    function setModeTexts(texts) {
        setText(modalKicker, texts.kicker);
        setText(modalTitle, texts.title);
        setText(modalSubtitle, texts.subtitle);
        setText(submitButton, texts.submit);
        setText(cancelButton, texts.cancel);
    }

    function setCreateMode(trigger) {
        form.reset();
        form.action = form.getAttribute('data-create-action') || form.action;
        setModeTexts(createTexts);
        if (reviewIdInput) {
            reviewIdInput.value = '';
        }
        if (carIdInput) {
            carIdInput.value = trigger.getAttribute('data-review-car-id') || modal.getAttribute('data-default-car-id') || carIdInput.value || '';
        }
        resetRating();
    }

    function setEditMode(trigger) {
        var data = trigger.dataset;
        form.reset();
        form.action = data.reviewAction || form.action;
        setModeTexts(editTexts);
        if (reviewIdInput) reviewIdInput.value = data.reviewId || '';
        if (carIdInput) carIdInput.value = data.reviewCarId || '';
        if (titleInput) titleInput.value = data.reviewTitle || '';
        if (bodyInput) bodyInput.value = data.reviewBody || '';
        if (modelYearInput) modelYearInput.value = data.reviewModelYear || '';
        if (mileageInput) mileageInput.value = data.reviewMileageKm || '';
        setRadioValue('ownershipStatus', data.reviewOwnershipStatus || '');
        setRadioValue('wouldRecommend', data.reviewWouldRecommend || '');
        setRating(Number(data.reviewRating || 0));
    }

    function openModal(trigger) {
        lastTrigger = trigger || lastTrigger;
        if (trigger && trigger.getAttribute('data-open-review-modal') === 'edit') {
            setEditMode(trigger);
        } else {
            setCreateMode(trigger || document.createElement('button'));
        }
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
        if (titleInput) {
            titleInput.focus();
        }
    }

    function closeModal() {
        modal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        if (lastTrigger && document.contains(lastTrigger)) {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    function validateRating() {
        if (!starInput || !starInput.value) {
            if (starLabel) {
                starLabel.textContent = 'Seleccioná una puntuación';
                starLabel.style.color = '#ef9a9a';
            }
            if (starWrap) {
                starWrap.focus();
            }
            return false;
        }
        return true;
    }

    function isInt(value) {
        return /^\d+$/.test(value);
    }

    function clearValidity(input) {
        if (input) {
            input.setCustomValidity('');
        }
    }

    function validateRequiredField(input) {
        if (!input) {
            return true;
        }

        clearValidity(input);
        if (input.required && (!input.value || input.value.trim() === '')) {
            input.setCustomValidity(requiredMessages[input.id] || 'Completá este campo.');
            return false;
        }
        return input.checkValidity();
    }

    function validateRequiredFields() {
        return Array.prototype.slice.call(form.querySelectorAll('[required]')).reduce(function (isValid, input) {
            return validateRequiredField(input) && isValid;
        }, true);
    }

    function validateNumerics() {
        var maxYear = new Date().getFullYear() + 1;
        var ok = true;
        clearValidity(modelYearInput);
        clearValidity(mileageInput);

        if (modelYearInput) {
            var year = modelYearInput.value.trim();
            modelYearInput.value = year;
            if (year.length > 0) {
                if (!isInt(year)) {
                    modelYearInput.setCustomValidity('El año del modelo debe ser numérico.');
                    ok = false;
                } else {
                    var parsedYear = Number(year);
                    if (parsedYear < 1886 || parsedYear > maxYear) {
                        modelYearInput.setCustomValidity('Ingresá un año entre 1886 y ' + maxYear + '.');
                        ok = false;
                    }
                }
            }
        }

        if (mileageInput) {
            var mileage = mileageInput.value.trim();
            mileageInput.value = mileage;
            if (mileage.length > 0) {
                if (!isInt(mileage)) {
                    mileageInput.setCustomValidity('El kilometraje debe ser numérico.');
                    ok = false;
                } else {
                    var parsedMileage = Number(mileage);
                    if (parsedMileage < 0 || parsedMileage > 2000000) {
                        mileageInput.setCustomValidity('Ingresá un kilometraje entre 0 y 2.000.000 km.');
                        ok = false;
                    }
                }
            }
        }

        return ok;
    }

    $$('.star-hit', modal).forEach(function (button) {
        button.addEventListener('click', function () {
            var star = parseInt(button.getAttribute('data-star'), 10);
            var isHalf = button.getAttribute('data-half') === 'true';
            var value = isHalf ? star - 0.5 : star;
            if (value === currentRating) {
                resetRating();
            } else {
                setRating(value);
            }
        });
    });

    if (starWrap) {
        starWrap.addEventListener('keydown', function (event) {
            if (event.key === 'ArrowRight' || event.key === 'ArrowUp') {
                event.preventDefault();
                setRating(Math.min(5, currentRating + 0.5));
            } else if (event.key === 'ArrowLeft' || event.key === 'ArrowDown') {
                event.preventDefault();
                if (currentRating <= 0.5) {
                    resetRating();
                } else {
                    setRating(currentRating - 0.5);
                }
            }
        });
    }

    document.addEventListener('click', function (event) {
        var trigger = event.target.closest('[data-open-review-modal]');
        if (!trigger) {
            return;
        }
        event.preventDefault();
        openModal(trigger);
    });

    $$('.review-modal [data-close-modal]').forEach(function (element) {
        element.addEventListener('click', closeModal);
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            closeModal();
        }
    });

    if (modelYearInput) modelYearInput.addEventListener('input', function () { clearValidity(modelYearInput); });
    if (mileageInput) mileageInput.addEventListener('input', function () { clearValidity(mileageInput); });
    Array.prototype.slice.call(form.querySelectorAll('[required]')).forEach(function (input) {
        input.addEventListener('input', function () {
            validateRequiredField(input);
        });
    });

    form.addEventListener('submit', function (event) {
        if (!validateRating()) {
            event.preventDefault();
            return;
        }
        if (!validateRequiredFields()) {
            event.preventDefault();
            form.reportValidity();
            return;
        }
        if (!validateNumerics()) {
            event.preventDefault();
            form.reportValidity();
        }
    });

    renderStars(0);
}());
