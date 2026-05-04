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
    var priceError = document.getElementById('panelPriceError');
    var consumptionSection = document.getElementById('panelConsumptionSection');
    var consumptionSlider = document.getElementById('panelConsumptionSlider');
    var fuelConsumptionSubsection = document.getElementById('panelFuelConsumptionSubsection');
    var previewSubmitTimer = null;
    var INVALID_PARAM = '__invalid__';

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
        updateConsumptionFilterVisibility();
        clearValidationErrors();
        schedulePreviewSubmit();
    });

    /* ── SINGLE-RANGE SLIDERS with "Hasta / Desde" display ── */

    function updateSingleRangeFill(slider, mode) {
        var min = parseFloat(slider.min || 0);
        var max = parseFloat(slider.max || 100);
        var value = parseFloat(slider.value);
        var percent = max > min ? ((value - min) / (max - min) * 100) : 0;
        percent = Math.min(100, Math.max(0, percent));

        if (mode === 'to-end') {
            slider.style.background = 'linear-gradient(to right, rgba(229, 226, 225, 0.14) 0%, rgba(229, 226, 225, 0.14) ' +
                    percent + '%, var(--tertiary) ' + percent + '%, var(--tertiary) 100%)';
        } else {
            slider.style.background = 'linear-gradient(to right, var(--tertiary) 0%, var(--tertiary) ' +
                    percent + '%, rgba(229, 226, 225, 0.14) ' + percent + '%, rgba(229, 226, 225, 0.14) 100%)';
        }
    }

    function initSingleRange(sliderId, displayId, prefix, unit, fillMode) {
        var slider  = document.getElementById(sliderId);
        var display = document.getElementById(displayId);
        if (!slider || !display) { return function () {}; }

        function updateDisplay() {
            var valueNode;
            updateSingleRangeFill(slider, fillMode);
            display.textContent = '';
            if (prefix) {
                display.appendChild(document.createTextNode(prefix + ' '));
            }
            valueNode = document.createElement('strong');
            valueNode.textContent = slider.value;
            display.appendChild(valueNode);
            if (unit) {
                display.appendChild(document.createTextNode(' ' + unit));
            }
        }

        slider.addEventListener('input', updateDisplay);
        updateDisplay();
        return updateDisplay;
    }

    var updateConsumptionDisplay = initSingleRange('panelConsumptionSlider', 'panelConsumptionDisplay', 'Hasta', 'L/100km', 'from-start');
    var updateSpeedDisplay       = initSingleRange('panelMaxSpeedSlider',    'panelMaxSpeedDisplay',    'Desde', 'km/h', 'to-end');

    function isElectricOnlyFilter() {
        var fuelTypeInput = document.getElementById('panelFuelType');
        return fuelTypeInput && fuelTypeInput.value === 'electric';
    }

    function updateConsumptionFilterVisibility() {
        var hidden = isElectricOnlyFilter();
        if (consumptionSection) {
            consumptionSection.hidden = hidden;
        }
        if (fuelConsumptionSubsection) {
            fuelConsumptionSubsection.hidden = hidden;
        }
        if (hidden && consumptionSlider) {
            consumptionSlider.value = consumptionSlider.max;
            updateConsumptionDisplay();
        }
    }

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

    var PRICE_SCALE_POINTS = [
        { pos: 0,    value: 10000,  step: 1000 },
        { pos: 250,  value: 25000,  step: 1000 },
        { pos: 500,  value: 50000,  step: 1000 },
        { pos: 700,  value: 100000, step: 5000 },
        { pos: 850,  value: 150000, step: 10000 },
        { pos: 1000, value: 250000, step: 25000 }
    ];

    function interpolatePriceScale(pos) {
        if (pos <= PRICE_SCALE_POINTS[0].pos) {
            return PRICE_SCALE_POINTS[0].value;
        }
        for (var i = 1; i < PRICE_SCALE_POINTS.length; i++) {
            var previous = PRICE_SCALE_POINTS[i - 1];
            var next = PRICE_SCALE_POINTS[i];
            if (pos <= next.pos) {
                var percent = (pos - previous.pos) / (next.pos - previous.pos);
                var raw = previous.value + (next.value - previous.value) * percent;
                return Math.round(raw / next.step) * next.step;
            }
        }
        return PRICE_SCALE_POINTS[PRICE_SCALE_POINTS.length - 1].value;
    }

    function pricePosToValue(pos, sliderMax, realMax) {
        if (pos >= sliderMax) {
            return realMax;
        }
        return interpolatePriceScale(pos);
    }

    function priceValueToPos(value, sliderMax) {
        if (value >= PRICE_SCALE_POINTS[PRICE_SCALE_POINTS.length - 1].value) {
            return sliderMax;
        }
        if (value <= PRICE_SCALE_POINTS[0].value) {
            return 0;
        }
        for (var i = 1; i < PRICE_SCALE_POINTS.length; i++) {
            var previous = PRICE_SCALE_POINTS[i - 1];
            var next = PRICE_SCALE_POINTS[i];
            if (value <= next.value) {
                var percent = (value - previous.value) / (next.value - previous.value);
                return Math.round(previous.pos + (next.pos - previous.pos) * percent);
            }
        }
        return sliderMax;
    }

    function snapToSliderStep(slider, value) {
        var minValue = parseFloat(slider.min || 0);
        var maxValue = parseFloat(slider.max || 100);
        var stepValue = slider.step === 'any' ? 1 : parseFloat(slider.step || 1);

        if (!Number.isFinite(value)) {
            return minValue;
        }
        if (!Number.isFinite(stepValue) || stepValue <= 0) {
            stepValue = 1;
        }

        var snapped = minValue + Math.round((value - minValue) / stepValue) * stepValue;
        return Math.min(maxValue, Math.max(minValue, snapped));
    }

    function parseCurrencyValue(value) {
        var text = String(value || '').trim();
        if (text === '') {
            return NaN;
        }
        if (!/^\$?\s*(?:\d+|\d{1,3}(?:,\d{3})+)$/.test(text)) {
            return NaN;
        }
        return Number(text.replace(/[$,\s]/g, ''));
    }

    function currencyParamValue(value) {
        if (String(value || '').trim() === '') {
            return '';
        }
        var parsed = parseCurrencyValue(value);
        return Number.isFinite(parsed) ? String(parsed) : INVALID_PARAM;
    }

    function formatCurrencyValue(value) {
        var parsed = typeof value === 'number' ? value : parseCurrencyValue(value);
        if (!Number.isFinite(parsed)) {
            return '';
        }
        return '$' + Math.round(parsed).toLocaleString('en-US');
    }

    function parsePlainNumberValue(value) {
        var text = String(value || '').trim();
        if (text === '') {
            return NaN;
        }
        if (!/^\d+$/.test(text)) {
            return NaN;
        }
        return Number(text);
    }

    function numberParamValue(value) {
        if (String(value || '').trim() === '') {
            return '';
        }
        var parsed = parsePlainNumberValue(value);
        return Number.isFinite(parsed) ? String(parsed) : INVALID_PARAM;
    }

    function isAllowedTypedCharacter(value, isCurrency) {
        return isCurrency ? /^[\d$,\s]*$/.test(value) : /^\d*$/.test(value);
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

        var scale   = container.getAttribute('data-scale') || '';
        var isLog   = scale === 'log';
        var isPrice = scale === 'price';
        var min     = parseFloat(container.getAttribute('data-range-min') || 0);
        var max     = parseFloat(container.getAttribute('data-range-max') || 1500);
        var realMin = parseFloat(container.getAttribute('data-real-min') || min);
        var realMax = (isLog || isPrice) ? parseFloat(container.getAttribute('data-real-max') || max) : max;

        function posToValue(pos) {
            if (isPrice) {
                return pricePosToValue(pos, max, realMax);
            }
            return isLog ? logPosToValue(pos, max, realMax) : pos;
        }
        function valueToPos(value) {
            if (isPrice) {
                return priceValueToPos(value, max);
            }
            return isLog ? logValueToPos(value, max, realMax) : value;
        }

        function inputStep(input) {
            var step = input ? parseFloat(input.step || input.getAttribute('data-step') || 1) : 1;
            return Number.isFinite(step) && step > 0 ? step : 1;
        }

        function parseRangeInput(input) {
            if (!input) {
                return NaN;
            }
            if (input.getAttribute('data-max-label') === String(input.value || '').trim()) {
                return Number(input.getAttribute('data-max'));
            }
            return input.hasAttribute('data-currency-input') ? parseCurrencyValue(input.value) : parsePlainNumberValue(input.value);
        }

        function setRangeInputValue(input, value, emptyBoundaryValue) {
            if (!input) {
                return;
            }
            var boundary = input.getAttribute('data-boundary');
            if (boundary && !input.getAttribute('data-max-label')) {
                var boundaryAttr = boundary === 'min' ? input.getAttribute('data-min') : input.getAttribute('data-max');
                if (boundaryAttr !== null && Number(value) === Number(boundaryAttr)) {
                    input.value = '';
                    return;
                }
            }
            if (input.hasAttribute('data-currency-input') && input.getAttribute('data-max-label')
                    && value >= Number(input.getAttribute('data-max'))) {
                input.value = input.getAttribute('data-max-label');
            } else if (input.hasAttribute('data-currency-input')) {
                input.value = formatCurrencyValue(value);
            } else {
                input.value = value;
            }
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

        function clamp(activeThumb) {
            lowThumb.value = snapToSliderStep(lowThumb, parseFloat(lowThumb.value));
            highThumb.value = snapToSliderStep(highThumb, parseFloat(highThumb.value));
            var low  = parseFloat(lowThumb.value);
            var high = parseFloat(highThumb.value);
            var minGap = Math.max(
                    lowThumb.step === 'any' ? 1 : parseFloat(lowThumb.step || 1),
                    highThumb.step === 'any' ? 1 : parseFloat(highThumb.step || 1)
            );
            if (!Number.isFinite(minGap) || minGap <= 0) {
                minGap = 1;
            }

            if (activeThumb === 'low' && low >= high) {
                lowThumb.value = snapToSliderStep(lowThumb, high - minGap);
            } else if (activeThumb === 'high' && high <= low) {
                highThumb.value = snapToSliderStep(highThumb, low + minGap);
            } else if (high - low < minGap) {
                highThumb.value = snapToSliderStep(highThumb, low + minGap);
                if (parseFloat(highThumb.value) > max) {
                    highThumb.value = max;
                    lowThumb.value = snapToSliderStep(lowThumb, max - minGap);
                }
            }
        }

        function syncLowInputFromThumb() {
            var realValue = posToValue(parseFloat(lowThumb.value));
            setRangeInputValue(lowInput, realValue, min);
        }

        function syncHighInputFromThumb() {
            var realValue = posToValue(parseFloat(highThumb.value));
            setRangeInputValue(highInput, realValue, realMax);
        }

        function prepareTypedInput(input) {
            if (!input) { return; }
            input.addEventListener('beforeinput', function (event) {
                if (!event.data) { return; }
                if (event.data.length === 1 && !isAllowedTypedCharacter(event.data, input.hasAttribute('data-currency-input'))) {
                    event.preventDefault();
                }
            });
        }

        function syncTypedInput(input, thumb, fallbackValue, isLowInput) {
            if (!input) { return; }
            var rawValue = input.value;
            if (rawValue.trim() === '') {
                thumb.value = fallbackValue;
                updateFill();
                return;
            }
            if (input.hasAttribute('data-currency-input')) {
                var digits = rawValue.replace(/[^\d]/g, '');
                if (input.getAttribute('data-max-label') === rawValue.trim()) {
                    digits = input.getAttribute('data-max') || '';
                }
                if (digits === '') {
                    input.value = '';
                    thumb.value = fallbackValue;
                    updateFill();
                    return;
                }
                input.value = formatCurrencyValue(Number(digits));
            } else {
                var numberDigits = rawValue.replace(/[^\d]/g, '');
                if (numberDigits !== rawValue) {
                    input.value = numberDigits;
                }
                if (numberDigits === '') {
                    thumb.value = fallbackValue;
                    updateFill();
                    return;
                }
            }

            var value = parseRangeInput(input);
            if (!Number.isFinite(value) || value < realMin || value > realMax) {
                return;
            }

            var otherValue = isLowInput
                    ? (highInput && highInput.value !== '' ? parseRangeInput(highInput) : realMax)
                    : (lowInput && lowInput.value !== '' ? parseRangeInput(lowInput) : min);
            if (Number.isFinite(otherValue) && ((isLowInput && value >= otherValue) || (!isLowInput && value <= otherValue))) {
                return;
            }

            thumb.value = snapToSliderStep(thumb, valueToPos(value));
            updateFill();
        }

        prepareTypedInput(lowInput);
        prepareTypedInput(highInput);

        lowThumb.addEventListener('input', function () {
            clamp('low'); updateFill();
            syncLowInputFromThumb();
        });

        highThumb.addEventListener('input', function () {
            clamp('high'); updateFill();
            syncHighInputFromThumb();
        });

        if (lowInput) {
            lowInput.addEventListener('input', function () {
                syncTypedInput(lowInput, lowThumb, min, true);
                clearValidationErrors();
            });
            lowInput.addEventListener('change', function () {
                var v = parseRangeInput(lowInput);
                if (!isNaN(v)) {
                    var highValue = highInput && highInput.value !== '' ? parseRangeInput(highInput) : realMax;
                    if (v < realMin || v > realMax || (Number.isFinite(highValue) && v > highValue)) {
                        clearValidationErrors();
                        return;
                    }
                    var pos = valueToPos(v);
                    var maxLow = parseFloat(highThumb.value) - inputStep(lowThumb);
                    lowThumb.value = snapToSliderStep(lowThumb, Math.min(Math.max(pos, min), maxLow));
                    syncLowInputFromThumb();
                    updateFill();
                }
                clearValidationErrors();
            });
        }
        if (highInput) {
            highInput.addEventListener('input', function () {
                syncTypedInput(highInput, highThumb, realMax, false);
                clearValidationErrors();
            });
            highInput.addEventListener('change', function () {
                var v = parseRangeInput(highInput);
                if (!isNaN(v)) {
                    var lowValue = lowInput && lowInput.value !== '' ? parseRangeInput(lowInput) : min;
                    if (v < realMin || v > realMax || (Number.isFinite(lowValue) && v < lowValue)) {
                        clearValidationErrors();
                        return;
                    }
                    var pos = valueToPos(v);
                    var minHigh = parseFloat(lowThumb.value) + inputStep(highThumb);
                    highThumb.value = snapToSliderStep(highThumb, Math.max(Math.min(pos, max), minHigh));
                    syncHighInputFromThumb();
                    updateFill();
                }
                clearValidationErrors();
            });
        }

        // Initialize slider positions from existing input values.
        if (lowInput && lowInput.value !== '') {
            lowThumb.value = snapToSliderStep(lowThumb, valueToPos(parseRangeInput(lowInput)));
        }
        if (highInput && highInput.value !== '') {
            highThumb.value = snapToSliderStep(highThumb, valueToPos(parseRangeInput(highInput)));
        }
        clamp();

        updateFill();
        syncLowInputFromThumb();
        syncHighInputFromThumb();
    });

    /* ── COLLECT PANEL PARAMS ── */

    var RANGE_DEFAULTS = { fuelConsumptionMax: '30', maxSpeedMin: '0' };

    function boundaryParamValue(el, value) {
        if (value === INVALID_PARAM || value === '') {
            return value;
        }
        if (el.getAttribute('data-keep-boundary-param') === 'true') {
            return value;
        }
        var boundary = el.getAttribute('data-boundary');
        if (!boundary) {
            return value;
        }
        var boundaryValue = boundary === 'min' ? el.getAttribute('data-min') : el.getAttribute('data-max');
        if (boundaryValue !== null && Number(value) === Number(boundaryValue)) {
            return '';
        }
        return value;
    }

    function collectPanelParams() {
        var params = {};

        var inputs = panel.querySelectorAll('input[name], select[name]');
        Array.prototype.forEach.call(inputs, function (el) {
            if (el.disabled) {
                return;
            }
            if (el.type === 'radio') {
                if (el.checked && el.value !== '') { params[el.name] = el.value; }
            } else if (el.type === 'range') {
                var def = RANGE_DEFAULTS[el.name];
                if (el.value !== '' && (def === undefined || el.value !== def)) {
                    params[el.name] = el.value;
                }
            } else if (el.hasAttribute('data-currency-input')) {
                var maxLabel = el.getAttribute('data-max-label');
                var currencyValue = maxLabel && String(el.value || '').trim() === maxLabel
                        ? el.getAttribute('data-max')
                        : currencyParamValue(el.value);
                currencyValue = boundaryParamValue(el, currencyValue);
                if (currencyValue !== '') { params[el.name] = currencyValue; }
            } else if (el.hasAttribute('data-number-input')) {
                var numberValue = numberParamValue(el.value);
                numberValue = boundaryParamValue(el, numberValue);
                if (numberValue !== '') { params[el.name] = numberValue; }
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
        if (priceError) {
            priceError.textContent = '';
            priceError.setAttribute('hidden', '');
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

    function showPriceValidationError(message, focusFirstField) {
        var priceMin = document.getElementById('panelPriceMin');
        var priceMax = document.getElementById('panelPriceMax');
        if (priceError) {
            priceError.textContent = message;
            priceError.removeAttribute('hidden');
        }
        [priceMin, priceMax].forEach(function (field) {
            if (!field) { return; }
            field.classList.add('is-invalid');
            field.setAttribute('aria-invalid', 'true');
        });
        if (focusFirstField && priceMin) { priceMin.focus(); }
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
        if (value === INVALID_PARAM) {
            return false;
        }
        var parsed = parsePlainNumberValue(value);
        return Number.isFinite(parsed) && parsed >= min && parsed <= max;
    }

    function validatePanelParams(panelParams, focusOnError) {
        clearValidationErrors();

        if (!areAllowedCsvValues(panelParams.fuelType, ['combustion', 'hybrid', 'electric'])) {
            showPanelValidationError(panel.getAttribute('data-msg-fuel-type-invalid') || '');
            return false;
        }
        if (!isAllowedValue(panelParams.transmission, ['', 'automatic', 'manual'])) {
            showPanelValidationError(panel.getAttribute('data-msg-transmission-invalid') || '');
            return false;
        }
        if (!isAllowedValue(panelParams.airbagMin, ['', '2', '4', '6', '8', '10'])) {
            showPanelValidationError(panel.getAttribute('data-msg-airbags-invalid') || '');
            return false;
        }
        if (!isValidNumberParam(panelParams.yearMin, 1950, 2026)
                || !isValidNumberParam(panelParams.yearMax, 1950, 2026)) {
            showYearValidationError(panel.getAttribute('data-msg-year-range') || '', focusOnError);
            return false;
        }

        if (panelParams.yearMin !== undefined && panelParams.yearMax !== undefined
                && Number(panelParams.yearMin) > Number(panelParams.yearMax)) {
            showYearValidationError(panel.getAttribute('data-msg-year-order') || '', focusOnError);
            return false;
        }

        if (!isValidNumberParam(panelParams.priceMin, 10000, 5000000)
                || !isValidNumberParam(panelParams.priceMax, 10000, 5000000)) {
            showPriceValidationError(panel.getAttribute('data-msg-price-range') || '', focusOnError);
            return false;
        }

        if (panelParams.priceMin !== undefined && panelParams.priceMax !== undefined
                && Number(panelParams.priceMin) > Number(panelParams.priceMax)) {
            showPriceValidationError(panel.getAttribute('data-msg-price-order') || '', focusOnError);
            return false;
        }

        if (!isValidNumberParam(panelParams.horsepowerMin, 50, 800)
                || !isValidNumberParam(panelParams.horsepowerMax, 50, 800)) {
            showHpValidationError(panel.getAttribute('data-msg-horsepower-range') || '', focusOnError);
            return false;
        }

        if (panelParams.horsepowerMin !== undefined && panelParams.horsepowerMax !== undefined
                && Number(panelParams.horsepowerMin) > Number(panelParams.horsepowerMax)) {
            showHpValidationError(panel.getAttribute('data-msg-horsepower-order') || '', focusOnError);
            return false;
        }

        if (!isValidNumberParam(panelParams.fuelConsumptionMax, 0, 30)) {
            showPanelValidationError(panel.getAttribute('data-msg-consumption-range') || '');
            return false;
        }
        if (!isValidNumberParam(panelParams.maxSpeedMin, 0, 500)) {
            showPanelValidationError(panel.getAttribute('data-msg-speed-range') || '');
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
        if (priceMax) { priceMax.value = priceMax.getAttribute('data-max-label') || ''; }

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

        if (consumptionSlider) {
            consumptionSlider.value = consumptionSlider.max;
            updateConsumptionDisplay();
        }

        var speedSlider = document.getElementById('panelMaxSpeedSlider');
        if (speedSlider) {
            speedSlider.value = speedSlider.min;
            updateSpeedDisplay();
        }
        updateConsumptionFilterVisibility();
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
    updateConsumptionFilterVisibility();

})();
