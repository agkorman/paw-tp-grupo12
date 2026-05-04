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
        var modalSelector = options.modalSelector || '.modal';
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

    function bindCloseAttr(modal, closeFn) {
        var id = modal.id;
        document.addEventListener('click', function (event) {
            var el = closest(event.target, function (node) {
                return node.getAttribute('data-close-modal') === id;
            });
            if (el) {
                closeFn();
            }
        });
    }

    function bindEscKey(modal, closeFn) {
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
                closeFn();
            }
        });
    }

    window.PawModal.closest = closest;
    window.PawModal.createController = createController;
    window.PawModal.bindCloseAttr = bindCloseAttr;
    window.PawModal.bindEscKey = bindEscKey;
}());
