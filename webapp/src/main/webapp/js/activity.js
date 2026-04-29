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
        var tab = closestByAttribute(event.target, 'data-activity-tab-target');
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

    setupActivityTabs();
}());
