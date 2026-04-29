(function () {
    var modals = {
        brand: document.getElementById('requestBrandModal'),
        'body-type': document.getElementById('requestBodyTypeModal')
    };

    var lastTrigger = null;

    function openModal(key) {
        var modal = modals[key];
        if (!modal) {
            return;
        }
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
        var firstField = modal.querySelector('input[type="text"], textarea');
        if (firstField && typeof firstField.focus === 'function') {
            firstField.focus();
        }
    }

    function closeModal(modal) {
        if (!modal) {
            return;
        }
        modal.setAttribute('hidden', 'hidden');
        var anyOpen = Object.keys(modals).some(function (k) {
            return modals[k] && !modals[k].hasAttribute('hidden');
        });
        var createCarModal = document.getElementById('createCarModal');
        var createCarOpen = createCarModal && !createCarModal.hasAttribute('hidden');
        if (!anyOpen && !createCarOpen) {
            document.body.classList.remove('modal-open');
        }
        if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    function findOpenTrigger(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-open-catalog-request')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function findCloseAncestor(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-close-catalog-request-modal')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function modalOf(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.classList && node.classList.contains('catalog-request-modal')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    document.addEventListener('click', function (event) {
        var openTrigger = findOpenTrigger(event.target);
        if (openTrigger) {
            event.preventDefault();
            lastTrigger = openTrigger;
            openModal(openTrigger.getAttribute('data-open-catalog-request'));
            return;
        }

        var closeTrigger = findCloseAncestor(event.target);
        if (closeTrigger) {
            event.preventDefault();
            closeModal(modalOf(closeTrigger));
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Escape') {
            return;
        }
        Object.keys(modals).forEach(function (key) {
            var modal = modals[key];
            if (modal && !modal.hasAttribute('hidden')) {
                closeModal(modal);
            }
        });
    });
}());
