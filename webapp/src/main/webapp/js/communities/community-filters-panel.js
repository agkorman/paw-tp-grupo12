(function () {
    var panel = document.getElementById('communityFiltersPanel');
    var overlay = document.getElementById('communityFiltersOverlay');
    var toggleBtn = document.getElementById('communityFiltersToggleBtn');
    var applyBtn = document.getElementById('communityFiltersApplyBtn');
    var clearBtn = document.getElementById('communityFiltersClearBtn');
    var toolbarForm = document.getElementById('community-filter-form');

    if (!panel || !toolbarForm) {
        return;
    }

    function openPanel() {
        panel.removeAttribute('hidden');
        panel.classList.add('is-open');
        if (overlay) { overlay.classList.add('is-visible'); }
        if (toggleBtn) { toggleBtn.setAttribute('aria-expanded', 'true'); }
        document.addEventListener('keydown', onEscape);
    }

    function closePanel() {
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

    function currentPanelParams() {
        var params = {};
        var joinedOnly = document.getElementById('panelJoinedOnly');
        if (joinedOnly && joinedOnly.value !== '') {
            params.joinedOnly = joinedOnly.value;
        }
        return params;
    }

    function syncToggleState(params) {
        if (!toggleBtn) {
            return;
        }
        var active = params.joinedOnly === 'true';
        toggleBtn.classList.toggle('is-active', active);
        toggleBtn.setAttribute('aria-pressed', active ? 'true' : 'false');
    }

    function injectPanelParams(params) {
        var existing = toolbarForm.querySelector('[data-panel-injected="joinedOnly"]');
        if (params.joinedOnly) {
            if (!existing) {
                existing = document.createElement('input');
                existing.type = 'hidden';
                existing.name = 'joinedOnly';
                existing.setAttribute('data-panel-injected', 'joinedOnly');
                toolbarForm.appendChild(existing);
            }
            existing.value = params.joinedOnly;
        } else if (existing) {
            existing.parentNode.removeChild(existing);
        }
    }

    function submitToolbarForm() {
        if (typeof toolbarForm.requestSubmit === 'function') {
            toolbarForm.requestSubmit();
            return;
        }
        toolbarForm.submit();
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', function () {
            panel.classList.contains('is-open') ? closePanel() : openPanel();
        });
    }

    document.addEventListener('click', function (event) {
        if (event.target && event.target.closest('[data-close-community-filters-panel]')) { closePanel(); }
        var openTrigger = event.target && event.target.closest('[data-open-community-filters-panel]');
        if (openTrigger && openTrigger !== toggleBtn) { openPanel(); }
    });

    panel.addEventListener('click', function (event) {
        var btn = event.target instanceof Element && event.target.closest('.filter-toggle-option');
        if (!btn || btn.disabled) { return; }

        var group = btn.closest('[data-community-filter-target]');
        if (!group) { return; }

        var hidden = document.getElementById(group.getAttribute('data-community-filter-target'));
        Array.prototype.forEach.call(group.querySelectorAll('.filter-toggle-option'), function (option) {
            option.classList.remove('is-selected');
        });
        btn.classList.add('is-selected');
        if (hidden) {
            hidden.value = btn.getAttribute('data-value') || '';
        }
        syncToggleState(currentPanelParams());
    });

    if (applyBtn) {
        applyBtn.addEventListener('click', function () {
            injectPanelParams(currentPanelParams());
            closePanel();
            submitToolbarForm();
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', function () {
            var hidden = document.getElementById('panelJoinedOnly');
            if (hidden) { hidden.value = ''; }
            Array.prototype.forEach.call(panel.querySelectorAll('.filter-toggle-option'), function (option) {
                option.classList.toggle('is-selected', option.getAttribute('data-value') === '');
            });
            injectPanelParams({});
            syncToggleState({});
            closePanel();
            submitToolbarForm();
        });
    }

    toolbarForm.addEventListener('submit', function () {
        var params = currentPanelParams();
        injectPanelParams(params);
        syncToggleState(params);
    });

    syncToggleState(currentPanelParams());
})();
