(function () {
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

    function activityTabStorageKey() {
        return 'paw.activity.activeTab.' + window.location.pathname;
    }

    function getStoredActivityTab() {
        try {
            return window.localStorage.getItem(activityTabStorageKey());
        } catch (error) {
            return null;
        }
    }

    function storeActivityTab(panelId) {
        try {
            window.localStorage.setItem(activityTabStorageKey(), panelId);
        } catch (error) {
            // Ignore storage failures so tab navigation keeps working.
        }
    }

    function hasActivityTab(tabsRoot, panelId) {
        return !!(tabsRoot && panelId && tabsRoot.querySelector('[data-activity-tab-target="' + panelId + '"]'));
    }

    function activePreviewLayout() {
        return document.querySelector('.activity-tab-panel:not([hidden]) .activity-panel-layout.has-active-preview');
    }

    function syncBodyScrollLock() {
        document.body.classList.toggle(
            'activity-preview-scroll-lock',
            !!activePreviewLayout() && window.innerWidth > 1100
        );
    }

    function setPreviewState(panel, active) {
        var layout = panel && panel.closest ? panel.closest('.activity-panel-layout') : null;
        if (panel) {
            panel.hidden = !active;
        }
        if (layout) {
            layout.classList.toggle('has-active-preview', active);
            if (active) {
                updatePreviewLayoutHeight(layout);
            } else {
                layout.style.removeProperty('--activity-panel-height');
            }
        }
        syncBodyScrollLock();
    }

    function updatePreviewLayoutHeight(layout) {
        var rect;
        var height;

        if (!layout || window.innerWidth <= 1100) {
            return;
        }

        rect = layout.getBoundingClientRect();
        height = Math.max(360, window.innerHeight - rect.top - 32);
        layout.style.setProperty('--activity-panel-height', height + 'px');
    }

    function updateActivePreviewLayoutHeights() {
        var layouts = document.querySelectorAll('.activity-panel-layout.has-active-preview');
        for (var i = 0; i < layouts.length; i += 1) {
            updatePreviewLayoutHeight(layouts[i]);
        }
    }

    function closeActivityPreview(tabsRoot) {
        var root = tabsRoot || document;
        var panels = root.querySelectorAll('[data-activity-preview-panel]');
        var cards = root.querySelectorAll('[data-activity-review-card]');

        for (var i = 0; i < panels.length; i += 1) {
            panels[i].hidden = true;
            var layout = panels[i].closest ? panels[i].closest('.activity-panel-layout') : null;
            if (layout) {
                layout.classList.remove('has-active-preview');
            }
        }
        for (var j = 0; j < cards.length; j += 1) {
            cards[j].classList.remove('is-selected');
            cards[j].removeAttribute('aria-current');
        }
        syncBodyScrollLock();
    }

    function openActivityPreview(card) {
        var targetId = card ? card.getAttribute('data-activity-preview-target') : null;
        var panel = targetId ? document.getElementById(targetId) : null;
        var tabsRoot = card ? closestByAttribute(card, 'data-activity-tabs') : null;
        var closeButton;

        if (!panel || !tabsRoot) {
            return false;
        }

        closeActivityPreview(tabsRoot);
        card.classList.add('is-selected');
        card.setAttribute('aria-current', 'true');
        setPreviewState(panel, true);

        closeButton = panel.querySelector('[data-close-activity-preview]');
        if (closeButton && typeof closeButton.focus === 'function') {
            closeButton.focus();
        }
        return true;
    }

    function activateActivityTab(tabsRoot, panelId, shouldFocus) {
        var tabs = tabsRoot ? tabsRoot.querySelectorAll('[data-activity-tab-target]') : [];
        var panels = tabsRoot ? tabsRoot.querySelectorAll('.activity-tab-panel') : [];
        var activeTab = null;

        if (!hasActivityTab(tabsRoot, panelId)) {
            return;
        }

        for (var i = 0; i < tabs.length; i += 1) {
            var selected = tabs[i].getAttribute('data-activity-tab-target') === panelId;
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
        updateActivePreviewLayoutHeights();
        syncBodyScrollLock();
        storeActivityTab(panelId);
    }

    function setupActivityTabs() {
        var tabsRoot = document.querySelector('[data-activity-tabs]');
        var tabs = tabsRoot ? tabsRoot.querySelectorAll('[data-activity-tab-target]') : [];
        var initialPanelId = null;

        if (!tabsRoot || tabs.length === 0) {
            return;
        }

        if (window.location.hash) {
            var hashPanel = document.getElementById(window.location.hash.substring(1));
            if (hashPanel && hashPanel.className.indexOf('activity-tab-panel') >= 0) {
                initialPanelId = hashPanel.id;
            }
        }

        if (!initialPanelId) {
            initialPanelId = getStoredActivityTab();
        }

        if (!hasActivityTab(tabsRoot, initialPanelId)) {
            initialPanelId = tabs[0].getAttribute('data-activity-tab-target');
        }

        activateActivityTab(tabsRoot, initialPanelId, false);
    }

    document.addEventListener('click', function (event) {
        var previewCard = closestByAttribute(event.target, 'data-activity-review-card');
        var closePreview = closestByAttribute(event.target, 'data-close-activity-preview');
        var tab = closestByAttribute(event.target, 'data-activity-tab-target');
        var tabsRoot;

        if (closePreview) {
            event.preventDefault();
            tabsRoot = closestByAttribute(closePreview, 'data-activity-tabs') || document;
            closeActivityPreview(tabsRoot);
            return;
        }

        if (previewCard) {
            if (openActivityPreview(previewCard)) {
                event.preventDefault();
            }
            return;
        }

        if (!tab) {
            return;
        }

        event.preventDefault();
        activateActivityTab(
            closestByAttribute(tab, 'data-activity-tabs'),
            tab.getAttribute('data-activity-tab-target'),
            false
        );
    });

    document.addEventListener('keydown', function (event) {
        var currentTab = closestByAttribute(event.target, 'data-activity-tab-target');
        var tabsRoot = currentTab ? closestByAttribute(currentTab, 'data-activity-tabs') : null;
        var tabs = tabsRoot ? tabsRoot.querySelectorAll('[data-activity-tab-target]') : [];
        var currentIndex = -1;
        var nextIndex = 0;

        if (!currentTab) {
            return;
        }

        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            activateActivityTab(tabsRoot, currentTab.getAttribute('data-activity-tab-target'), false);
            return;
        }

        if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].indexOf(event.key) < 0) {
            return;
        }

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
            activateActivityTab(tabsRoot, tabs[nextIndex].getAttribute('data-activity-tab-target'), true);
        }
    });

    window.addEventListener('resize', function () {
        updateActivePreviewLayoutHeights();
        syncBodyScrollLock();
    });

    setupActivityTabs();
}());
