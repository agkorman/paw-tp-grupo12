(function () {

    var panel     = document.getElementById('usersSearchPanel');
    var overlay   = document.getElementById('usersPanelOverlay');
    var toggleBtn = document.getElementById('usersPanelToggleBtn');

    if (!panel) { return; }

    var wasAutoOpened = panel.getAttribute('data-auto-open') === 'true';

    /* ── OPEN / CLOSE ── */

    function openPanel() {
        panel.removeAttribute('hidden');
        panel.classList.add('is-open');
        if (overlay) { overlay.classList.add('is-visible'); }
        if (toggleBtn) { toggleBtn.setAttribute('aria-expanded', 'true'); }
        document.addEventListener('keydown', onEscape);
    }

    function closePanel() {
        if (wasAutoOpened) {
            history.back();
            return;
        }
        panel.classList.remove('is-open');
        if (overlay) { overlay.classList.remove('is-visible'); }
        if (toggleBtn) {
            toggleBtn.setAttribute('aria-expanded', 'false');
            toggleBtn.focus();
        }
        document.removeEventListener('keydown', onEscape);
        panel.addEventListener('transitionend', function hide() {
            if (!panel.classList.contains('is-open')) { panel.setAttribute('hidden', ''); }
            panel.removeEventListener('transitionend', hide);
        });
    }

    function onEscape(event) {
        if (event.key === 'Escape') { closePanel(); }
    }

    document.addEventListener('click', function (event) {
        if (event.target && event.target.closest('[data-close-users-panel]')) { closePanel(); }
        if (event.target && event.target.closest('[data-open-users-panel]')) { openPanel(); }
    });

    /* When the browser restores this page from bfcache (after history.back()),
       force-close the panel so it doesn't appear open on the previous page. */
    window.addEventListener('pageshow', function (event) {
        if (event.persisted) {
            panel.classList.remove('is-open');
            if (overlay) { overlay.classList.remove('is-visible'); }
            if (toggleBtn) { toggleBtn.setAttribute('aria-expanded', 'false'); }
            panel.setAttribute('hidden', '');
            document.removeEventListener('keydown', onEscape);
        }
    });

    /* Replace history entry when re-searching from within the results page,
       so closing the panel always returns to the page before the first search. */
    if (wasAutoOpened) {
        var searchForm = panel.querySelector('form');
        if (searchForm) {
            searchForm.addEventListener('submit', function (event) {
                event.preventDefault();
                var input = searchForm.querySelector('input[name="q"]');
                var q = input ? input.value : '';
                location.replace(searchForm.getAttribute('action') + '?q=' + encodeURIComponent(q));
            });
        }
        openPanel();
    }

})();
