(function () {
    'use strict';

    var toast = document.getElementById('globalToast');
    if (!toast) {
        return;
    }

    var messageNode = toast.querySelector('[data-toast-message]');
    var closeButton = toast.querySelector('[data-toast-close]');
    var hideTimer = null;

    function hideToast() {
        toast.hidden = true;
        toast.classList.remove('global-toast-success', 'global-toast-error', 'global-toast-visible');
        if (hideTimer) {
            window.clearTimeout(hideTimer);
            hideTimer = null;
        }
    }

    function showToast(message, type) {
        if (!messageNode) {
            return;
        }
        if (hideTimer) {
            window.clearTimeout(hideTimer);
        }
        messageNode.textContent = message || '';
        toast.classList.remove('global-toast-success', 'global-toast-error');
        toast.classList.add(type === 'error' ? 'global-toast-error' : 'global-toast-success');
        toast.hidden = false;
        window.requestAnimationFrame(function () {
            toast.classList.add('global-toast-visible');
        });
        hideTimer = window.setTimeout(hideToast, 5200);
    }

    if (closeButton) {
        closeButton.addEventListener('click', hideToast);
    }

    window.PawToast = {
        show: showToast,
        hide: hideToast
    };
})();
