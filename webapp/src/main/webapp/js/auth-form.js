(function () {
    var EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    var USERNAME_PATTERN = /^[A-Za-z0-9._-]+$/;

    function formatMessage(template, value) {
        return (template || '').replace('{0}', value);
    }

    function dataKeyForInput(prefix, input) {
        return prefix + input.id.charAt(0).toLowerCase() + input.id.slice(1).replace(/[A-Z]/g, function (letter) {
            return '-' + letter.toLowerCase();
        });
    }

    function message(form, key) {
        return form.getAttribute('data-msg-' + key) || '';
    }

    function requiredMessage(input, form) {
        return message(form, dataKeyForInput('required-', input)) || message(form, 'required-generic');
    }

    function fieldContainer(input) {
        var node = input;
        while (node && node.nodeType === 1) {
            if (node.classList && node.classList.contains('auth-field')) {
                return node;
            }
            node = node.parentNode;
        }
        return input ? input.parentNode : document.body;
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

    function normalizedValue(input) {
        return input && input.value ? input.value.trim() : '';
    }

    function validateRequired(input, form) {
        if (input.required && normalizedValue(input) === '') {
            setInlineError(input, requiredMessage(input, form));
            return false;
        }
        return true;
    }

    function validateLength(input, form) {
        var value = input.value || '';
        var minLength = input.getAttribute('minlength');
        var maxLength = input.getAttribute('maxlength');
        if (minLength && value.length > 0 && value.length < Number(minLength)) {
            setInlineError(input, formatMessage(message(form, 'length-min'), minLength));
            return false;
        }
        if (maxLength && value.length > Number(maxLength)) {
            setInlineError(input, formatMessage(message(form, 'length-max'), maxLength));
            return false;
        }
        return true;
    }

    function validateField(input, form) {
        if (!input || input.disabled) {
            return true;
        }

        clearInlineError(input);

        if (!validateRequired(input, form)) {
            return false;
        }

        var value = normalizedValue(input);
        if (input.type === 'email' && value.length > 0 && !EMAIL_PATTERN.test(value)) {
            setInlineError(input, message(form, 'email-invalid'));
            return false;
        }

        if (input.id === 'registerUsername' && value.length > 0 && !USERNAME_PATTERN.test(value)) {
            setInlineError(input, message(form, 'username-pattern'));
            return false;
        }

        if (!validateLength(input, form)) {
            return false;
        }

        if (input.id === 'registerConfirmPassword') {
            var password = form.querySelector('#registerPassword');
            if (password && input.value.length > 0 && input.value !== password.value) {
                setInlineError(input, message(form, 'password-match'));
                return false;
            }
        }

        return true;
    }

    function focusFirstInvalid(form) {
        var invalid = form.querySelector('[aria-invalid="true"]');
        if (invalid && typeof invalid.focus === 'function') {
            invalid.focus();
        }
    }

    function validateForm(form) {
        return Array.prototype.slice.call(form.querySelectorAll('input')).reduce(function (isValid, input) {
            if (input.type === 'hidden' || input.type === 'checkbox') {
                return isValid;
            }
            return validateField(input, form) && isValid;
        }, true);
    }

    function install(form) {
        form.noValidate = true;

        Array.prototype.slice.call(form.querySelectorAll('input')).forEach(function (input) {
            if (input.type === 'hidden' || input.type === 'checkbox') {
                return;
            }
            input.addEventListener('input', function () {
                validateField(input, form);
                if (input.id === 'registerPassword') {
                    var confirm = form.querySelector('#registerConfirmPassword');
                    if (confirm && confirm.value.length > 0) {
                        validateField(confirm, form);
                    }
                }
            });
        });

        form.addEventListener('submit', function (event) {
            if (!validateForm(form)) {
                event.preventDefault();
                focusFirstInvalid(form);
            }
        });
    }

    Array.prototype.slice.call(document.querySelectorAll('form[data-auth-form]')).forEach(install);
}());
