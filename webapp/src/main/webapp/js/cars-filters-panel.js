(function () {

    var panel        = document.getElementById('carsFiltersPanel');
    var overlay      = document.querySelector('.cars-filters-overlay');
    var toggleBtn    = document.getElementById('filtersToggleBtn');
    var applyBtn     = document.getElementById('filtersApplyBtn');
    var clearBtn     = document.getElementById('filtersClearBtn');
    var countDisplay = document.getElementById('filtersVehicleCount');
    var toolbarForm  = document.getElementById('car-filter-form');
    var panelValidationMessage = document.getElementById('filtersPanelValidationMessage');
    var hpError = document.getElementById('panelHpError');
    var yearError = document.getElementById('panelYearError');
    var previewSubmitTimer = null;

    if (!panel || !toolbarForm) {
        return;
    }

    /* ── OPEN / CLOSE ── */

    function openPanel() {
        syncHiddenFieldsFromToolbar();
        clearValidationErrors();
        panel.removeAttribute('hidden');
        panel.classList.add('is-open');
        if (overlay) { overlay.classList.add('is-visible'); }
        if (toggleBtn) { toggleBtn.setAttribute('aria-expanded', 'true'); }
        document.addEventListener('keydown', onEscape);
    }

    function closePanel() {
        window.clearTimeout(previewSubmitTimer);
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

    if (toggleBtn) {
        toggleBtn.addEventListener('click', function () {
            panel.classList.contains('is-open') ? closePanel() : openPanel();
        });
    }

    document.addEventListener('click', function (event) {
        if (event.target && event.target.closest('[data-close-filters-panel]')) { closePanel(); }
        var openTrigger = event.target && event.target.closest('[data-open-filters-panel]');
        if (openTrigger && openTrigger !== toggleBtn)  { openPanel();  }
    });

    /* ── SYNC TOOLBAR → PANEL HIDDEN FIELDS ── */

    function syncHiddenFieldsFromToolbar() {
        var searchInput = document.getElementById('cars-toolbar-search');
        var brandSelect = document.getElementById('filter-brand');
        var bodySelect  = document.getElementById('filter-body');
        var hiddenQ        = document.getElementById('panelHiddenQ');
        var hiddenBrand    = document.getElementById('panelHiddenBrand');
        var hiddenBodyType = document.getElementById('panelHiddenBodyType');
        if (hiddenQ && searchInput)       { hiddenQ.value        = searchInput.value; }
        if (hiddenBrand && brandSelect)   { hiddenBrand.value    = brandSelect.value; }
        if (hiddenBodyType && bodySelect) { hiddenBodyType.value = bodySelect.value; }
    }

    /* ── UNIFIED FILTER GROUP HANDLER (chips + segmented controls) ── */

    panel.addEventListener('click', function (event) {
        var btn = event.target instanceof Element && event.target.closest('.filter-toggle-option, .filter-segment-option');
        if (!btn) { return; }

        var group = btn.closest('[data-filter-target]');
        if (!group) { return; }

        var hiddenId = group.getAttribute('data-filter-target');
        var hidden   = document.getElementById(hiddenId);
        var options  = group.querySelectorAll('.filter-toggle-option, .filter-segment-option');
        var isMultiple = group.getAttribute('data-filter-multiple') === 'true';

        if (isMultiple) {
            btn.classList.toggle('is-selected');
            if (hidden) {
                hidden.value = Array.prototype.map.call(options, function (opt) {
                    return opt.classList.contains('is-selected') ? opt.getAttribute('data-value') : '';
                }).filter(Boolean).join(',');
            }
        } else {
            Array.prototype.forEach.call(options, function (opt) { opt.classList.remove('is-selected'); });
            btn.classList.add('is-selected');
            if (hidden) { hidden.value = btn.getAttribute('data-value') || ''; }
        }
        clearValidationErrors();
        schedulePreviewSubmit();
    });

    /* ── SINGLE-RANGE SLIDERS with "Hasta / Desde" display ── */

    function initSingleRange(sliderId, displayId, prefix, unit) {
        var slider  = document.getElementById(sliderId);
        var display = document.getElementById(displayId);
        if (!slider || !display) { return function () {}; }

        function updateDisplay() {
            display.innerHTML = (prefix ? prefix + ' <strong>' : '<strong>') +
                                slider.value +
                                '</strong>' +
                                (unit ? ' ' + unit : '');
        }

        slider.addEventListener('input', updateDisplay);
        updateDisplay();
        return updateDisplay;
    }

    var updateConsumptionDisplay = initSingleRange('panelConsumptionSlider', 'panelConsumptionDisplay', 'Hasta', 'L/100km');
    var updateSpeedDisplay       = initSingleRange('panelMaxSpeedSlider',    'panelMaxSpeedDisplay',    'Desde', 'km/h');

    /* ── DUAL-RANGE SLIDER ── */

    function logPosToValue(pos, sliderMax, realMax) {
        if (pos <= 0) return 0;
        if (pos >= sliderMax) return realMax;
        // Map slider position to real value on a log scale (minimum anchor: 1000)
        var minAnchor = 1000;
        var raw = minAnchor * Math.pow(realMax / minAnchor, pos / sliderMax);
        return Math.round(raw / 1000) * 1000;
    }

    function logValueToPos(value, sliderMax, realMax) {
        if (value <= 0) return 0;
        if (value >= realMax) return sliderMax;
        var minAnchor = 1000;
        return Math.round(Math.log(value / minAnchor) / Math.log(realMax / minAnchor) * sliderMax);
    }

    var dualRanges = panel.querySelectorAll('.dual-range');
    Array.prototype.forEach.call(dualRanges, function (container) {
        var lowThumb  = container.querySelector('.dual-range-low');
        var highThumb = container.querySelector('.dual-range-high');
        var fill      = container.querySelector('.dual-range-fill');
        var lowId     = container.getAttribute('data-input-low');
        var highId    = container.getAttribute('data-input-high');
        var lowInput  = lowId  ? document.getElementById(lowId)  : null;
        var highInput = highId ? document.getElementById(highId) : null;

        if (!lowThumb || !highThumb) { return; }

        var isLog   = container.getAttribute('data-scale') === 'log';
        var min     = parseFloat(container.getAttribute('data-range-min') || 0);
        var max     = parseFloat(container.getAttribute('data-range-max') || 1500);
        var realMax = isLog ? parseFloat(container.getAttribute('data-real-max') || max) : max;

        function posToValue(pos) {
            return isLog ? logPosToValue(pos, max, realMax) : pos;
        }
        function valueToPos(value) {
            return isLog ? logValueToPos(value, max, realMax) : value;
        }

        function updateFill() {
            var low  = parseFloat(lowThumb.value);
            var high = parseFloat(highThumb.value);
            var range = max - min;
            if (fill && range > 0) {
                fill.style.left  = ((low  - min) / range * 100) + '%';
                fill.style.width = ((high - low)  / range * 100) + '%';
            }
        }

        function clamp() {
            var low  = parseFloat(lowThumb.value);
            var high = parseFloat(highThumb.value);
            if (low  > high) { lowThumb.value  = high; }
            if (high < low)  { highThumb.value = low;  }
        }

        lowThumb.addEventListener('input', function () {
            clamp(); updateFill();
            var realValue = posToValue(parseFloat(lowThumb.value));
            if (lowInput) { lowInput.value = realValue === min ? '' : realValue; }
        });

        highThumb.addEventListener('input', function () {
            clamp(); updateFill();
            var realValue = posToValue(parseFloat(highThumb.value));
            if (highInput) { highInput.value = realValue === realMax ? '' : realValue; }
        });

        if (lowInput) {
            lowInput.addEventListener('change', function () {
                var v = parseFloat(lowInput.value);
                if (!isNaN(v)) {
                    var pos = valueToPos(v);
                    lowThumb.value = Math.min(Math.max(pos, min), parseFloat(highThumb.value));
                    updateFill();
                }
                clearValidationErrors();
            });
        }
        if (highInput) {
            highInput.addEventListener('change', function () {
                var v = parseFloat(highInput.value);
                if (!isNaN(v)) {
                    var pos = valueToPos(v);
                    highThumb.value = Math.max(Math.min(pos, max), parseFloat(lowThumb.value));
                    updateFill();
                }
                clearValidationErrors();
            });
        }

        // Initialize slider positions from existing number input values (needed for log scale)
        if (isLog) {
            if (lowInput && lowInput.value !== '') {
                lowThumb.value = valueToPos(parseFloat(lowInput.value));
            }
            if (highInput && highInput.value !== '') {
                highThumb.value = valueToPos(parseFloat(highInput.value));
            }
        }

        updateFill();
    });

    /* ── COLLECT PANEL PARAMS ── */

    var RANGE_DEFAULTS = { fuelConsumptionMax: '30', maxSpeedMin: '0' };

    function collectPanelParams() {
        var params = {};

        var inputs = panel.querySelectorAll('input[name], select[name]');
        Array.prototype.forEach.call(inputs, function (el) {
            if (el.type === 'radio') {
                if (el.checked && el.value !== '') { params[el.name] = el.value; }
            } else if (el.type === 'range') {
                var def = RANGE_DEFAULTS[el.name];
                if (el.value !== '' && (def === undefined || el.value !== def)) {
                    params[el.name] = el.value;
                }
            } else if (el.type === 'hidden' || el.type === 'number') {
                if (el.value !== '') { params[el.name] = el.value; }
            }
        });

        return params;
    }

    /* ── INJECT / REMOVE PANEL PARAMS IN TOOLBAR FORM ── */

    var PANEL_PARAM_KEYS = [
        'q', 'brand', 'bodyType',
        'yearMin', 'yearMax', 'priceMin', 'priceMax',
        'fuelType', 'horsepowerMin', 'horsepowerMax',
        'airbagMin', 'transmission', 'fuelConsumptionMax', 'maxSpeedMin'
    ];
    var ADVANCED_PANEL_PARAM_KEYS = [
        'yearMin', 'yearMax', 'priceMin', 'priceMax',
        'fuelType', 'horsepowerMin', 'horsepowerMax',
        'airbagMin', 'transmission', 'fuelConsumptionMax', 'maxSpeedMin'
    ];

    function syncFiltersToggleState(panelParams) {
        if (!toggleBtn) {
            return;
        }

        var hasAdvancedFilters = ADVANCED_PANEL_PARAM_KEYS.some(function (key) {
            return panelParams[key] !== undefined && panelParams[key] !== '';
        });

        toggleBtn.classList.toggle('is-active', hasAdvancedFilters);
        toggleBtn.setAttribute('aria-pressed', hasAdvancedFilters ? 'true' : 'false');
    }

    function injectPanelParamsIntoForm(panelParams) {
        PANEL_PARAM_KEYS.forEach(function (key) {
            var existing = toolbarForm.querySelector('[data-panel-injected="' + key + '"]');
            var value = panelParams[key];
            if (value !== undefined && value !== '') {
                if (!existing) {
                    existing = document.createElement('input');
                    existing.type = 'hidden';
                    existing.name = key;
                    existing.setAttribute('data-panel-injected', key);
                    toolbarForm.appendChild(existing);
                }
                existing.value = value;
            } else if (existing) {
                existing.parentNode.removeChild(existing);
            }
        });
    }

    function removePanelParamsFromForm() {
        var injected = toolbarForm.querySelectorAll('[data-panel-injected]');
        Array.prototype.forEach.call(injected, function (el) { el.parentNode.removeChild(el); });
    }

    function clearValidationErrors() {
        if (panelValidationMessage) {
            panelValidationMessage.textContent = '';
            panelValidationMessage.setAttribute('hidden', '');
        }
        if (hpError) {
            hpError.textContent = '';
            hpError.setAttribute('hidden', '');
        }
        if (yearError) {
            yearError.textContent = '';
            yearError.setAttribute('hidden', '');
        }
        var invalidFields = panel.querySelectorAll('.is-invalid');
        Array.prototype.forEach.call(invalidFields, function (el) {
            el.classList.remove('is-invalid');
            el.removeAttribute('aria-invalid');
        });
    }

    function showPanelValidationError(message) {
        if (!panelValidationMessage) {
            return;
        }
        panelValidationMessage.textContent = message;
        panelValidationMessage.removeAttribute('hidden');
    }

    function showHpValidationError(message, focusFirstField) {
        var hpMin = document.getElementById('panelHpMin');
        var hpMax = document.getElementById('panelHpMax');
        if (hpError) {
            hpError.textContent = message;
            hpError.removeAttribute('hidden');
        }
        [hpMin, hpMax].forEach(function (field) {
            if (!field) { return; }
            field.classList.add('is-invalid');
            field.setAttribute('aria-invalid', 'true');
        });
        if (focusFirstField && hpMin) { hpMin.focus(); }
    }

    function showYearValidationError(message, focusFirstField) {
        var yearMin = document.getElementById('panelYearMin');
        var yearMax = document.getElementById('panelYearMax');
        if (yearError) {
            yearError.textContent = message;
            yearError.removeAttribute('hidden');
        }
        [yearMin, yearMax].forEach(function (field) {
            if (!field) { return; }
            field.classList.add('is-invalid');
            field.setAttribute('aria-invalid', 'true');
        });
        if (focusFirstField && yearMin) { yearMin.focus(); }
    }

    function isAllowedValue(value, allowedValues) {
        return value === undefined || value === '' || allowedValues.indexOf(value) !== -1;
    }

    function areAllowedCsvValues(value, allowedValues) {
        if (value === undefined || value === '') {
            return true;
        }
        return value.split(',').every(function (part) {
            return allowedValues.indexOf(part) !== -1;
        });
    }

    function isValidNumberParam(value, min, max) {
        if (value === undefined || value === '') {
            return true;
        }
        var parsed = Number(value);
        return Number.isFinite(parsed) && parsed >= min && parsed <= max;
    }

    function validatePanelParams(panelParams, focusOnError) {
        clearValidationErrors();

        if (!areAllowedCsvValues(panelParams.fuelType, ['combustion', 'hybrid', 'electric'])) {
            showPanelValidationError('Elegí una motorización válida.');
            return false;
        }
        if (!isAllowedValue(panelParams.transmission, ['', 'automatic', 'manual'])) {
            showPanelValidationError('Elegí una transmisión válida.');
            return false;
        }
        if (!isAllowedValue(panelParams.airbagMin, ['', '2', '4', '6', '8', '10'])) {
            showPanelValidationError('Elegí una cantidad de airbags válida.');
            return false;
        }
        if (!isValidNumberParam(panelParams.yearMin, 1886, 2100)
                || !isValidNumberParam(panelParams.yearMax, 1886, 2100)) {
            showYearValidationError('Usá años entre 1886 y 2100.', focusOnError);
            return false;
        }

        if (panelParams.yearMin !== undefined && panelParams.yearMax !== undefined
                && Number(panelParams.yearMin) > Number(panelParams.yearMax)) {
            showYearValidationError('El año mínimo no puede superar al máximo.', focusOnError);
            return false;
        }

        if (!isValidNumberParam(panelParams.horsepowerMin, 0, 1500)
                || !isValidNumberParam(panelParams.horsepowerMax, 0, 1500)) {
            showHpValidationError('Usá valores de potencia entre 0 y 1500 HP.', focusOnError);
            return false;
        }

        if (panelParams.horsepowerMin !== undefined && panelParams.horsepowerMax !== undefined
                && Number(panelParams.horsepowerMin) > Number(panelParams.horsepowerMax)) {
            showHpValidationError('La potencia mínima no puede superar la máxima.', focusOnError);
            return false;
        }

        if (!isValidNumberParam(panelParams.fuelConsumptionMax, 0, 30)) {
            showPanelValidationError('Usá un consumo entre 0 y 30 L/100km.');
            return false;
        }
        if (!isValidNumberParam(panelParams.maxSpeedMin, 0, 500)) {
            showPanelValidationError('Usá una velocidad entre 0 y 500 km/h.');
            return false;
        }

        return true;
    }

    /* ── LIVE PREVIEW / APPLY ── */

    function submitWithPanelFilters(closeAfterSubmit) {
        syncHiddenFieldsFromToolbar();
        var panelParams = collectPanelParams();
        if (!validatePanelParams(panelParams, closeAfterSubmit)) {
            return;
        }
        injectPanelParamsIntoForm(panelParams);
        syncFiltersToggleState(panelParams);
        if (!closeAfterSubmit) {
            toolbarForm.dataset.skipNextScroll = 'true';
            toolbarForm.dataset.suppressFallbackSubmit = 'true';
            toolbarForm.dataset.quietLoading = 'true';
        }
        toolbarForm.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
        if (closeAfterSubmit) {
            closePanel();
        }
    }

    function schedulePreviewSubmit() {
        if (!panel.classList.contains('is-open')) {
            return;
        }
        window.clearTimeout(previewSubmitTimer);
        previewSubmitTimer = window.setTimeout(function () {
            if (!panel.classList.contains('is-open')) {
                return;
            }
            submitWithPanelFilters(false);
        }, 350);
    }

    panel.addEventListener('input', function (event) {
        var target = event.target;
        if (!target || !target.matches('.single-range, .dual-range-thumb, .range-number-input')) {
            return;
        }
        clearValidationErrors();
        schedulePreviewSubmit();
    });

    panel.addEventListener('change', function (event) {
        var target = event.target;
        if (!target || !target.matches('.range-number-input')) {
            return;
        }
        schedulePreviewSubmit();
    });

    if (applyBtn) {
        applyBtn.addEventListener('click', function () {
            window.clearTimeout(previewSubmitTimer);
            submitWithPanelFilters(true);
        });
    }

    /* ── RESET ALL FILTER GROUPS ── */

    function resetFilterGroup(hiddenId) {
        var container = panel.querySelector('[data-filter-target="' + hiddenId + '"]');
        if (!container) { return; }
        var hidden  = document.getElementById(hiddenId);
        if (hidden) { hidden.value = ''; }
        var options = container.querySelectorAll('.filter-toggle-option, .filter-segment-option');
        Array.prototype.forEach.call(options, function (opt) {
            opt.classList.toggle('is-selected', opt.getAttribute('data-value') === '');
        });
    }

    function resetPanel() {
        resetFilterGroup('panelFuelType');
        resetFilterGroup('panelAirbagMin');
        resetFilterGroup('panelTransmission');
        clearValidationErrors();

        var priceMin = document.getElementById('panelPriceMin');
        var priceMax = document.getElementById('panelPriceMax');
        if (priceMin) { priceMin.value = ''; }
        if (priceMax) { priceMax.value = ''; }

        var yearMin = document.getElementById('panelYearMin');
        var yearMax = document.getElementById('panelYearMax');
        if (yearMin) { yearMin.value = ''; }
        if (yearMax) { yearMax.value = ''; }

        var hpMin = document.getElementById('panelHpMin');
        var hpMax = document.getElementById('panelHpMax');
        if (hpMin) { hpMin.value = ''; }
        if (hpMax) { hpMax.value = ''; }

        var lowThumbs  = panel.querySelectorAll('.dual-range-low');
        var highThumbs = panel.querySelectorAll('.dual-range-high');
        Array.prototype.forEach.call(lowThumbs,  function (t) { t.value = t.min; });
        Array.prototype.forEach.call(highThumbs, function (t) { t.value = t.max; });
        var fills = panel.querySelectorAll('.dual-range-fill');
        Array.prototype.forEach.call(fills, function (f) { f.style.left = '0%'; f.style.width = '100%'; })

        var consumptionSlider = document.getElementById('panelConsumptionSlider');
        if (consumptionSlider) {
            consumptionSlider.value = consumptionSlider.max;
            updateConsumptionDisplay();
        }

        var speedSlider = document.getElementById('panelMaxSpeedSlider');
        if (speedSlider) {
            speedSlider.value = speedSlider.min;
            updateSpeedDisplay();
        }
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', function () {
            resetPanel();
            removePanelParamsFromForm();
            syncFiltersToggleState({});
            toolbarForm.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
            closePanel();
        });
    }

    /* ── SYNC VEHICLE COUNT FROM TOOLBAR ── */

    var toolbarCount = document.querySelector('.cars-toolbar-count');
    if (toolbarCount && countDisplay) {
        new MutationObserver(function () {
            var match = (toolbarCount.textContent || '').match(/(\d+)/);
            if (match) { countDisplay.textContent = match[1]; }
        }).observe(toolbarCount, { childList: true, subtree: true, characterData: true });
    }

    syncFiltersToggleState(collectPanelParams());

})();
