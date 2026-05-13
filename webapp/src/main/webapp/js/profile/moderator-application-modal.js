(function () {
    var modal = document.getElementById('requestAdminModal');
    if (!modal) {
        return;
    }
    var form = document.getElementById('requestAdminForm');
    var lastTrigger = null;

    function fieldContainer(field) {
        var node = field;
        while (node && node.nodeType === 1) {
            if (node.classList && node.classList.contains('modal-field')) {
                return node;
            }
            node = node.parentNode;
        }
        return field ? field.parentNode : document.body;
    }

    function fieldKey(field) {
        if (!field || !field.name) {
            return 'generic';
        }
        return field.name.replace(/[^A-Za-z0-9_-]/g, '-');
    }

    function clientErrorId(field) {
        return field.id + 'ClientError';
    }

    function setDescribedBy(field, errorId) {
        if (!field || !errorId) {
            return;
        }
        var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (ids.indexOf(errorId) === -1) {
            ids.push(errorId);
            field.setAttribute('aria-describedby', ids.join(' '));
        }
    }

    function removeDescribedBy(field, errorId) {
        if (!field || !errorId) {
            return;
        }
        var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
            return id && id !== errorId;
        });
        if (ids.length) {
            field.setAttribute('aria-describedby', ids.join(' '));
        } else {
            field.removeAttribute('aria-describedby');
        }
    }

    function requiredMessage(field) {
        if (!form || !field) {
            return '';
        }
        return form.getAttribute('data-msg-required-' + fieldKey(field))
            || form.getAttribute('data-msg-required-generic')
            || '';
    }

    function setInlineError(field, message) {
        if (!field) {
            return;
        }
        var container = fieldContainer(field);
        var errorId = clientErrorId(field);
        var error = container.querySelector('[data-client-error-for="' + field.id + '"]');
        if (!error) {
            error = document.createElement('span');
            error.id = errorId;
            error.className = 'form-error client-form-error';
            error.setAttribute('data-client-error-for', field.id);
            error.setAttribute('role', 'alert');
            container.appendChild(error);
        }
        error.textContent = message;
        error.hidden = false;
        field.classList.add('is-invalid');
        field.setAttribute('aria-invalid', 'true');
        setDescribedBy(field, errorId);
    }

    function clearInlineError(field) {
        if (!field) {
            return;
        }
        var container = fieldContainer(field);
        var errorId = clientErrorId(field);
        var error = container.querySelector('[data-client-error-for="' + field.id + '"]');
        if (error) {
            error.textContent = '';
            error.hidden = true;
        }
        field.classList.remove('is-invalid');
        field.removeAttribute('aria-invalid');
        removeDescribedBy(field, errorId);
    }

    function normalizedValue(field) {
        return field && field.value ? field.value.trim() : '';
    }

    function formatMessage(template, value) {
        return (template || '').replace('{0}', value);
    }

    function validateField(field) {
        var maxLength;

        if (!field || field.disabled) {
            return true;
        }
        clearInlineError(field);
        if (field.required && normalizedValue(field) === '') {
            setInlineError(field, requiredMessage(field));
            return false;
        }
        maxLength = field.getAttribute('maxlength');
        if (maxLength && field.value.length > Number(maxLength)) {
            setInlineError(field, formatMessage(form.getAttribute('data-msg-length-max') || '', maxLength));
            return false;
        }
        return true;
    }

    function focusFirstInvalid() {
        var invalid = form ? form.querySelector('[aria-invalid="true"]') : null;
        if (invalid && typeof invalid.focus === 'function') {
            invalid.focus();
        }
    }

    function validateForm() {
        if (!form) {
            return true;
        }
        return Array.prototype.slice.call(form.querySelectorAll('textarea, input, select')).reduce(function (isValid, field) {
            if (field.type === 'hidden') {
                return isValid;
            }
            return validateField(field) && isValid;
        }, true);
    }

    function findOpenTrigger(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-open-request-admin-modal')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function findCloseAncestor(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-close-request-admin-modal')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function open(trigger) {
        lastTrigger = trigger;
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
        var first = modal.querySelector('textarea');
        if (first && typeof first.focus === 'function') {
            first.focus();
        }
    }

    function close() {
        modal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    if (form) {
        form.noValidate = true;
        Array.prototype.slice.call(form.querySelectorAll('textarea, input, select')).forEach(function (field) {
            if (field.type === 'hidden') {
                return;
            }
            field.addEventListener('input', function () {
                validateField(field);
            });
        });

        form.addEventListener('submit', function (event) {
            if (!validateForm()) {
                event.preventDefault();
                focusFirstInvalid();
            }
        });
    }

    document.addEventListener('click', function (event) {
        var openTrigger = findOpenTrigger(event.target);
        if (openTrigger) {
            event.preventDefault();
            open(openTrigger);
            return;
        }
        if (findCloseAncestor(event.target)) {
            event.preventDefault();
            close();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            close();
        }
    });
}());
