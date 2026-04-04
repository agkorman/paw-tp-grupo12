(function () {
    var nativeSubmit = HTMLFormElement.prototype.submit;

    var buildSearchParams = function (form) {
        var formData = new FormData(form);
        var params = new URLSearchParams();

        formData.forEach(function (value, key) {
            if (typeof value === 'string') {
                var trimmed = value.trim();
                if (trimmed.length > 0) {
                    params.append(key, trimmed);
                }
            } else if (value != null) {
                params.append(key, value);
            }
        });

        return params;
    };

    var setControlsDisabled = function (form, disabled) {
        var controls = form.querySelectorAll('button, select, input, textarea');
        Array.prototype.forEach.call(controls, function (control) {
            control.disabled = disabled;
        });
    };

    var submitEnhancedForm = function (form) {
        if (!form || form.dataset.loading === 'true') {
            return;
        }

        var fragmentUrl = form.dataset.fragmentUrl;
        var targetSelector = form.dataset.target;
        var target = document.querySelector(targetSelector);

        if (!fragmentUrl || !targetSelector || !target) {
            nativeSubmit.call(form);
            return;
        }

        var params = buildSearchParams(form);
        var actionUrl = new URL(form.action, window.location.href);
        var fetchUrl = new URL(fragmentUrl, window.location.href);
        var query = params.toString();
        if (query.length > 0) {
            actionUrl.search = query;
            fetchUrl.search = query;
        } else {
            actionUrl.search = '';
            fetchUrl.search = '';
        }

        form.dataset.loading = 'true';
        setControlsDisabled(form, true);
        target.classList.add('is-loading');
        target.setAttribute('aria-busy', 'true');

        fetch(fetchUrl.toString(), {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('Fragment request failed');
            }
            return response.text();
        }).then(function (html) {
            var parsed = new DOMParser().parseFromString(html, 'text/html');
            var replacement = parsed.querySelector(targetSelector);
            var currentTarget = document.querySelector(targetSelector);

            if (!replacement || !currentTarget) {
                throw new Error('Fragment target not found');
            }

            currentTarget.replaceWith(replacement);
            window.history.replaceState({}, '', actionUrl.pathname + actionUrl.search);
        }).catch(function () {
            nativeSubmit.call(form);
        }).finally(function () {
            delete form.dataset.loading;
            if (document.contains(form)) {
                setControlsDisabled(form, false);
            }
            var currentTarget = document.querySelector(targetSelector);
            if (currentTarget) {
                currentTarget.classList.remove('is-loading');
                currentTarget.removeAttribute('aria-busy');
            }
        });
    };

    document.addEventListener('change', function (event) {
        var target = event.target;
        if (!(target instanceof HTMLSelectElement)) {
            return;
        }

        var form = target.form;
        if (!form || form.dataset.enhancedFilter !== 'true' || form.dataset.autoSubmit !== 'true') {
            return;
        }

        submitEnhancedForm(form);
    });

    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!(form instanceof HTMLFormElement) || form.dataset.enhancedFilter !== 'true') {
            return;
        }

        event.preventDefault();
        submitEnhancedForm(form);
    });
})();
