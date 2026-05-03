(function () {
    var modal = document.getElementById('adminCatalogRequestModal');
    if (!modal) {
        return;
    }

    var kicker = document.getElementById('adminCatalogRequestKicker');
    var title = document.getElementById('adminCatalogRequestTitle');
    var nameField = document.getElementById('adminCatalogRequestName');
    var submitterField = document.getElementById('adminCatalogRequestSubmitter');
    var commentsField = document.getElementById('adminCatalogRequestComments');
    var rejectForm = document.getElementById('adminCatalogRejectForm');
    var acceptForm = document.getElementById('adminCatalogAcceptForm');
    var acceptNameInput = document.getElementById('adminCatalogAcceptName');
    var adminBaseUrl = (modal.getAttribute('data-admin-base-url') || '/admin').replace(/\/$/, '');
    var modalController = window.PawModal.createController({ modal: modal });

    var COPY = {
        'brand': {
            kicker: 'Solicitud de marca',
            title: 'Revisar marca solicitada',
            collection: 'brand-requests'
        },
        'body-type': {
            kicker: 'Solicitud de carrocería',
            title: 'Revisar carrocería solicitada',
            collection: 'body-type-requests'
        }
    };

    function findOpenTrigger(node) {
        return window.PawModal.closest(node, function (candidate) {
            return candidate.hasAttribute('data-open-admin-catalog-request');
        });
    }

    function findCloseAncestor(node) {
        return window.PawModal.closest(node, function (candidate) {
            return candidate.hasAttribute('data-close-admin-catalog-request-modal');
        });
    }

    function openModal(trigger) {
        var type = trigger.getAttribute('data-catalog-type') || '';
        var copy = COPY[type];
        if (!copy) {
            return;
        }

        var requestId = trigger.getAttribute('data-request-id') || '';
        var name = trigger.getAttribute('data-request-name') || '';
        var submitter = trigger.getAttribute('data-request-submitter') || '';
        var comments = trigger.getAttribute('data-request-comments') || '';

        if (kicker) {
            kicker.textContent = copy.kicker;
        }
        if (title) {
            title.textContent = copy.title;
        }
        if (nameField) {
            nameField.value = name;
        }
        if (submitterField) {
            submitterField.value = submitter;
        }
        if (commentsField) {
            commentsField.value = comments;
        }
        if (acceptNameInput) {
            acceptNameInput.value = name;
        }

        var basePath = adminBaseUrl + '/' + copy.collection + '/' + encodeURIComponent(requestId);
        if (acceptForm) {
            acceptForm.setAttribute('action', basePath + '/accept');
        }
        if (rejectForm) {
            rejectForm.setAttribute('action', basePath + '/reject');
        }

        modalController.open(trigger);
    }

    function closeModal() {
        modalController.close();
    }

    document.addEventListener('click', function (event) {
        var openTrigger = findOpenTrigger(event.target);
        if (openTrigger) {
            event.preventDefault();
            openModal(openTrigger);
            return;
        }
        if (findCloseAncestor(event.target)) {
            event.preventDefault();
            closeModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && modalController.isOpen()) {
            closeModal();
        }
    });
}());
