(function () {
    var lastTrigger = null;

    function closestByAttribute(element, attribute) {
        while (element && element !== document) {
            if (element.hasAttribute && element.hasAttribute(attribute)) {
                return element;
            }
            element = element.parentNode;
        }
        return null;
    }

    function getModalFromTrigger(trigger) {
        var modalId = trigger ? trigger.getAttribute('data-review-preview-target') : null;

        return modalId ? document.getElementById(modalId) : null;
    }

    function firstFocusable(modal) {
        return modal.querySelector('button, a, input, textarea, select, [tabindex]:not([tabindex="-1"])');
    }

    function closeOpenModal() {
        var modal = document.querySelector('.review-preview-modal:not([hidden])');

        if (!modal) {
            return;
        }

        modal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    function openModal(trigger) {
        var modal = getModalFromTrigger(trigger);
        var focusTarget;

        if (!modal) {
            return false;
        }

        closeOpenModal();
        lastTrigger = trigger;
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');

        focusTarget = firstFocusable(modal);
        if (focusTarget && typeof focusTarget.focus === 'function') {
            focusTarget.focus();
        }
        return true;
    }

    document.addEventListener('click', function (event) {
        var openTrigger = closestByAttribute(event.target, 'data-open-review-preview');
        var closeTrigger;

        if (openTrigger) {
            if (openModal(openTrigger)) {
                event.preventDefault();
            }
            return;
        }

        closeTrigger = closestByAttribute(event.target, 'data-close-review-preview');
        if (closeTrigger) {
            event.preventDefault();
            closeOpenModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeOpenModal();
        }
    });
}());
