(function () {
    var page = document.getElementById('createCarFormPage');
    var form = document.getElementById('createCarForm');

    if (!page || !form) {
        return;
    }

    var messages = page.dataset;
    var formatMessage = function (template) {
        var args = Array.prototype.slice.call(arguments, 1);
        return (template || '').replace(/\{(\d+)}/g, function (match, index) {
            return args[index] == null ? match : args[index];
        });
    };
    var requiredMessages = {
        modalCarBrand: messages.msgRequiredBrand,
        modalCarBodyType: messages.msgRequiredBodyType,
        modalCarModel: messages.msgRequiredModel,
        modalCarYear: messages.msgRequiredYear,
        modalCarDescription: messages.msgRequiredDescription,
        modalCarHorsepower: messages.msgRequiredHorsepower,
        modalCarAirbagCount: messages.msgRequiredAirbags,
        modalCarFuelConsumption: messages.msgRequiredConsumption,
        modalCarMaxSpeed: messages.msgRequiredMaxSpeed,
        modalCarPrice: messages.msgRequiredPrice
    };

    var EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    var isNumericField = function (field) {
        return field && field.hasAttribute('data-number-field');
    };

    var numericKind = function (field) {
        return field.getAttribute('data-number-field') === 'decimal' ? 'decimal' : 'integer';
    };

    var normalizedNumericText = function (field) {
        var value = (field.value || '').trim();
        return numericKind(field) === 'decimal' ? value.replace(',', '.') : value;
    };

    var isCompleteNumericText = function (field, value) {
        return numericKind(field) === 'decimal' ? /^\d+(?:[.,]\d+)?$/.test(value) : /^\d+$/.test(value);
    };

    var isPartialNumericText = function (field, value) {
        return numericKind(field) === 'decimal' ? /^\d*(?:[.,]\d*)?$/.test(value) : /^\d*$/.test(value);
    };

    var numericInvalidMessage = function (field) {
        return field.getAttribute('data-msg-number-invalid') || messages.msgNumberInvalid;
    };

    var numericRangeMessage = function (field) {
        return field.getAttribute('data-msg-number-range') || '';
    };

    var numericBounds = function (field) {
        return {
            min: field.getAttribute('min') === null || field.getAttribute('min') === '' ? null : Number(field.getAttribute('min')),
            max: field.getAttribute('max') === null || field.getAttribute('max') === '' ? null : Number(field.getAttribute('max'))
        };
    };

    var wouldKeepNumericInputValid = function (field, insertedText) {
        var start = typeof field.selectionStart === 'number' ? field.selectionStart : field.value.length;
        var end = typeof field.selectionEnd === 'number' ? field.selectionEnd : field.value.length;
        var nextValue = field.value.slice(0, start) + insertedText + field.value.slice(end);
        return isPartialNumericText(field, nextValue.trim());
    };

    var fieldKey = function (field) {
        if (field.type === 'radio') {
            return field.name || field.id || 'field';
        }
        return field.id || field.name || 'field';
    };

    var fieldContainer = function (field) {
        var node = field;
        while (node && node !== page) {
            if (node.classList && node.classList.contains('modal-field')) {
                return node;
            }
            node = node.parentNode;
        }
        return field ? field.parentNode : form;
    };

    var clientErrorId = function (field) {
        return fieldKey(field) + 'ClientError';
    };

    var setDescribedBy = function (field, errorId) {
        var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (ids.indexOf(errorId) === -1) {
            ids.push(errorId);
            field.setAttribute('aria-describedby', ids.join(' '));
        }
    };

    var removeDescribedBy = function (field, errorId) {
        var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
            return id && id !== errorId;
        });
        if (ids.length) {
            field.setAttribute('aria-describedby', ids.join(' '));
        } else {
            field.removeAttribute('aria-describedby');
        }
    };

    var radioGroup = function (field) {
        return Array.prototype.slice.call(form.querySelectorAll('input[type="radio"][name="' + field.name + '"]'));
    };

    var setInlineError = function (field, message) {
        if (!field) {
            return;
        }
        var container = fieldContainer(field);
        var key = fieldKey(field);
        var errorId = clientErrorId(field);
        var error = container.querySelector('[data-client-error-for="' + key + '"]');
        if (!error) {
            error = document.createElement('span');
            error.id = errorId;
            error.className = 'form-error client-form-error';
            error.setAttribute('data-client-error-for', key);
            error.setAttribute('role', 'alert');
            container.appendChild(error);
        }
        error.textContent = message;
        error.hidden = false;

        if (field.type === 'radio') {
            radioGroup(field).forEach(function (radio) {
                radio.setAttribute('aria-invalid', 'true');
                setDescribedBy(radio, errorId);
            });
        } else {
            field.classList.add('is-invalid');
            field.setAttribute('aria-invalid', 'true');
            setDescribedBy(field, errorId);
        }
    };

    var clearInlineError = function (field) {
        if (!field) {
            return;
        }
        var container = fieldContainer(field);
        var key = fieldKey(field);
        var errorId = clientErrorId(field);
        var error = container.querySelector('[data-client-error-for="' + key + '"]');
        if (error) {
            error.textContent = '';
            error.hidden = true;
        }

        if (field.type === 'radio') {
            radioGroup(field).forEach(function (radio) {
                radio.removeAttribute('aria-invalid');
                removeDescribedBy(radio, errorId);
            });
        } else {
            field.classList.remove('is-invalid');
            field.removeAttribute('aria-invalid');
            removeDescribedBy(field, errorId);
        }
    };

    var hasRequiredRadioGroup = function (field) {
        return field.type === 'radio' && radioGroup(field).some(function (radio) {
            return radio.required;
        });
    };

    var isMissingRequiredValue = function (field) {
        return !field.value || field.value.trim() === '';
    };

    var validateField = function (field) {
        if (!field || field.disabled) {
            return true;
        }

        clearInlineError(field);

        if ((field.required || hasRequiredRadioGroup(field)) && isMissingRequiredValue(field)) {
            setInlineError(field, requiredMessages[field.id] || messages.msgRequiredGeneric);
            return false;
        }

        if (field.type === 'radio' && hasRequiredRadioGroup(field)
                && !radioGroup(field).some(function (radio) { return radio.checked; })) {
            setInlineError(field, messages.msgRadioRequired);
            return false;
        }

        if (field.type === 'email' && field.value && !EMAIL_PATTERN.test(field.value.trim())) {
            setInlineError(field, messages.msgEmailInvalid);
            return false;
        }

        if (isNumericField(field) && field.value) {
            var numericText = normalizedNumericText(field);
            if (!isCompleteNumericText(field, field.value.trim())) {
                setInlineError(field, numericInvalidMessage(field));
                return false;
            }

            var parsed = Number(numericText);
            if (!Number.isFinite(parsed)) {
                setInlineError(field, numericInvalidMessage(field));
                return false;
            }
            var bounds = numericBounds(field);
            if (bounds.min !== null && parsed < bounds.min) {
                setInlineError(field, numericRangeMessage(field) || formatMessage(messages.msgNumberMin, field.min));
                return false;
            }
            if (bounds.max !== null && parsed > bounds.max) {
                setInlineError(field, numericRangeMessage(field) || formatMessage(messages.msgNumberMax, field.max));
                return false;
            }
            field.value = numericText;
        }

        return true;
    };

    // The file input is owned by the shared image picker (js/shared/image-upload-picker.js).
    var imagePicker = function () {
        return window.ImageUploadPicker ? window.ImageUploadPicker.get('modalCar') : null;
    };

    var validateFields = function () {
        var seenRadioGroups = {};
        var fieldsValid = Array.prototype.slice.call(form.querySelectorAll('input, textarea, select')).reduce(function (isValid, field) {
            if (field.type === 'hidden' || field.type === 'file') {
                return isValid;
            }
            if (field.type === 'radio') {
                if (seenRadioGroups[field.name]) {
                    return isValid;
                }
                seenRadioGroups[field.name] = true;
            }
            return validateField(field) && isValid;
        }, true);
        var picker = imagePicker();
        var imageValid = picker ? picker.validate() : true;
        return fieldsValid && imageValid;
    };

    form.noValidate = true;

    Array.prototype.slice.call(form.querySelectorAll('input, textarea, select')).forEach(function (field) {
        if (field.type === 'hidden' || field.type === 'file') {
            return;
        }
        var eventName = field.tagName === 'SELECT' || field.type === 'radio' ? 'change' : 'input';
        if (isNumericField(field)) {
            field.addEventListener('beforeinput', function (event) {
                if (!event.data || event.inputType === 'deleteContentBackward' || event.inputType === 'deleteContentForward') {
                    return;
                }
                if (!wouldKeepNumericInputValid(field, event.data)) {
                    event.preventDefault();
                }
            });
            field.addEventListener('paste', function (event) {
                var pasted = event.clipboardData ? event.clipboardData.getData('text') : '';
                if (pasted && !wouldKeepNumericInputValid(field, pasted)) {
                    event.preventDefault();
                }
            });
            field.addEventListener('blur', function () {
                if (field.value.trim() !== '') {
                    validateField(field);
                }
            });
        }
        field.addEventListener(eventName, function () {
            validateField(field);
        });
    });

    form.addEventListener('submit', function (event) {
        if (!validateFields()) {
            event.preventDefault();
            var invalid = form.querySelector('[aria-invalid="true"]');
            if (invalid && typeof invalid.focus === 'function') {
                invalid.focus();
            }
        }
    });
})();
