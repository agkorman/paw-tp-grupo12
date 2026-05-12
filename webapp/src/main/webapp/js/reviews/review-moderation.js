(function () {
    'use strict';

    var modal = document.querySelector('[data-hide-review-modal]');
    var form = document.getElementById('hideReviewForm');
    var reasonField = document.getElementById('hideReviewReason');
    var redirectField = form ? form.querySelector('[data-hide-review-redirect]') : null;
    var errorNode = modal ? modal.querySelector('[data-hide-review-error]') : null;

    if (!modal || !form || !reasonField) {
        return;
    }

    function setModalOpen(open) {
        modal.hidden = !open;
        document.body.classList.toggle('modal-open', open);
        if (open) {
            reasonField.focus();
        }
    }

    function clearError() {
        reasonField.classList.remove('is-invalid');
        if (errorNode) {
            errorNode.textContent = '';
            errorNode.hidden = true;
        }
    }

    function showError(message) {
        reasonField.classList.add('is-invalid');
        if (errorNode) {
            errorNode.textContent = message || '';
            errorNode.hidden = false;
        }
    }

    function validateReason() {
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

    function openModal(button) {
        form.action = button.getAttribute('data-review-hide-action');
        if (redirectField) {
            redirectField.value = button.getAttribute('data-review-hide-redirect') || '';
        }
        reasonField.value = '';
        clearError();
        setModalOpen(true);
    }

    function closeModal() {
        setModalOpen(false);
        form.removeAttribute('action');
        clearError();
    }

    document.addEventListener('click', function (event) {
        var openButton = event.target.closest('[data-open-hide-review-modal]');
        if (openButton) {
            event.preventDefault();
            openModal(openButton);
            return;
        }

        if (event.target.closest('[data-close-hide-review-modal]')) {
            event.preventDefault();
            closeModal();
        }
    });

    reasonField.addEventListener('input', clearError);

    form.addEventListener('submit', function (event) {
        clearError();

        var validationError = validateReason();
        if (validationError) {
            event.preventDefault();
            showError(validationError);
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) {
            closeModal();
        }
    });
})();
