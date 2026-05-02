(function () {
    var HIGHLIGHT_CLASS = 'is-anchor-highlighted';
    var HIGHLIGHT_DURATION_MS = 1000;
    var activeTarget = null;
    var activeTimer = null;

    function highlightedReviewFromHash() {
        var id;

        if (!window.location.hash || window.location.hash.indexOf('#review-') !== 0) {
            return null;
        }

        id = window.location.hash.substring(1);
        return document.getElementById(id);
    }

    function clearHighlight() {
        if (activeTimer) {
            window.clearTimeout(activeTimer);
            activeTimer = null;
        }

        if (activeTarget) {
            activeTarget.classList.remove(HIGHLIGHT_CLASS);
            activeTarget = null;
        }
    }

    function applyAnchorHighlight() {
        var target = highlightedReviewFromHash();

        clearHighlight();

        if (!target || target.className.indexOf('review-item') < 0) {
            return;
        }

        activeTarget = target;
        activeTarget.classList.add(HIGHLIGHT_CLASS);
        activeTimer = window.setTimeout(clearHighlight, HIGHLIGHT_DURATION_MS);
    }

    applyAnchorHighlight();
    window.addEventListener('hashchange', applyAnchorHighlight);
}());
