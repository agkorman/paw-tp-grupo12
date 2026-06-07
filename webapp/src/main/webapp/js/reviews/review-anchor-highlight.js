(function () {
    var HIGHLIGHT_CLASS = 'is-anchor-highlighted';
    var HIGHLIGHT_DURATION_MS = 1000;
    var HIGHLIGHTABLE_CLASSES = ['review-item', 'review-reply', 'profile-review-card', 'community-post-card'];
    var activeTarget = null;
    var activeTimer = null;

    function anchorTargetFromHash() {
        if (!window.location.hash || window.location.hash.length < 2) {
            return null;
        }
        return document.getElementById(window.location.hash.substring(1));
    }

    function isHighlightable(element) {
        if (!element) {
            return false;
        }
        var className = ' ' + element.className + ' ';
        for (var i = 0; i < HIGHLIGHTABLE_CLASSES.length; i += 1) {
            if (className.indexOf(' ' + HIGHLIGHTABLE_CLASSES[i] + ' ') >= 0) {
                return true;
            }
        }
        return false;
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
        var target = anchorTargetFromHash();

        clearHighlight();

        if (!isHighlightable(target)) {
            return;
        }

        activeTarget = target;
        activeTarget.classList.add(HIGHLIGHT_CLASS);
        activeTimer = window.setTimeout(clearHighlight, HIGHLIGHT_DURATION_MS);
    }

    applyAnchorHighlight();
    window.addEventListener('hashchange', applyAnchorHighlight);
}());
