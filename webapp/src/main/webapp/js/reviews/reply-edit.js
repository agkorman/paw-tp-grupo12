(function () {
    document.addEventListener('click', function (event) {
        var editTrigger = event.target instanceof Element && event.target.closest('[data-edit-reply-trigger]');
        if (editTrigger) {
            var reply = editTrigger.closest('.review-reply');
            if (!reply) {
                return;
            }
            var body = reply.querySelector('[data-reply-body]');
            var form = reply.querySelector('[data-reply-edit-form]');
            var textarea = form && form.querySelector('textarea[name="body"]');
            if (!form || !body || !textarea) {
                return;
            }
            body.hidden = true;
            form.hidden = false;
            textarea.focus();
            textarea.setSelectionRange(textarea.value.length, textarea.value.length);
            return;
        }

        var cancelTrigger = event.target instanceof Element && event.target.closest('[data-cancel-reply-edit]');
        if (!cancelTrigger) {
            return;
        }
        var reply = cancelTrigger.closest('.review-reply');
        if (!reply) {
            return;
        }
        var body = reply.querySelector('[data-reply-body]');
        var form = reply.querySelector('[data-reply-edit-form]');
        if (form) {
            form.hidden = true;
        }
        if (body) {
            body.hidden = false;
        }
    });
})();
