(function () {
    function message(form, key) {
        return form.getAttribute('data-msg-' + key) || '';
    }

    function fieldContainer(input) {
        var node = input;
        while (node && node.nodeType === 1) {
            if (node.classList && node.classList.contains('modal-field')) {
                return node;
            }
            node = node.parentNode;
        }
        return input ? input.parentNode : document.body;
    }

    function setInlineError(input, message) {
        var container = fieldContainer(input);
        var error = container.querySelector('[data-client-error-for="' + input.id + '"]');
        var errorId = input.id + 'ClientError';
        var describedBy;

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
        describedBy = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (describedBy.indexOf(errorId) === -1) {
            describedBy.push(errorId);
            input.setAttribute('aria-describedby', describedBy.join(' '));
        }
    }

    function clearInlineError(input) {
        var container = fieldContainer(input);
        var errorId = input.id + 'ClientError';
        var error = container.querySelector('[data-client-error-for="' + input.id + '"]');
        var describedBy;

        if (error) {
            error.textContent = '';
            error.hidden = true;
        }
        input.classList.remove('is-invalid');
        input.removeAttribute('aria-invalid');
        describedBy = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
            return id && id !== errorId;
        });
        if (describedBy.length) {
            input.setAttribute('aria-describedby', describedBy.join(' '));
        } else {
            input.removeAttribute('aria-describedby');
        }
    }

    function validateField(input) {
        var value = input.value ? input.value.trim() : '';
        var maxLength = input.getAttribute('maxlength');

        clearInlineError(input);
        if (input.required && value.length === 0) {
            setInlineError(input, message(input.form, 'required-generic'));
            return false;
        }
        if (maxLength && input.value.length > Number(maxLength)) {
            setInlineError(input, message(input.form, 'length-max').replace('{0}', maxLength));
            return false;
        }
        return true;
    }

    function validateForm(form) {
        return Array.prototype.slice.call(form.querySelectorAll('input:not([type="hidden"]), textarea')).reduce(function (valid, input) {
            return validateField(input) && valid;
        }, true);
    }

    function focusFirstInvalid(form) {
        var invalid = form.querySelector('[aria-invalid="true"]');
        if (invalid && typeof invalid.focus === 'function') {
            invalid.focus();
        }
    }

    Array.prototype.slice.call(document.querySelectorAll('[data-catalog-request-form]')).forEach(function (form) {
        form.noValidate = true;
        Array.prototype.slice.call(form.querySelectorAll('input:not([type="hidden"]), textarea')).forEach(function (input) {
            input.addEventListener('input', function () {
                validateField(input);
            });
        });
        form.addEventListener('submit', function (event) {
            if (!validateForm(form)) {
                event.preventDefault();
                focusFirstInvalid(form);
            }
        });
    });
}());
