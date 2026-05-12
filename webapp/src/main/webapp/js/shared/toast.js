(function () {
    'use strict';

    var toast = document.getElementById('globalToast');
    if (!toast) {
        return;
    }

    var messageNode = toast.querySelector('[data-toast-message]');
    var closeButton = toast.querySelector('[data-toast-close]');
    var hideTimer = null;
    var hideAnimationTimer = null;
    var DEFAULT_TIMEOUT = 6000;

    function hideToast() {
        if (hideTimer) {
            window.clearTimeout(hideTimer);
            hideTimer = null;
        }
        if (hideAnimationTimer) {
            window.clearTimeout(hideAnimationTimer);
        }
        toast.classList.remove('global-toast-visible');
        toast.classList.add('global-toast-hiding');
        hideAnimationTimer = window.setTimeout(function () {
            toast.hidden = true;
            toast.classList.remove('global-toast-success', 'global-toast-error', 'global-toast-hiding');
            toast.setAttribute('role', 'status');
            toast.setAttribute('aria-live', 'polite');
            hideAnimationTimer = null;
        }, 300);
    }

    function showToast(message, type, options) {
        if (!messageNode) {
            return;
        }
        if (hideTimer) {
            window.clearTimeout(hideTimer);
        }
        var timeout = options && typeof options.timeout === 'number'
            ? options.timeout
            : DEFAULT_TIMEOUT;
        var resolvedType = type === 'error' ? 'error' : 'success';
        if (hideAnimationTimer) {
            window.clearTimeout(hideAnimationTimer);
            hideAnimationTimer = null;
        }
        messageNode.textContent = message || '';
        toast.classList.remove('global-toast-success', 'global-toast-error', 'global-toast-hiding');
        toast.classList.add(resolvedType === 'error' ? 'global-toast-error' : 'global-toast-success');
        toast.setAttribute('role', resolvedType === 'error' ? 'alert' : 'status');
        toast.setAttribute('aria-live', resolvedType === 'error' ? 'assertive' : 'polite');
        toast.hidden = false;
        window.requestAnimationFrame(function () {
            toast.classList.add('global-toast-visible');
        });
        hideTimer = window.setTimeout(hideToast, timeout);
    }

    if (closeButton) {
        closeButton.addEventListener('click', hideToast);
    }

    window.PawToast = {
        show: showToast,
        hide: hideToast
    };

    var initialMessage = toast.getAttribute('data-toast-initial-message');
    if (initialMessage) {
        var initialType = toast.getAttribute('data-toast-initial-type');
        var initialTimeout = Number(toast.getAttribute('data-toast-initial-timeout'));
        showToast(initialMessage, initialType, {
            timeout: Number.isNaN(initialTimeout) ? 6000 : initialTimeout
        });
    }
})();
