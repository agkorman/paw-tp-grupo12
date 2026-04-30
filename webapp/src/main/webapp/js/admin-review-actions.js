(function () {
    function closestByAttribute(target, attrName) {
        var node = target;
        while (node && node !== document) {
            if (node.nodeType === 1 && node.getAttribute && node.getAttribute(attrName) !== null) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function hasClass(node, className) {
        return (' ' + (node.className || '') + ' ').indexOf(' ' + className + ' ') >= 0;
    }

    function setBodyModalOpen(open) {
        var body = document.body;
        if (!body) {
            return;
        }
        if (open && !hasClass(body, 'profile-modal-open')) {
            body.className += ' profile-modal-open';
        }
        if (!open && hasClass(body, 'profile-modal-open')) {
            body.className = (' ' + body.className + ' ')
                .replace(' profile-modal-open ', ' ')
                .replace(/^\s+|\s+$/g, '');
        }
    }

    function openModal(modal) {
        if (!modal) {
            return;
        }
        modal.hidden = false;
        setBodyModalOpen(true);
    }

    function closeAllAdminModals() {
        var modals = document.querySelectorAll('.profile-modal');
        var anyOpen = false;
        for (var i = 0; i < modals.length; i += 1) {
            if (!modals[i].hidden) {
                modals[i].hidden = true;
                anyOpen = true;
            }
        }
        if (anyOpen) {
            setBodyModalOpen(false);
        }
    }

    function closeActionMenus() {
        if (window.PawActionMenus) {
            window.PawActionMenus.close();
        }
    }

    function openDeleteReviewModal(button) {
        var modal = document.getElementById('deleteReviewModal');
        var form = document.getElementById('deleteReviewForm');
        if (!modal || !form) {
            return;
        }
        form.setAttribute('action', button.getAttribute('data-review-delete-action') || '#');
        var title = modal.querySelector('[data-delete-review-title]');
        if (title) {
            title.textContent = button.getAttribute('data-review-title') || '';
        }
        closeActionMenus();
        openModal(modal);
    }

    function openDeleteReplyModal(button) {
        var modal = document.getElementById('deleteReplyModal');
        var form = document.getElementById('deleteReplyForm');
        if (!modal || !form) {
            return;
        }
        form.setAttribute('action', button.getAttribute('data-reply-delete-action') || '#');
        var body = modal.querySelector('[data-delete-reply-body]');
        if (body) {
            body.textContent = button.getAttribute('data-reply-body') || '';
        }
        closeActionMenus();
        openModal(modal);
    }

    document.addEventListener('click', function (event) {
        var reviewButton = closestByAttribute(event.target, 'data-open-delete-review-modal');
        if (reviewButton) {
            event.preventDefault();
            openDeleteReviewModal(reviewButton);
            return;
        }

        var replyButton = closestByAttribute(event.target, 'data-open-delete-reply-modal');
        if (replyButton) {
            event.preventDefault();
            openDeleteReplyModal(replyButton);
            return;
        }

        var closeButton = closestByAttribute(event.target, 'data-close-profile-modal');
        if (closeButton) {
            closeAllAdminModals();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeAllAdminModals();
        }
    });
}());
