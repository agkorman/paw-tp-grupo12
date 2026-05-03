(function () {
    function setupSubmitModal() {
        var modal = document.getElementById('requestAdminModal');
        if (!modal) {
            return;
        }
        var form = document.getElementById('requestAdminForm');
        var lastTrigger = null;

        function fieldContainer(field) {
            var node = field;
            while (node && node.nodeType === 1) {
                if (node.classList && node.classList.contains('review-modal-field')) {
                    return node;
                }
                node = node.parentNode;
            }
            return field ? field.parentNode : document.body;
        }

        function fieldKey(field) {
            if (!field || !field.name) {
                return 'generic';
            }
            return field.name.replace(/[^A-Za-z0-9_-]/g, '-');
        }

        function clientErrorId(field) {
            return field.id + 'ClientError';
        }

        function setDescribedBy(field, errorId) {
            if (!field || !errorId) {
                return;
            }
            var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
            if (ids.indexOf(errorId) === -1) {
                ids.push(errorId);
                field.setAttribute('aria-describedby', ids.join(' '));
            }
        }

        function removeDescribedBy(field, errorId) {
            if (!field || !errorId) {
                return;
            }
            var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
                return id && id !== errorId;
            });
            if (ids.length) {
                field.setAttribute('aria-describedby', ids.join(' '));
            } else {
                field.removeAttribute('aria-describedby');
            }
        }

        function requiredMessage(field) {
            if (!form || !field) {
                return '';
            }
            return form.getAttribute('data-msg-required-' + fieldKey(field))
                || form.getAttribute('data-msg-required-generic')
                || '';
        }

        function setInlineError(field, message) {
            if (!field) {
                return;
            }
            var container = fieldContainer(field);
            var errorId = clientErrorId(field);
            var error = container.querySelector('[data-client-error-for="' + field.id + '"]');
            if (!error) {
                error = document.createElement('span');
                error.id = errorId;
                error.className = 'form-error client-form-error';
                error.setAttribute('data-client-error-for', field.id);
                error.setAttribute('role', 'alert');
                container.appendChild(error);
            }
            error.textContent = message;
            error.hidden = false;
            field.classList.add('is-invalid');
            field.setAttribute('aria-invalid', 'true');
            setDescribedBy(field, errorId);
        }

        function clearInlineError(field) {
            if (!field) {
                return;
            }
            var container = fieldContainer(field);
            var errorId = clientErrorId(field);
            var error = container.querySelector('[data-client-error-for="' + field.id + '"]');
            if (error) {
                error.textContent = '';
                error.hidden = true;
            }
            field.classList.remove('is-invalid');
            field.removeAttribute('aria-invalid');
            removeDescribedBy(field, errorId);
        }

        function normalizedValue(field) {
            return field && field.value ? field.value.trim() : '';
        }

        function validateField(field) {
            if (!field || field.disabled) {
                return true;
            }
            clearInlineError(field);
            if (field.required && normalizedValue(field) === '') {
                setInlineError(field, requiredMessage(field));
                return false;
            }
            return true;
        }

        function focusFirstInvalid() {
            var invalid = form ? form.querySelector('[aria-invalid="true"]') : null;
            if (invalid && typeof invalid.focus === 'function') {
                invalid.focus();
            }
        }

        function validateForm() {
            if (!form) {
                return true;
            }
            return Array.prototype.slice.call(form.querySelectorAll('textarea, input, select')).reduce(function (isValid, field) {
                if (field.type === 'hidden') {
                    return isValid;
                }
                return validateField(field) && isValid;
            }, true);
        }

        function findOpenTrigger(node) {
            while (node && node !== document) {
                if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-open-request-admin-modal')) {
                    return node;
                }
                node = node.parentNode;
            }
            return null;
        }

        function findCloseAncestor(node) {
            while (node && node !== document) {
                if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-close-request-admin-modal')) {
                    return node;
                }
                node = node.parentNode;
            }
            return null;
        }

        function open(trigger) {
            lastTrigger = trigger;
            modal.removeAttribute('hidden');
            document.body.classList.add('modal-open');
            var first = modal.querySelector('textarea');
            if (first && typeof first.focus === 'function') {
                first.focus();
            }
        }

        function close() {
            modal.setAttribute('hidden', 'hidden');
            document.body.classList.remove('modal-open');
            if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
                lastTrigger.focus();
            }
            lastTrigger = null;
        }

        if (form) {
            form.noValidate = true;
            Array.prototype.slice.call(form.querySelectorAll('textarea, input, select')).forEach(function (field) {
                if (field.type === 'hidden') {
                    return;
                }
                field.addEventListener('input', function () {
                    validateField(field);
                });
            });

            form.addEventListener('submit', function (event) {
                if (!validateForm()) {
                    event.preventDefault();
                    focusFirstInvalid();
                }
            });
        }

        document.addEventListener('click', function (event) {
            var openTrigger = findOpenTrigger(event.target);
            if (openTrigger) {
                event.preventDefault();
                open(openTrigger);
                return;
            }
            if (findCloseAncestor(event.target)) {
                event.preventDefault();
                close();
            }
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
                close();
            }
        });
    }

    function setupReviewModal() {
        var modal = document.getElementById('adminRequestReviewModal');
        if (!modal) {
            return;
        }

        var submitterField = document.getElementById('adminRequestReviewSubmitter');
        var motivationField = document.getElementById('adminRequestReviewMotivation');
        var bioField = document.getElementById('adminRequestReviewBio');
        var justificationField = document.getElementById('adminRequestReviewJustification');
        var acceptForm = document.getElementById('adminRequestAcceptForm');
        var rejectForm = document.getElementById('adminRequestRejectForm');
        var adminBaseUrl = (modal.getAttribute('data-admin-base-url') || '/admin').replace(/\/$/, '');

        var lastTrigger = null;

        function findOpenTrigger(node) {
            while (node && node !== document) {
                if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-open-admin-request-review')) {
                    return node;
                }
                node = node.parentNode;
            }
            return null;
        }

        function findCloseAncestor(node) {
            while (node && node !== document) {
                if (node.nodeType === 1 && node.hasAttribute && node.hasAttribute('data-close-admin-request-review-modal')) {
                    return node;
                }
                node = node.parentNode;
            }
            return null;
        }

        function open(trigger) {
            var requestId = trigger.getAttribute('data-request-id') || '';
            var submitter = trigger.getAttribute('data-request-submitter') || '';
            var motivation = trigger.getAttribute('data-request-motivation') || '';
            var bio = trigger.getAttribute('data-request-bio') || '';
            var justification = trigger.getAttribute('data-request-justification') || '';

            if (submitterField) {
                submitterField.textContent = submitter;
            }
            if (motivationField) {
                motivationField.value = motivation;
            }
            if (bioField) {
                bioField.value = bio;
            }
            if (justificationField) {
                justificationField.value = justification;
            }

            var basePath = adminBaseUrl + '/admin-requests/' + encodeURIComponent(requestId);
            if (acceptForm) {
                acceptForm.setAttribute('action', basePath + '/accept');
            }
            if (rejectForm) {
                rejectForm.setAttribute('action', basePath + '/reject');
            }

            lastTrigger = trigger;
            modal.removeAttribute('hidden');
            document.body.classList.add('modal-open');
        }

        function close() {
            modal.setAttribute('hidden', 'hidden');
            document.body.classList.remove('modal-open');
            if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
                lastTrigger.focus();
            }
            lastTrigger = null;
        }

        document.addEventListener('click', function (event) {
            var openTrigger = findOpenTrigger(event.target);
            if (openTrigger) {
                event.preventDefault();
                open(openTrigger);
                return;
            }
            if (findCloseAncestor(event.target)) {
                event.preventDefault();
                close();
            }
        });

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
                close();
            }
        });
    }

    setupSubmitModal();
    setupReviewModal();
}());
