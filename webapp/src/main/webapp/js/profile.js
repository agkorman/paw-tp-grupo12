(function () {
    function hasClass(node, className) {
        return (' ' + node.className + ' ').indexOf(' ' + className + ' ') >= 0;
    }

    function supportsFetchFormData() {
        return typeof window.fetch === 'function'
            && typeof window.FormData === 'function'
            && typeof window.Promise === 'function';
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
                || closestByAttribute(target, 'data-profile-review-menu')
                || closestByAttribute(target, 'data-review-like-toggle');
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

    function closeActionMenus() {
        if (window.PawActionMenus) {
            window.PawActionMenus.close();
        }
    }

    function updateFollowButton(button, following) {
        var followLabel = button.getAttribute('data-follow-label') || '';
        var followingLabel = button.getAttribute('data-following-label') || followLabel;

        button.setAttribute('aria-pressed', String(following));
        setClass(button, 'is-following', following);
        button.textContent = following ? followingLabel : followLabel;
    }

    function updateFollowButtons(userId, following) {
        var buttons = document.querySelectorAll('[data-follow-toggle][data-follow-user-id="' + userId + '"]');

        for (var i = 0; i < buttons.length; i += 1) {
            updateFollowButton(buttons[i], following);
        }
    }

    function updateProfileFollowerCount(userId, followerCount) {
        var profileRoot = document.querySelector('[data-profile-user-id]');
        var countNode = document.querySelector('[data-profile-follower-count]');

        if (!profileRoot || !countNode || profileRoot.getAttribute('data-profile-user-id') !== userId) {
            return;
        }
        countNode.textContent = String(Math.max(0, followerCount));
    }

    function parseFollowResponse(body) {
        var parts = (body || '').trim().split('|');
        var followerCount;

        if (parts.length !== 2) {
            return null;
        }
        followerCount = parseInt(parts[1], 10);
        if (isNaN(followerCount)) {
            return null;
        }
        return {
            following: parts[0] === 'true',
            followerCount: followerCount
        };
    }

    function submitFollowForm(form) {
        var button = form.querySelector('[data-follow-toggle]');
        var userId = form.getAttribute('data-follow-user-id');

        if (!button || !userId || button.disabled || form.getAttribute('data-loading') === 'true') {
            return;
        }
        if (!supportsFetchFormData()) {
            form.submit();
            return;
        }

        form.setAttribute('data-loading', 'true');
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');

        window.fetch(form.getAttribute('action'), {
            method: 'POST',
            body: new window.FormData(form),
            credentials: 'same-origin',
            headers: {
                'Accept': 'text/plain',
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).then(function (response) {
            if (response.redirected) {
                window.location.href = response.url;
                return '';
            }
            if (response.status === 401) {
                window.location.href = '/login';
                return '';
            }
            if (!response.ok) {
                throw new Error('follow-request-failed');
            }
            return response.text();
        }).then(function (body) {
            var nextState;

            if (!body) {
                return;
            }
            nextState = parseFollowResponse(body);
            if (!nextState) {
                throw new Error('invalid-follow-response');
            }
            updateFollowButtons(userId, nextState.following);
            updateProfileFollowerCount(userId, nextState.followerCount);
        }).catch(function () {
            form.submit();
        }).finally(function () {
            form.removeAttribute('data-loading');
            if (document.contains(button)) {
                button.disabled = false;
                button.removeAttribute('aria-busy');
            }
        });
    }

    // funciones para recordar la pestaña en la que se encuentra el usuario en caso de un reload
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
            var hashPanel = document.getElementById(window.location.hash.substring(1));
            if (hashPanel && hasClass(hashPanel, 'profile-tab-panel')) {
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

    function setupAutoOpenModals() {
        var modals = document.querySelectorAll('.profile-modal[data-open-on-load="true"]');
        for (var i = 0; i < modals.length; i += 1) {
            openModal(modals[i]);
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

        var editButton = closestByAttribute(event.target, 'data-open-edit-profile-modal');
        if (editButton) {
            openModal(document.getElementById('editProfileModal'));
            return;
        }

        var connectionsButton = closestByAttribute(event.target, 'data-open-connections-modal');
        if (connectionsButton) {
            switchConnectionsList(
                connectionsButton.getAttribute('data-connections-kind') || 'following',
                connectionsButton.getAttribute('data-connections-title') || ''
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
            var tabsRoot = document.querySelector('[data-profile-tabs]');
            var reviewsPanel = document.getElementById('profileReviewsPanel');
            var scrollTarget = document.querySelector('[data-profile-tabs]') || document.getElementById('profileReviewsTitle');
            if (tabsRoot && reviewsPanel) {
                activateProfileTab(tabsRoot, reviewsPanel.id, false);
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

        var sectionToggle = closestByAttribute(event.target, 'data-collapsible-toggle');
        if (sectionToggle) {
            event.preventDefault();
            setCollapsibleSectionExpanded(
                sectionToggle,
                sectionToggle.getAttribute('data-expanded') !== 'true'
            );
        }
    });

    document.addEventListener('submit', function (event) {
        var form = event.target;

        if (!hasAttribute(form, 'data-enhanced-follow')) {
            return;
        }
        if (!supportsFetchFormData()) {
            return;
        }
        event.preventDefault();
        submitFollowForm(form);
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

    var search = document.querySelector('[data-connections-search]');
    if (search) {
        search.addEventListener('input', function () {
            filterConnections(search.value);
        });
    }

    setupProfileTabs();
    setupCollapsibleSections();
    setupAutoOpenModals();
}());
