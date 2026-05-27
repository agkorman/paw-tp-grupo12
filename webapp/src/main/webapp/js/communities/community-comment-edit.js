(function () {
    document.addEventListener('click', function (event) {
        var editTrigger = event.target instanceof Element && event.target.closest('[data-edit-community-comment-trigger]');
        if (editTrigger) {
            var comment = editTrigger.closest('.community-comment-row');
            if (!comment) {
                return;
            }
            var body = comment.querySelector('[data-community-comment-body]');
            var form = comment.querySelector('[data-community-comment-edit-form]');
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

        var cancelTrigger = event.target instanceof Element && event.target.closest('[data-cancel-community-comment-edit]');
        if (!cancelTrigger) {
            return;
        }
        var comment = cancelTrigger.closest('.community-comment-row');
        if (!comment) {
            return;
        }
        var body = comment.querySelector('[data-community-comment-body]');
        var form = comment.querySelector('[data-community-comment-edit-form]');
        if (form) {
            form.hidden = true;
        }
        if (body) {
            body.hidden = false;
        }
    });
})();
