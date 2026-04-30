(function () {
    document.addEventListener('keydown', function (event) {
        var currentTab = event.target && event.target.closest && event.target.closest('[data-activity-tab-target]');
        if (!currentTab) {
            return;
        }

        if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].indexOf(event.key) < 0) {
            return;
        }

        var tabsRoot = currentTab.closest('.activity-tabs-list');
        var tabs = tabsRoot ? Array.prototype.slice.call(tabsRoot.querySelectorAll('[data-activity-tab-target]')) : [];
        var currentIndex = tabs.indexOf(currentTab);
        var nextIndex;

        if (event.key === 'Home') {
            nextIndex = 0;
        } else if (event.key === 'End') {
            nextIndex = tabs.length - 1;
        } else if (event.key === 'ArrowLeft') {
            nextIndex = currentIndex <= 0 ? tabs.length - 1 : currentIndex - 1;
        } else {
            nextIndex = currentIndex >= tabs.length - 1 ? 0 : currentIndex + 1;
        }

        var nextTab = tabs[nextIndex];
        if (nextTab && nextTab.href) {
            event.preventDefault();
            window.location.href = nextTab.href;
        }
    });
}());
