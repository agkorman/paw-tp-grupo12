/*
 * Prevents double-submission by disabling the submit button(s) once a form
 * is actually submitted. Applied to every POST form by default; opt out per
 * form with data-submit-lock="false".
 *
 * Plays nicely with other submit handlers: if any earlier handler calls
 * event.preventDefault() (e.g. an enhanced flow handles the submit), we do NOT
 * lock the button, because the form will not actually be submitted.
 */
(function () {
    function findSubmitButtons(form) {
        var formId = form.id;
        var inside = form.querySelectorAll(
            'button[type="submit"], input[type="submit"], button:not([type])'
        );
        var outside = formId
            ? document.querySelectorAll(
                  'button[type="submit"][form="' + formId + '"], input[type="submit"][form="' + formId + '"]'
              )
            : [];
        var all = [];
        Array.prototype.forEach.call(inside, function (b) { all.push(b); });
        Array.prototype.forEach.call(outside, function (b) {
            if (all.indexOf(b) === -1) all.push(b);
        });
        return all;
    }

    function lockButton(button) {
        if (!button || button.disabled) {
            return;
        }
        if (button.dataset.lockedLabel == null) {
            button.dataset.lockedLabel = button.textContent;
            if (button.dataset.loadingLabel) {
                button.textContent = button.dataset.loadingLabel;
            }
        }
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');
    }

    function anyDisabled(buttons) {
        for (var i = 0; i < buttons.length; i++) {
            if (buttons[i].disabled) return true;
        }
        return false;
    }

    function installOn(form) {
        form.addEventListener('submit', function (event) {
            // If an earlier handler already blocked the submit, leave the
            // button enabled so the user can fix errors and retry.
            if (event.defaultPrevented) {
                return;
            }

            // Forms gated by a confirmation modal submit twice: once to open
            // the modal (which prevents default) and once after the user
            // confirms. Skip locking on the pre-confirmation pass; lock only
            // when the form is actually being submitted for real.
            if (
                form.hasAttribute('data-confirm-modal') &&
                form.getAttribute('data-confirmed') !== 'true'
            ) {
                return;
            }

            var buttons = findSubmitButtons(form);
            if (anyDisabled(buttons)) {
                // User clicked again while we were mid-submit - kill the extra.
                event.preventDefault();
                return;
            }

            for (var i = 0; i < buttons.length; i++) {
                lockButton(buttons[i]);
            }
        });
    }

    function shouldLock(form) {
        if (form.dataset.submitLock === 'false') return false;
        var method = (form.getAttribute('method') || 'get').toLowerCase();
        return method === 'post';
    }

    function init() {
        var forms = document.querySelectorAll('form');
        Array.prototype.forEach.call(forms, function (form) {
            if (shouldLock(form)) installOn(form);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
