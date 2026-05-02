(function () {
    var pendingForm = null;

    function setBodyModalState(open) {
        document.body.classList.toggle('profile-modal-open', open);
    }

    function openModal(modal, form) {
        if (!modal || !form) {
            return;
        }
        pendingForm = form;
        modal.hidden = false;
        setBodyModalState(true);

        var confirmButton = modal.querySelector('[data-confirmation-submit]');
        if (confirmButton && typeof confirmButton.focus === 'function') {
            confirmButton.focus();
        }
    }

    function closeModal(modal) {
        if (!modal) {
            return;
        }
        modal.hidden = true;
        pendingForm = null;
        setBodyModalState(document.querySelectorAll('.profile-modal:not([hidden])').length > 0);
    }

    function modalForForm(form) {
        var modalId = form ? form.getAttribute('data-confirm-modal') : null;
        return modalId ? document.getElementById(modalId) : null;
    }

    function onSubmit(event) {
        var form = event.target;
        if (!form || !form.getAttribute || !form.getAttribute('data-confirm-modal')) {
            return;
        }
        if (form.getAttribute('data-confirmed') === 'true') {
            form.removeAttribute('data-confirmed');
            return;
        }

        var modal = modalForForm(form);
        if (!modal) {
            return;
        }

        event.preventDefault();
        openModal(modal, form);
    }

    function onClick(event) {
        var closeButton = event.target.closest('[data-close-confirmation-modal]');
        if (closeButton) {
            event.preventDefault();
            closeModal(closeButton.closest('[data-confirmation-modal]'));
            return;
        }

        var confirmButton = event.target.closest('[data-confirmation-submit]');
        if (!confirmButton || !pendingForm) {
            return;
        }

        event.preventDefault();
        var form = pendingForm;
        closeModal(confirmButton.closest('[data-confirmation-modal]'));
        form.setAttribute('data-confirmed', 'true');
        if (typeof form.requestSubmit === 'function') {
            form.requestSubmit();
        } else {
            form.submit();
        }
    }

    function onKeydown(event) {
        if (event.key !== 'Escape') {
            return;
        }
        var openModalNode = document.querySelector('[data-confirmation-modal]:not([hidden])');
        if (openModalNode) {
            closeModal(openModalNode);
        }
    }

    document.addEventListener('submit', onSubmit);
    document.addEventListener('click', onClick);
    document.addEventListener('keydown', onKeydown);
})();
