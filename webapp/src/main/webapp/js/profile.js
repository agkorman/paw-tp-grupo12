(function () {
    function hasClass(node, className) {
        return (' ' + node.className + ' ').indexOf(' ' + className + ' ') >= 0;
    }

    function setClass(node, className, enabled) {
        if (enabled && !hasClass(node, className)) {
            node.className += ' ' + className;
        }
        if (!enabled && hasClass(node, className)) {
            node.className = (' ' + node.className + ' ')
                .replace(' ' + className + ' ', ' ')
                .replace(/^\s+|\s+$/g, '');
        }
    }

    function hasAttribute(node, attrName) {
        return node && node.nodeType === 1 && node.getAttribute(attrName) !== null;
    }

    function closestByAttribute(target, attrName) {
        var node = target;

        while (node && node !== document) {
            if (hasAttribute(node, attrName)) {
                return node;
            }
            node = node.parentNode;
        }

        return null;
    }

    function openModal(modal) {
        if (!modal) {
            return;
        }
        modal.hidden = false;
        setClass(document.body, 'profile-modal-open', true);
    }

    function closeModal(modal) {
        if (!modal) {
            return;
        }
        modal.hidden = true;
        setClass(document.body, 'profile-modal-open', false);
    }

    function closeOpenModal() {
        var modals = document.querySelectorAll('.profile-modal');
        for (var i = 0; i < modals.length; i += 1) {
            if (!modals[i].hidden) {
                closeModal(modals[i]);
            }
        }
    }

    function switchConnectionsList(kind, title) {
        var modal = document.getElementById('profileConnectionsModal');
        var titleNode = modal ? modal.querySelector('[data-connections-title]') : null;
        var lists = modal ? modal.querySelectorAll('[data-connections-list]') : [];
        var search = modal ? modal.querySelector('[data-connections-search]') : null;

        if (titleNode) {
            titleNode.textContent = title;
        }
        for (var i = 0; i < lists.length; i += 1) {
            lists[i].hidden = lists[i].getAttribute('data-connections-list') !== kind;
        }
        if (search) {
            search.value = '';
            filterConnections('');
        }
        openModal(modal);
    }

    function filterConnections(query) {
        var modal = document.getElementById('profileConnectionsModal');
        var visibleList = modal ? modal.querySelector('[data-connections-list]:not([hidden])') : null;
        var rows = visibleList ? visibleList.querySelectorAll('[data-connection-row]') : [];
        var normalizedQuery = query.toLowerCase();

        for (var i = 0; i < rows.length; i += 1) {
            var haystack = (rows[i].getAttribute('data-search-text') || '').toLowerCase();
            rows[i].hidden = normalizedQuery !== '' && haystack.indexOf(normalizedQuery) < 0;
        }
    }

    function closeActionMenus() {
        if (window.PawActionMenus) {
            window.PawActionMenus.close();
        }
    }

    function setupCollapsibleSections() {
        var sections = document.querySelectorAll('[data-collapsible-section]');

        for (var i = 0; i < sections.length; i += 1) {
            var extras = sections[i].querySelectorAll('[data-collapsible-extra]');
            var toggle = sections[i].querySelector('[data-collapsible-toggle]');

            if (!toggle || extras.length === 0) {
                continue;
            }

            for (var j = 0; j < extras.length; j += 1) {
                extras[j].hidden = true;
            }
            toggle.hidden = false;
            toggle.setAttribute('aria-expanded', 'false');
            toggle.setAttribute('data-expanded', 'false');
            toggle.textContent = toggle.getAttribute('data-show-label') || 'Ver más';
        }
    }

    function setCollapsibleSectionExpanded(toggle, expanded) {
        var section = closestByAttribute(toggle, 'data-collapsible-section');
        var extras = section ? section.querySelectorAll('[data-collapsible-extra]') : [];
        var label = expanded
            ? toggle.getAttribute('data-hide-label') || 'Ver menos'
            : toggle.getAttribute('data-show-label') || 'Ver más';

        for (var i = 0; i < extras.length; i += 1) {
            extras[i].hidden = !expanded;
        }
        toggle.setAttribute('aria-expanded', expanded ? 'true' : 'false');
        toggle.setAttribute('data-expanded', expanded ? 'true' : 'false');
        toggle.textContent = label;
    }

    function openDeleteReviewModal(button) {
        var modal = document.getElementById('deleteReviewModal');
        var form = document.getElementById('deleteReviewForm');
        var title = modal ? modal.querySelector('[data-delete-review-title]') : null;
        if (!modal || !form) {
            return;
        }

        form.setAttribute('action', button.getAttribute('data-review-delete-action') || '#');
        if (title) {
            title.textContent = button.getAttribute('data-review-title') || '';
        }
        closeActionMenus();
        openModal(modal);
    }

    document.addEventListener('click', function (event) {
        var deleteReviewButton = closestByAttribute(event.target, 'data-open-delete-review-modal');
        if (deleteReviewButton) {
            event.preventDefault();
            openDeleteReviewModal(deleteReviewButton);
            return;
        }

        if (closestByAttribute(event.target, 'data-open-review-modal')) {
            closeActionMenus();
        }

        var editButton = closestByAttribute(event.target, 'data-open-edit-profile-modal');
        if (editButton) {
            openModal(document.getElementById('editProfileModal'));
            return;
        }

        var connectionsButton = closestByAttribute(event.target, 'data-open-connections-modal');
        if (connectionsButton) {
            switchConnectionsList(
                connectionsButton.getAttribute('data-connections-kind') || 'following',
                connectionsButton.getAttribute('data-connections-title') || 'Seguidos'
            );
            return;
        }

        var closeButton = closestByAttribute(event.target, 'data-close-profile-modal');
        if (closeButton) {
            closeOpenModal();
            return;
        }

        var reviewsButton = closestByAttribute(event.target, 'data-scroll-to-reviews');
        if (reviewsButton) {
            var reviewsTitle = document.getElementById('profileReviewsTitle');
            if (reviewsTitle) {
                reviewsTitle.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
            return;
        }

        var sectionToggle = closestByAttribute(event.target, 'data-collapsible-toggle');
        if (sectionToggle) {
            event.preventDefault();
            setCollapsibleSectionExpanded(
                sectionToggle,
                sectionToggle.getAttribute('data-expanded') !== 'true'
            );
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeOpenModal();
        }
    });

    var search = document.querySelector('[data-connections-search]');
    if (search) {
        search.addEventListener('input', function () {
            filterConnections(search.value);
        });
    }

    var photoInput = document.querySelector('[data-profile-photo-input]');
    var photoPreview = document.querySelector('[data-profile-photo-preview]');

    if (photoInput && photoPreview && window.FileReader) {
        photoInput.addEventListener('change', function () {
            var file = photoInput.files && photoInput.files[0];
            if (!file || file.type.indexOf('image/') !== 0) {
                return;
            }

            var reader = new FileReader();
            reader.onload = function (event) {
                photoPreview.style.backgroundImage = 'url("' + event.target.result + '")';
            };
            reader.readAsDataURL(file);
        });
    }

    setupCollapsibleSections();
}());
