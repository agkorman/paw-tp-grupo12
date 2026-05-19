(function () {
    'use strict';

    function findReplyItem(node) {
        return node.closest('[data-reply-item]');
    }

    function openEdit(replyItem) {
        var body = replyItem.querySelector('[data-reply-body]');
        var form = replyItem.querySelector('[data-reply-edit-form]');
        if (!form) {
            return;
        }
        if (body) {
            body.hidden = true;
        }
        form.hidden = false;
        var textarea = form.querySelector('textarea[name="body"]');
        if (textarea) {
            textarea.focus();
            var value = textarea.value;
            textarea.setSelectionRange(value.length, value.length);
        }
    }

    function closeEdit(replyItem) {
        var body = replyItem.querySelector('[data-reply-body]');
        var form = replyItem.querySelector('[data-reply-edit-form]');
        if (!form) {
            return;
        }
        form.hidden = true;
        if (body) {
            body.hidden = false;
        }
    }

    document.addEventListener('click', function (event) {
        var editTrigger = event.target.closest('[data-edit-reply-trigger]');
        if (editTrigger) {
            event.preventDefault();
            var replyItem = findReplyItem(editTrigger);
            if (replyItem) {
                openEdit(replyItem);
            }
            return;
        }

        var cancelTrigger = event.target.closest('[data-cancel-reply-edit]');
        if (cancelTrigger) {
            event.preventDefault();
            var item = findReplyItem(cancelTrigger);
            if (item) {
                closeEdit(item);
            }
        }
    });
})();
