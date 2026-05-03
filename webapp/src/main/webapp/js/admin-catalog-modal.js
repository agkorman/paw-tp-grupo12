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

    var acceptSuccessMsg = modal.getAttribute('data-accept-success-msg') || '';
    var rejectSuccessMsg = modal.getAttribute('data-reject-success-msg') || '';
    var errorMsg = modal.getAttribute('data-error-msg') || '';

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

    var lastTrigger = null;

    function findOpenTrigger(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-open-admin-catalog-request')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function findCloseAncestor(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-close-admin-catalog-request-modal')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
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

        lastTrigger = trigger;
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
    }

    function closeModal() {
        modal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    function removeTriggerCard(trigger) {
        if (trigger && trigger.parentNode) {
            trigger.parentNode.removeChild(trigger);
        }
    }

    function showToast(message, type) {
        if (window.PawToast && typeof window.PawToast.show === 'function') {
            window.PawToast.show(message, type);
        }
    }

    function submitAjax(form, successMsg) {
        var trigger = lastTrigger;
        var action = form.getAttribute('action');
        var data = new FormData(form);
        closeModal();
        fetch(action, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            body: data
        }).then(function (res) {
            if (res.ok) {
                removeTriggerCard(trigger);
                showToast(successMsg, 'success');
            } else {
                showToast(errorMsg, 'error');
            }
        }).catch(function () {
            showToast(errorMsg, 'error');
        });
    }

    if (acceptForm) {
        acceptForm.addEventListener('submit', function (e) {
            e.preventDefault();
            submitAjax(acceptForm, acceptSuccessMsg);
        });
    }

    if (rejectForm) {
        rejectForm.addEventListener('submit', function (e) {
            e.preventDefault();
            submitAjax(rejectForm, rejectSuccessMsg);
        });
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
        if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            closeModal();
        }
    });
}());
