(function () {
    var modal = document.getElementById('authRequiredModal');

    if (!modal) {
        return;
    }

    var actionText = modal.querySelector('[data-auth-required-action]');
    var loginLink = modal.querySelector('[data-auth-required-login]');
    var closeElements = Array.prototype.slice.call(modal.querySelectorAll('[data-close-auth-required-modal]'));
    var contextPath = modal.getAttribute('data-context-path') || '';
    var loginUrl = modal.getAttribute('data-login-url') || '/login';
    var lastTrigger = null;
    var INTENT_PATTERN = /^[A-Za-z0-9_-]{1,64}$/;

    function closestAuthRequired(node) {
        while (node && node !== document) {
            if (node.nodeType === 1 && node.getAttribute('data-auth-required') === 'true') {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function appRelativeLocation() {
        var path = window.location.pathname;

        if (contextPath && path.indexOf(contextPath) === 0) {
            path = path.substring(contextPath.length) || '/';
        }

        return path + window.location.search + window.location.hash;
    }

    function buildLoginHref(trigger) {
        var intent = trigger.getAttribute('data-auth-required-intent') || '';
        var redirect = trigger.getAttribute('data-auth-return-url') || appRelativeLocation();
        var url = new URL(loginUrl, window.location.origin);

        url.searchParams.set('redirect', redirect);
        if (INTENT_PATTERN.test(intent)) {
            url.searchParams.set('intent', intent);
        }

        return url.pathname + url.search + url.hash;
    }

    function openModal(trigger) {
        lastTrigger = trigger;
        if (actionText) {
            actionText.textContent = trigger.getAttribute('data-auth-required-action') || 'hacer esta acción';
        }
        if (loginLink) {
            loginLink.setAttribute('href', buildLoginHref(trigger));
        }
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
        if (loginLink) {
            loginLink.focus();
        }
    }

    function closeModal() {
        modal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    function focusResumeTarget(target) {
        var focusable;

        if (!target) {
            return;
        }

        focusable = target.matches('button, a, input, textarea, select')
                ? target
                : target.querySelector('button, a, input, textarea, select');

        target.scrollIntoView({ behavior: 'smooth', block: 'center' });
        target.classList.add('auth-resume-highlight');
        if (focusable && typeof focusable.focus === 'function') {
            focusable.focus();
        }
        window.setTimeout(function () {
            target.classList.remove('auth-resume-highlight');
        }, 1800);
    }

    function findResumeTarget(intent) {
        var targets = document.querySelectorAll('[data-auth-resume-intent]');
        var i;

        for (i = 0; i < targets.length; i += 1) {
            if (targets[i].getAttribute('data-auth-resume-intent') === intent) {
                return targets[i];
            }
        }
        return null;
    }

    function clearIntentParam() {
        var url = new URL(window.location.href);

        url.searchParams.delete('intent');
        window.history.replaceState({}, document.title, url.pathname + url.search + url.hash);
    }

    function resumeIntent() {
        var params = new URLSearchParams(window.location.search);
        var intent = params.get('intent');
        var target;

        if (!intent || !INTENT_PATTERN.test(intent)) {
            return;
        }

        target = findResumeTarget(intent);
        clearIntentParam();

        if (!target) {
            return;
        }

        focusResumeTarget(target);
    }

    document.addEventListener('click', function (event) {
        var trigger = closestAuthRequired(event.target);

        if (!trigger) {
            return;
        }

        event.preventDefault();
        event.stopImmediatePropagation();
        openModal(trigger);
    }, true);

    document.addEventListener('submit', function (event) {
        var trigger = event.submitter && event.submitter.getAttribute('data-auth-required') === 'true'
                ? event.submitter
                : closestAuthRequired(event.target);

        if (!trigger) {
            return;
        }

        event.preventDefault();
        event.stopImmediatePropagation();
        openModal(trigger);
    }, true);

    closeElements.forEach(function (element) {
        element.addEventListener('click', closeModal);
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            closeModal();
        }
    });

    window.setTimeout(resumeIntent, 0);
}());
