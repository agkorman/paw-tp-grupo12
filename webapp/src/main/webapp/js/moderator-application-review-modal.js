(function () {
    var modal = document.getElementById('adminRequestReviewModal');
    if (!modal) {
        return;
    }

    var submitterField = document.getElementById('adminRequestReviewSubmitter');
    var motivationField = document.getElementById('adminRequestReviewMotivation');
    var bioField = document.getElementById('adminRequestReviewBio');
    var justificationField = document.getElementById('adminRequestReviewJustification');
    var acceptForm = document.getElementById('adminRequestAcceptForm');
    var rejectForm = document.getElementById('adminRequestRejectForm');
    var adminBaseUrl = (modal.getAttribute('data-admin-base-url') || '/admin').replace(/\/$/, '');

    var lastTrigger = null;

    function findOpenTrigger(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-open-admin-request-review')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function findCloseAncestor(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-close-admin-request-review-modal')) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function open(trigger) {
        var requestId = trigger.getAttribute('data-request-id') || '';
        var submitter = trigger.getAttribute('data-request-submitter') || '';
        var motivation = trigger.getAttribute('data-request-motivation') || '';
        var bio = trigger.getAttribute('data-request-bio') || '';
        var justification = trigger.getAttribute('data-request-justification') || '';

        if (submitterField) {
            submitterField.textContent = submitter;
        }
        if (motivationField) {
            motivationField.value = motivation;
        }
        if (bioField) {
            bioField.value = bio;
        }
        if (justificationField) {
            justificationField.value = justification;
        }

        var basePath = adminBaseUrl + '/admin-requests/' + encodeURIComponent(requestId);
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

    function close() {
        modal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    document.addEventListener('click', function (event) {
        var openTrigger = findOpenTrigger(event.target);
        if (openTrigger) {
            event.preventDefault();
            open(openTrigger);
            return;
        }
        if (findCloseAncestor(event.target)) {
            event.preventDefault();
            close();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            close();
        }
    });
}());
