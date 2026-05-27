(function () {
    function $(id) { return document.getElementById(id); }
    function $$(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }

    var form = $('createReviewForm');
    var modal = $('createReviewFormPage');
    if (!modal || !form) {
        return;
    }

    var STAR_FILLED = '#ff5719';
    var STAR_EMPTY = '#2e2e2e';
    var starSlots = $$('.star-slot', modal);
    var starInput = $('modalRating');
    var starWrap = modal.querySelector('.star-rating');
    var starLabel = modal.querySelector('.star-rating-value');
    var currentRating = 0;

    var mileageInput = $('modalMileageKm');

    function dataKeyForInput(prefix, input) {
        return prefix + input.id.charAt(0).toLowerCase() + input.id.slice(1).replace(/[A-Z]/g, function (letter) {
            return '-' + letter.toLowerCase();
        });
    }

    function message(key) {
        return form.getAttribute('data-msg-' + key) || '';
    }

    function requiredMessage(input) {
        return message(dataKeyForInput('required-', input)) || message('required-generic');
    }

    function starTextFor(value) {
        if (value === 0) return form.getAttribute('data-rating-none') || '';
        if (value <= 1) return form.getAttribute('data-rating-bad') || '';
        if (value <= 2) return form.getAttribute('data-rating-fair') || '';
        if (value <= 3) return form.getAttribute('data-rating-good') || '';
        if (value <= 4) return form.getAttribute('data-rating-very-good') || '';
        return form.getAttribute('data-rating-excellent') || '';
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

    function syncRatingFromInput() {
        var raw = starInput ? starInput.value : '';
        var value = raw ? Number(raw) : 0;
        if (value > 0 && value <= 5) {
            setRating(value);
        } else {
            resetRating();
        }
    }

    function isInt(value) {
        return /^\d+$/.test(value);
    }

    function isPartialInt(value) {
        return /^\d*$/.test(value);
    }

    function wouldKeepIntegerInputValid(input, insertedText) {
        var start = typeof input.selectionStart === 'number' ? input.selectionStart : input.value.length;
        var end = typeof input.selectionEnd === 'number' ? input.selectionEnd : input.value.length;
        var nextValue = input.value.slice(0, start) + insertedText + input.value.slice(end);
        return isPartialInt(nextValue.trim());
    }

    function fieldContainer(input) {
        var node = input;
        while (node && node !== modal) {
            if (node.classList && node.classList.contains('modal-field')) {
                return node;
            }
            node = node.parentNode;
        }
        return input ? input.parentNode : form;
    }

    function clientErrorId(input) {
        return input.id + 'ClientError';
    }

    function setDescribedBy(input, errorId) {
        if (!input || !errorId) {
            return;
        }
        var ids = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (ids.indexOf(errorId) === -1) {
            ids.push(errorId);
            input.setAttribute('aria-describedby', ids.join(' '));
        }
    }

    function removeDescribedBy(input, errorId) {
        if (!input || !errorId) {
            return;
        }
        var ids = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
            return id && id !== errorId;
        });
        if (ids.length) {
            input.setAttribute('aria-describedby', ids.join(' '));
        } else {
            input.removeAttribute('aria-describedby');
        }
    }

    function setInlineError(input, message) {
        if (!input) {
            return;
        }
        var container = fieldContainer(input);
        var errorId = clientErrorId(input);
        var error = container.querySelector('[data-client-error-for="' + input.id + '"]');
        if (!error) {
            error = document.createElement('span');
            error.id = errorId;
            error.className = 'form-error client-form-error';
            error.setAttribute('data-client-error-for', input.id);
            error.setAttribute('role', 'alert');
            container.appendChild(error);
        }
        error.textContent = message;
        error.hidden = false;
        input.classList.add('is-invalid');
        input.setAttribute('aria-invalid', 'true');
        setDescribedBy(input, errorId);
    }

    function clearInlineError(input) {
        if (!input) {
            return;
        }
        var container = fieldContainer(input);
        var errorId = clientErrorId(input);
        var error = container.querySelector('[data-client-error-for="' + input.id + '"]');
        if (error) {
            error.textContent = '';
            error.hidden = true;
        }
        input.classList.remove('is-invalid');
        input.removeAttribute('aria-invalid');
        removeDescribedBy(input, errorId);
    }

    function setRatingError(message) {
        if (starWrap) {
            starWrap.classList.add('is-invalid');
            starWrap.setAttribute('aria-invalid', 'true');
        }
        if (starLabel) {
            starLabel.textContent = message;
            starLabel.style.color = '#ef9a9a';
        }
    }

    function clearRatingError() {
        if (starWrap) {
            starWrap.classList.remove('is-invalid');
            starWrap.removeAttribute('aria-invalid');
        }
        if (starLabel) {
            starLabel.style.color = '';
        }
    }

    function validateRating() {
        if (!starInput || !starInput.value) {
            setRatingError(message('required-rating'));
            if (starWrap) {
                starWrap.focus();
            }
            return false;
        }
        clearRatingError();
        return true;
    }

    function focusFirstInvalid() {
        var invalid = form.querySelector('[aria-invalid="true"]:not([type="hidden"])');
        if (invalid && typeof invalid.focus === 'function') {
            invalid.focus();
        }
    }

    function clearAllClientErrors() {
        var errorIds = Array.prototype.slice.call(form.querySelectorAll('.client-form-error')).map(function (error) {
            return error.id;
        }).filter(Boolean);
        Array.prototype.slice.call(form.querySelectorAll('.client-form-error')).forEach(function (error) {
            error.textContent = '';
            error.hidden = true;
        });
        Array.prototype.slice.call(form.querySelectorAll('[aria-invalid="true"]')).forEach(function (input) {
            input.classList.remove('is-invalid');
            input.removeAttribute('aria-invalid');
        });
        Array.prototype.slice.call(form.querySelectorAll('[aria-describedby]')).forEach(function (input) {
            var ids = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
                return id && errorIds.indexOf(id) === -1;
            });
            if (ids.length) {
                input.setAttribute('aria-describedby', ids.join(' '));
            } else {
                input.removeAttribute('aria-describedby');
            }
        });
    }

    function validateRequiredField(input) {
        if (!input) {
            return true;
        }

        clearInlineError(input);
        if (input.required && (!input.value || input.value.trim() === '')) {
            setInlineError(input, requiredMessage(input));
            return false;
        }
        return true;
    }

    function validateRequiredFields() {
        return Array.prototype.slice.call(form.querySelectorAll('[required]')).reduce(function (isValid, input) {
            return validateRequiredField(input) && isValid;
        }, true);
    }

    function validateNumerics() {
        var ok = true;
        clearInlineError(mileageInput);

        if (mileageInput) {
            var mileage = mileageInput.value.trim();
            mileageInput.value = mileage;
            if (mileage.length > 0) {
                if (!isInt(mileage)) {
                    setInlineError(mileageInput, message('mileage-numeric'));
                    ok = false;
                } else {
                    var parsedMileage = Number(mileage);
                    if (parsedMileage < 0 || parsedMileage > 2000000) {
                        setInlineError(mileageInput, message('mileage-range'));
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
            clearRatingError();
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
            clearRatingError();
        });
    }

    form.noValidate = true;

    if (mileageInput) {
        mileageInput.addEventListener('beforeinput', function (event) {
            if (!event.data || event.inputType === 'deleteContentBackward' || event.inputType === 'deleteContentForward') {
                return;
            }
            if (!wouldKeepIntegerInputValid(mileageInput, event.data)) {
                event.preventDefault();
            }
        });
        mileageInput.addEventListener('paste', function (event) {
            var pasted = event.clipboardData ? event.clipboardData.getData('text') : '';
            if (pasted && !wouldKeepIntegerInputValid(mileageInput, pasted)) {
                event.preventDefault();
            }
        });
        mileageInput.addEventListener('input', function () {
            validateNumerics();
            if (mileageInput.value.trim() === '') {
                clearInlineError(mileageInput);
            }
        });
        mileageInput.addEventListener('blur', function () {
            validateNumerics();
            if (mileageInput.value.trim() === '') {
                clearInlineError(mileageInput);
            }
        });
    }
    Array.prototype.slice.call(form.querySelectorAll('[required]')).forEach(function (input) {
        input.addEventListener('input', function () {
            validateRequiredField(input);
        });
    });

    function validateImages() {
        var picker = window.ImageUploadPicker ? window.ImageUploadPicker.get('review') : null;
        return picker ? picker.validate() : true;
    }

    form.addEventListener('submit', function (event) {
        clearAllClientErrors();
        if (!validateRating()) {
            event.preventDefault();
            return;
        }
        var requiredOk = validateRequiredFields();
        var numericsOk = validateNumerics();
        var imagesOk = validateImages();
        if (!requiredOk || !numericsOk || !imagesOk) {
            event.preventDefault();
            focusFirstInvalid();
        }
    });

    syncRatingFromInput();
}());
