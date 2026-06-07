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

    function closestByClass(target, className) {
        var node = target;

        while (node && node !== document) {
            if (node.nodeType === 1 && hasClass(node, className)) {
                return node;
            }
            node = node.parentNode;
        }

        return null;
    }

    function closestByTagName(target, tagNames) {
        var node = target;

        while (node && node !== document) {
            if (node.nodeType === 1 && tagNames.indexOf(node.tagName) >= 0) {
                return node;
            }
            node = node.parentNode;
        }

        return null;
    }

    function isInteractiveCardTarget(target) {
        return closestByTagName(target, ['A', 'BUTTON', 'INPUT', 'SELECT', 'TEXTAREA', 'FORM', 'LABEL'])
                || closestByAttribute(target, 'data-profile-review-menu');
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

    function filterConnections(query) {
        var modal = document.getElementById('profileConnectionsModal');
        var visibleList = modal ? modal.querySelector('[data-connections-list]') : null;
        var rows = visibleList ? visibleList.querySelectorAll('[data-connection-row]') : [];
        var normalizedQuery = (query || '').toLowerCase();
        var empty = visibleList ? visibleList.querySelector('[data-connections-empty]') : null;
        var visibleCount = 0;

        for (var i = 0; i < rows.length; i += 1) {
            var haystack = (rows[i].getAttribute('data-search-text') || '').toLowerCase();
            rows[i].hidden = normalizedQuery !== '' && haystack.indexOf(normalizedQuery) < 0;
            if (!rows[i].hidden) {
                visibleCount += 1;
            }
        }

        if (empty) {
            empty.hidden = normalizedQuery === '' || visibleCount > 0;
        }
    }

    function setupProfileEditValidation() {
        var form = document.querySelector('[data-profile-edit-form]');
        var username = document.getElementById('profileNameInput');
        var usernamePattern = /^[A-Za-z0-9._-]+$/;

        if (!form || !username) {
            return;
        }

        function errorId() {
            return username.id + 'ClientError';
        }

        function findError() {
            return form.querySelector('[data-client-error-for="' + username.id + '"]');
        }

        function setDescribedBy() {
            var ids = (username.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
            if (ids.indexOf(errorId()) === -1) {
                ids.push(errorId());
                username.setAttribute('aria-describedby', ids.join(' '));
            }
        }

        function removeDescribedBy() {
            var ids = (username.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
                return id && id !== errorId();
            });
            if (ids.length) {
                username.setAttribute('aria-describedby', ids.join(' '));
            } else {
                username.removeAttribute('aria-describedby');
            }
        }

        function showError(message) {
            var container = closestByClass(username, 'profile-edit-field') || username.parentNode;
            var error = findError();
            if (!error) {
                error = document.createElement('span');
                error.id = errorId();
                error.className = 'form-error client-form-error';
                error.setAttribute('data-client-error-for', username.id);
                error.setAttribute('role', 'alert');
                container.appendChild(error);
            }
            error.textContent = message || '';
            error.hidden = false;
            username.classList.add('is-invalid');
            username.setAttribute('aria-invalid', 'true');
            setDescribedBy();
        }

        function clearError() {
            var error = findError();
            if (error) {
                error.textContent = '';
                error.hidden = true;
            }
            username.classList.remove('is-invalid');
            username.removeAttribute('aria-invalid');
            removeDescribedBy();
        }

        function validateUsername() {
            var value = username.value ? username.value.trim() : '';
            clearError();
            if (value === '') {
                showError(form.getAttribute('data-msg-required-username') || '');
                return false;
            }
            if (value.length > 50) {
                showError(form.getAttribute('data-msg-username-max') || '');
                return false;
            }
            if (!usernamePattern.test(value)) {
                showError(form.getAttribute('data-msg-username-pattern') || '');
                return false;
            }
            username.value = value;
            return true;
        }

        form.noValidate = true;
        username.addEventListener('input', validateUsername);
        form.addEventListener('submit', function (event) {
            if (!validateUsername()) {
                event.preventDefault();
                username.focus();
            }
        });
    }

    function closeActionMenus() {
        if (window.PawActionMenus) {
            window.PawActionMenus.close();
        }
    }

    // Remember the selected tab across page reloads.
    function profileTabStorageKey() {
        return 'paw.profile.activeTab.' + window.location.pathname;
    }

    function getStoredProfileTab() {
        try {
            return window.localStorage.getItem(profileTabStorageKey());
        } catch (error) {
            return null;
        }
    }

    function storeProfileTab(panelId) {
        try {
            window.localStorage.setItem(profileTabStorageKey(), panelId);
        } catch (error) {
            // Ignore storage failures so tab navigation keeps working.
        }
    }

    function hasProfileTab(tabsRoot, panelId) {
        return !!(tabsRoot && panelId && tabsRoot.querySelector('[data-profile-tab-target="' + panelId + '"]'));
    }

    function activateProfileTab(tabsRoot, panelId, shouldFocus) {
        var tabs = tabsRoot ? tabsRoot.querySelectorAll('[data-profile-tab-target]') : [];
        var panels = tabsRoot ? tabsRoot.querySelectorAll('.profile-tab-panel') : [];
        var activeTab = null;

        if (!hasProfileTab(tabsRoot, panelId)) {
            return;
        }

        for (var i = 0; i < tabs.length; i += 1) {
            var selected = tabs[i].getAttribute('data-profile-tab-target') === panelId;
            tabs[i].setAttribute('aria-selected', selected ? 'true' : 'false');
            tabs[i].setAttribute('tabindex', selected ? '0' : '-1');
            if (selected) {
                activeTab = tabs[i];
            }
        }

        for (var j = 0; j < panels.length; j += 1) {
            panels[j].hidden = panels[j].id !== panelId;
        }

        if (shouldFocus && activeTab) {
            activeTab.focus();
        }
        storeProfileTab(panelId);
    }

    function setupProfileTabs() {
        var tabsRoot = document.querySelector('[data-profile-tabs]');
        var tabs = tabsRoot ? tabsRoot.querySelectorAll('[data-profile-tab-target]') : [];
        var initialPanelId = null;

        if (!tabsRoot || tabs.length === 0) {
            return;
        }

        tabsRoot.setAttribute('data-tabs-ready', 'true');

        if (window.location.hash) {
            var hashTarget = document.getElementById(window.location.hash.substring(1));
            var hashPanel = hashTarget ? closestByClass(hashTarget, 'profile-tab-panel') : null;
            if (hashPanel) {
                initialPanelId = hashPanel.id;
            }
        }

        if (!initialPanelId) {
            initialPanelId = getStoredProfileTab();
        }

        if (!hasProfileTab(tabsRoot, initialPanelId)) {
            initialPanelId = null;
        }

        if (!initialPanelId) {
            for (var i = 0; i < tabs.length; i += 1) {
                if (tabs[i].getAttribute('aria-selected') === 'true') {
                    initialPanelId = tabs[i].getAttribute('data-profile-tab-target');
                    break;
                }
            }
        }

        activateProfileTab(tabsRoot, initialPanelId || tabs[0].getAttribute('data-profile-tab-target'), false);
    }

    function setupAutoOpenModals() {
        var modals = document.querySelectorAll('.profile-modal[data-open-on-load="true"]');
        for (var i = 0; i < modals.length; i += 1) {
            openModal(modals[i]);
        }
    }

    function setupConnectionsSearch() {
        var search = document.querySelector('[data-connections-search]');

        if (!search) {
            return;
        }

        search.addEventListener('input', function () {
            filterConnections(search.value);
        });
    }

    document.addEventListener('click', function (event) {
        var editButton = closestByAttribute(event.target, 'data-open-edit-profile-modal');
        if (editButton) {
            openModal(document.getElementById('editProfileModal'));
            return;
        }

        var closeButton = closestByAttribute(event.target, 'data-close-profile-modal');
        if (closeButton) {
            closeOpenModal();
            return;
        }

        var reviewsButton = closestByAttribute(event.target, 'data-scroll-to-reviews');
        if (reviewsButton) {
            var tabsRoot = document.querySelector('[data-profile-tabs]');
            var activityPanel = document.getElementById('profileActivityPanel');
            var scrollTarget = document.querySelector('[data-profile-tabs]') || document.getElementById('profileActivityTitle');
            if (tabsRoot && activityPanel) {
                activateProfileTab(tabsRoot, activityPanel.id, false);
            }
            if (scrollTarget) {
                scrollTarget.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
            return;
        }

        var profileTab = closestByAttribute(event.target, 'data-profile-tab-target');
        if (profileTab) {
            event.preventDefault();
            activateProfileTab(
                closestByAttribute(profileTab, 'data-profile-tabs'),
                profileTab.getAttribute('data-profile-tab-target'),
                false
            );
            return;
        }

        var linkedCard = closestByAttribute(event.target, 'data-profile-card-link');
        if (linkedCard && !isInteractiveCardTarget(event.target)) {
            window.location.href = linkedCard.getAttribute('data-profile-card-link');
            return;
        }

    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeOpenModal();
            return;
        }

        if (event.key !== 'Enter' && event.key !== ' ') {
            var currentTab = closestByAttribute(event.target, 'data-profile-tab-target');
            if (!currentTab || ['ArrowLeft', 'ArrowRight', 'Home', 'End'].indexOf(event.key) < 0) {
                return;
            }

            var tabsRoot = closestByAttribute(currentTab, 'data-profile-tabs');
            var tabs = tabsRoot ? tabsRoot.querySelectorAll('[data-profile-tab-target]') : [];
            var currentIndex = -1;
            var nextIndex = 0;

            for (var i = 0; i < tabs.length; i += 1) {
                if (tabs[i] === currentTab) {
                    currentIndex = i;
                    break;
                }
            }

            if (event.key === 'Home') {
                nextIndex = 0;
            } else if (event.key === 'End') {
                nextIndex = tabs.length - 1;
            } else if (event.key === 'ArrowLeft') {
                nextIndex = currentIndex <= 0 ? tabs.length - 1 : currentIndex - 1;
            } else {
                nextIndex = currentIndex >= tabs.length - 1 ? 0 : currentIndex + 1;
            }

            if (tabs[nextIndex]) {
                event.preventDefault();
                activateProfileTab(tabsRoot, tabs[nextIndex].getAttribute('data-profile-tab-target'), true);
            }
            return;
        }

        var activeTab = closestByAttribute(event.target, 'data-profile-tab-target');
        if (activeTab) {
            event.preventDefault();
            activateProfileTab(
                closestByAttribute(activeTab, 'data-profile-tabs'),
                activeTab.getAttribute('data-profile-tab-target'),
                false
            );
            return;
        }

        var linkedCard = closestByAttribute(event.target, 'data-profile-card-link');
        if (!linkedCard || isInteractiveCardTarget(event.target)) {
            return;
        }

        event.preventDefault();
        window.location.href = linkedCard.getAttribute('data-profile-card-link');
    });

    setupProfileTabs();
    setupAutoOpenModals();
    setupConnectionsSearch();
    setupProfileEditValidation();
}());
