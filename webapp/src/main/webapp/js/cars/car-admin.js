(function () {
    var deleteModal = document.getElementById('deleteCarModal');
    var deleteForm = document.getElementById('deleteCarForm');
    var deleteTitle = deleteModal ? deleteModal.querySelector('[data-delete-car-title]') : null;
    var lastTrigger = null;

    var closestByAttribute = function (element, attribute) {
        while (element && element !== document) {
            if (element.hasAttribute && element.hasAttribute(attribute)) {
                return element;
            }
            element = element.parentNode;
        }
        return null;
    };

    var closeActionMenus = function () {
        if (window.PawActionMenus) {
            window.PawActionMenus.close();
        }
    };

    var openDeleteModal = function (trigger) {
        if (!deleteModal || !deleteForm || !trigger) {
            return;
        }

        lastTrigger = trigger;
        deleteForm.setAttribute('action', trigger.getAttribute('data-car-delete-action') || '#');
        if (deleteTitle) {
            deleteTitle.textContent = trigger.getAttribute('data-car-title') || '';
        }
        closeActionMenus();
        deleteModal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
    };

    var closeDeleteModal = function () {
        if (!deleteModal) {
            return;
        }

        deleteModal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        if (lastTrigger && document.contains(lastTrigger)) {
            lastTrigger.focus();
        }
        lastTrigger = null;
    };

    document.addEventListener('click', function (event) {
        var deleteButton = closestByAttribute(event.target, 'data-open-delete-car-modal');
        if (deleteButton) {
            event.preventDefault();
            openDeleteModal(deleteButton);
            return;
        }

        if (closestByAttribute(event.target, 'data-close-delete-car-modal')) {
            event.preventDefault();
            closeDeleteModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && deleteModal && !deleteModal.hasAttribute('hidden')) {
            closeDeleteModal();
        }
    });
})();
