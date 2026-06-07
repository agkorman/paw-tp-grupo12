(function () {
    'use strict';

    function shouldStartOpen(form, intent) {
        if (form.getAttribute('data-reply-has-error') === 'true') {
            return true;
        }
        return Boolean(intent) && form.getAttribute('data-reply-intent') === intent;
    }

    function setExpanded(toggle, panel, expanded) {
        toggle.setAttribute('aria-expanded', expanded ? 'true' : 'false');
        panel.hidden = !expanded;
    }

    var params = new URLSearchParams(window.location.search);
    var intent = params.get('intent');

    var forms = document.querySelectorAll('.review-reply-form');
    forms.forEach(function (form) {
        var toggle = form.querySelector('[data-reply-toggle]');
        var panel = form.querySelector('[data-reply-panel]');
        if (!toggle || !panel) {
            return;
        }

        form.classList.add('review-reply-form--js');
        setExpanded(toggle, panel, shouldStartOpen(form, intent));

        toggle.addEventListener('click', function () {
            var willOpen = toggle.getAttribute('aria-expanded') !== 'true';
            setExpanded(toggle, panel, willOpen);
            if (willOpen) {
                var textarea = panel.querySelector('textarea[name="body"]');
                if (textarea) {
                    textarea.focus();
                }
            }
        });
    });
})();
