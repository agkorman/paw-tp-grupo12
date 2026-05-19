(function () {
    'use strict';

    var MAX_LENGTH = 1000;

    function findErrorNode(form) {
        return form.querySelector('[data-reply-error]');
    }

    function findTextarea(form) {
        return form.querySelector('textarea[name="body"]');
    }

    function clearError(form) {
        var textarea = findTextarea(form);
        var errorNode = findErrorNode(form);
        if (textarea) {
            textarea.classList.remove('is-invalid');
            textarea.removeAttribute('aria-invalid');
        }
        if (errorNode) {
            errorNode.textContent = '';
            errorNode.hidden = true;
        }
    }

    function showError(form, message) {
        var textarea = findTextarea(form);
        var errorNode = findErrorNode(form);
        if (textarea) {
            textarea.classList.add('is-invalid');
            textarea.setAttribute('aria-invalid', 'true');
        }
        if (errorNode) {
            errorNode.textContent = message || '';
            errorNode.hidden = false;
        }
    }

    function validate(form) {
        var textarea = findTextarea(form);
        if (!textarea) {
            return null;
        }
        var value = textarea.value.trim();
        if (!value) {
            return form.dataset.replyRequiredMessage || '';
        }
        if (value.length > MAX_LENGTH) {
            return form.dataset.replyMaxMessage || '';
        }
        return null;
    }

    var forms = document.querySelectorAll('.review-reply-form');
    forms.forEach(function (form) {
        var textarea = findTextarea(form);
        if (textarea) {
            textarea.addEventListener('input', function () {
                clearError(form);
            });
        }
        form.addEventListener('submit', function (event) {
            clearError(form);
            var error = validate(form);
            if (error) {
                event.preventDefault();
                showError(form, error);
            }
        });
    });
})();
