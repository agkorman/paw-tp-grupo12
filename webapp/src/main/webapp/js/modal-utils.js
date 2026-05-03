(function () {
    window.PawModal = window.PawModal || {};

    function closest(node, predicate) {
        while (node && node !== document) {
            if (node.nodeType === 1 && predicate(node)) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function createController(options) {
        var modal = options.modal;
        var bodyClass = options.bodyClass || 'modal-open';
        var modalSelector = options.modalSelector || '.review-modal';
        var lastTrigger = null;

        function hasOpenModal() {
            return document.querySelectorAll(modalSelector + ':not([hidden])').length > 0;
        }

        function open(trigger, focusTarget) {
            if (!modal) {
                return;
            }
            lastTrigger = trigger || null;
            modal.removeAttribute('hidden');
            document.body.classList.add(bodyClass);
            if (focusTarget && typeof focusTarget.focus === 'function') {
                focusTarget.focus();
            }
        }

        function close() {
            if (!modal) {
                return;
            }
            modal.setAttribute('hidden', 'hidden');
            if (!hasOpenModal()) {
                document.body.classList.remove(bodyClass);
            }
            if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
                lastTrigger.focus();
            }
            lastTrigger = null;
        }

        function isOpen() {
            return modal && !modal.hasAttribute('hidden');
        }

        return {
            open: open,
            close: close,
            isOpen: isOpen
        };
    }

    window.PawModal.closest = closest;
    window.PawModal.createController = createController;
}());
