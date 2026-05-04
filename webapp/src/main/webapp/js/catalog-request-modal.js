(function () {
    var modals = {
        brand: document.getElementById('requestBrandModal'),
        'body-type': document.getElementById('requestBodyTypeModal')
    };
    var controllers = {
        brand: window.PawModal.createController({ modal: modals.brand }),
        'body-type': window.PawModal.createController({ modal: modals['body-type'] })
    };

    function openModal(key, trigger) {
        var modal = modals[key];
        if (!modal) {
            return;
        }
        controllers[key].open(trigger, modal.querySelector('input[type="text"], textarea'));
    }

    function closeModal(modal) {
        Object.keys(modals).some(function (key) {
            if (modals[key] === modal) {
                controllers[key].close();
                return true;
            }
            return false;
        });
    }

    function findOpenTrigger(node) {
        return window.PawModal.closest(node, function (candidate) {
            return candidate.hasAttribute('data-open-catalog-request');
        });
    }

    function findCloseAncestor(node) {
        return window.PawModal.closest(node, function (candidate) {
            return candidate.hasAttribute('data-close-catalog-request-modal');
        });
    }

    function modalOf(node) {
        return window.PawModal.closest(node, function (candidate) {
            return candidate.classList.contains('catalog-request-modal');
        });
    }

    document.addEventListener('click', function (event) {
        var openTrigger = findOpenTrigger(event.target);
        if (openTrigger) {
            event.preventDefault();
            openModal(openTrigger.getAttribute('data-open-catalog-request'), openTrigger);
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
            if (modal && controllers[key].isOpen()) {
                controllers[key].close();
            }
        });
    });
}());
