(function () {
    var nativeSubmit = HTMLFormElement.prototype.submit;

    function supportsFetchFormData() {
        return typeof window.fetch === 'function'
            && typeof window.FormData === 'function'
            && typeof window.Promise === 'function';
    }

    function supportsEnhancedReplyForm() {
        return supportsFetchFormData() && typeof window.DOMParser === 'function';
    }

    function isLoginRedirect(response) {
        if (!response || !response.redirected) {
            return false;
        }

        try {
            return new URL(response.url).pathname.indexOf('/login') >= 0;
        } catch (ignored) {
            return false;
        }
    }

    function setControlsDisabled(form, disabled) {
        var controls = form.querySelectorAll('button, select, textarea, input');

        Array.prototype.forEach.call(controls, function (control) {
            control.disabled = disabled;
        });
    }

    function showReplyError(form, message) {
        var error = form.querySelector('[data-review-reply-error]');

        if (!error) {
            error = document.createElement('p');
            error.className = 'review-reply-inline-error';
            error.setAttribute('data-review-reply-error', 'true');
            error.setAttribute('role', 'alert');
            form.appendChild(error);
        }

        error.textContent = message;
    }

    function submitEnhancedReplyForm(form) {
        var targetSelector = form.dataset.target;
        var target = targetSelector ? document.querySelector(targetSelector) : null;
        var body = form.querySelector('textarea[name="body"]');
        var formData;

        if (form.dataset.loading === 'true') {
            return;
        }

        if (!target) {
            nativeSubmit.call(form);
            return;
        }

        if (body && body.value.trim().length === 0) {
            showReplyError(form, form.getAttribute('data-msg-empty') || '');
            body.focus();
            return;
        }

        formData = new FormData(form);
        form.dataset.loading = 'true';
        setControlsDisabled(form, true);
        target.classList.add('is-loading');
        target.setAttribute('aria-busy', 'true');

        fetch(form.action, {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin'
        }).then(function (response) {
            if (isLoginRedirect(response)) {
                window.location.href = response.url;
                return '';
            }
            if (!response.ok) {
                throw new Error('Review reply request failed');
            }
            return response.text();
        }).then(function (html) {
            var parsed;
            var replacement;
            var currentTarget;

            if (!html) {
                return;
            }

            parsed = new DOMParser().parseFromString(html, 'text/html');
            replacement = parsed.querySelector(targetSelector);
            currentTarget = document.querySelector(targetSelector);

            if (!replacement || !currentTarget) {
                throw new Error('Review feed target not found');
            }

            currentTarget.replaceWith(replacement);
        }).catch(function () {
            showReplyError(form, form.getAttribute('data-msg-error') || '');
        }).finally(function () {
            var currentTarget;

            delete form.dataset.loading;

            if (document.contains(form)) {
                setControlsDisabled(form, false);
            }

            currentTarget = document.querySelector(targetSelector);
            if (currentTarget) {
                currentTarget.classList.remove('is-loading');
                currentTarget.removeAttribute('aria-busy');
            }
        });
    }

    document.addEventListener('submit', function (event) {
        var form = event.target;

        if (!(form instanceof HTMLFormElement) || form.dataset.enhancedReviewReply !== 'true') {
            return;
        }

        if (!supportsEnhancedReplyForm()) {
            return;
        }

        event.preventDefault();
        submitEnhancedReplyForm(form);
    });
}());
