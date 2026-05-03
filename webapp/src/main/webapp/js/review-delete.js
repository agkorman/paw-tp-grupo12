(function () {
    'use strict';

    function supportsFetch() {
        return typeof window.fetch === 'function'
            && typeof window.FormData === 'function';
    }

    function hasAttr(node, attr) {
        return node && node.nodeType === 1 && node.getAttribute(attr) !== null;
    }

    function removeReviewCard(form) {
        var node = form;
        while (node && node !== document.body) {
            if (node.tagName === 'ARTICLE') {
                node.remove();
                return;
            }
            node = node.parentNode;
        }
    }

    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (!form || !hasAttr(form, 'data-review-delete-form')) { return; }
        if (!supportsFetch()) { return; }
        if (e.defaultPrevented) { return; }

        e.preventDefault();

        var successMsg = form.getAttribute('data-delete-success') || '';
        var errorMsg = form.getAttribute('data-delete-error') || '';
        var submitBtn = form.querySelector('[type="submit"]');

        if (submitBtn) { submitBtn.disabled = true; }

        window.fetch(form.getAttribute('action'), {
            method: 'POST',
            body: new window.FormData(form),
            credentials: 'same-origin',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        }).then(function (response) {
            if (response.ok) {
                removeReviewCard(form);
                if (window.PawToast) { window.PawToast.show(successMsg, 'success'); }
            } else {
                return response.text().then(function (body) {
                    if (window.PawToast) { window.PawToast.show(body || errorMsg, 'error'); }
                });
            }
        }).catch(function () {
            if (window.PawToast) { window.PawToast.show(errorMsg, 'error'); }
        }).finally(function () {
            if (submitBtn && document.contains(submitBtn)) {
                submitBtn.disabled = false;
            }
        });
    });
}());
