/*
 * Prevents double-submission by disabling the submit button once a form
 * is actually submitted. Opt-in per form with data-submit-lock="true".
 *
 * Plays nicely with other submit handlers: if any earlier handler calls
 * event.preventDefault() (e.g. client-side validation fails), we do NOT
 * lock the button, because the form will not actually be submitted.
 */
(function () {
    function findSubmitButton(form) {
        return form.querySelector(
            'button[type="submit"], input[type="submit"], button:not([type])'
        );
    }

    function lockButton(button) {
        if (!button || button.disabled) {
            return;
        }
        if (button.dataset.lockedLabel == null) {
            button.dataset.lockedLabel = button.textContent;
            button.textContent = button.dataset.loadingLabel || 'Enviando...';
        }
        button.disabled = true;
        button.setAttribute('aria-busy', 'true');
    }

    function installOn(form) {
        form.addEventListener('submit', function (event) {
            // If an earlier handler already blocked the submit, leave the
            // button enabled so the user can fix errors and retry.
            if (event.defaultPrevented) {
                return;
            }

            var button = findSubmitButton(form);
            if (button && button.disabled) {
                // User somehow clicked again while we were mid-submit -
                // kill this extra submission.
                event.preventDefault();
                return;
            }

            lockButton(button);
        });
    }

    function init() {
        var forms = document.querySelectorAll('form[data-submit-lock="true"]');
        Array.prototype.forEach.call(forms, installOn);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
