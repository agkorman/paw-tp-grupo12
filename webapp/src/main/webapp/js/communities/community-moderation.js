(function () {
    'use strict';

    function setModalOpen(modal, open) {
        modal.hidden = !open;
        document.body.classList.toggle('modal-open', open);
    }

    function clearError(reasonField, errorNode) {
        reasonField.classList.remove('is-invalid');
        reasonField.removeAttribute('aria-invalid');
        if (errorNode) {
            errorNode.textContent = '';
            errorNode.hidden = true;
        }
    }

    function showError(reasonField, errorNode, message) {
        reasonField.classList.add('is-invalid');
        reasonField.setAttribute('aria-invalid', 'true');
        if (errorNode) {
            errorNode.textContent = message || '';
            errorNode.hidden = false;
        }
    }

    function validateReason(form, reasonField) {
        var value = reasonField.value.trim();
        if (!value) {
            return form.dataset.reasonRequiredMessage;
        }
        if (value.length < 10) {
            return form.dataset.reasonMinMessage;
        }
        if (value.length > 600) {
            return form.dataset.reasonMaxMessage;
        }
        return null;
    }

    function getModalParts(modal) {
        return {
            form: modal.querySelector('[data-community-hide-form]'),
            reasonField: modal.querySelector('[data-community-hide-reason]'),
            errorNode: modal.querySelector('[data-community-hide-error]')
        };
    }

    function openModal(button) {
        var modal = document.getElementById(button.getAttribute('data-community-hide-modal-target') || '');
        if (!modal) {
            return;
        }
        var parts = getModalParts(modal);
        if (!parts.form || !parts.reasonField) {
            return;
        }
        parts.form.action = button.getAttribute('data-community-hide-action') || '';
        parts.reasonField.value = '';
        clearError(parts.reasonField, parts.errorNode);
        setModalOpen(modal, true);
        parts.reasonField.focus();
    }

    function closeModal(modal) {
        var parts = getModalParts(modal);
        if (parts.form) {
            parts.form.removeAttribute('action');
        }
        if (parts.reasonField) {
            clearError(parts.reasonField, parts.errorNode);
        }
        setModalOpen(modal, false);
    }

    document.addEventListener('click', function (event) {
        var openButton = event.target.closest('[data-open-community-hide-modal]');
        if (openButton) {
            event.preventDefault();
            openModal(openButton);
            return;
        }

        var closeTarget = event.target.closest('[data-close-community-hide-modal]');
        if (closeTarget) {
            event.preventDefault();
            var modal = closeTarget.closest('[data-community-hide-modal]');
            if (modal) {
                closeModal(modal);
            }
        }
    });

    document.addEventListener('input', function (event) {
        var reasonField = event.target.closest('[data-community-hide-reason]');
        if (!reasonField) {
            return;
        }
        var modal = reasonField.closest('[data-community-hide-modal]');
        var parts = getModalParts(modal);
        clearError(parts.reasonField, parts.errorNode);
    });

    document.addEventListener('submit', function (event) {
        var form = event.target.closest('[data-community-hide-form]');
        if (!form) {
            return;
        }
        var modal = form.closest('[data-community-hide-modal]');
        var parts = getModalParts(modal);
        clearError(parts.reasonField, parts.errorNode);

        var validationError = validateReason(form, parts.reasonField);
        if (validationError) {
            event.preventDefault();
            showError(parts.reasonField, parts.errorNode, validationError);
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Escape') {
            return;
        }
        document.querySelectorAll('[data-community-hide-modal]:not([hidden])').forEach(closeModal);
    });
})();
