(function () {
    var form = document.getElementById('communityCreateForm');
    if (!form) {
        return;
    }

    var topicGroup = form.querySelector('[data-community-topic-chips]');
    var topicCheckboxes = topicGroup
        ? topicGroup.querySelectorAll('input[type="checkbox"][name="selectedTopicIds"]')
        : [];

    function dataKeyForInput(prefix, input) {
        return prefix + input.id.charAt(0).toLowerCase() + input.id.slice(1).replace(/[A-Z]/g, function (letter) {
            return '-' + letter.toLowerCase();
        });
    }

    function message(key) {
        return form.getAttribute('data-msg-' + key) || '';
    }

    function requiredMessage(input) {
        return message(dataKeyForInput('required-', input)) || message('required-generic');
    }

    function fieldContainer(input) {
        var node = input;
        while (node && node !== form) {
            if (node.classList && node.classList.contains('community-create-field')) {
                return node;
            }
            node = node.parentNode;
        }
        return input ? input.parentNode : form;
    }

    function clientErrorId(input) {
        return input.id + 'ClientError';
    }

    function setDescribedBy(input, errorId) {
        var ids = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (ids.indexOf(errorId) === -1) {
            ids.push(errorId);
            input.setAttribute('aria-describedby', ids.join(' '));
        }
    }

    function removeDescribedBy(input, errorId) {
        var ids = (input.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
            return id && id !== errorId;
        });
        if (ids.length) {
            input.setAttribute('aria-describedby', ids.join(' '));
        } else {
            input.removeAttribute('aria-describedby');
        }
    }

    function setInlineError(input, text) {
        var container = fieldContainer(input);
        var errorId = clientErrorId(input);
        var error = container.querySelector('[data-client-error-for="' + input.id + '"]');
        if (!error) {
            error = document.createElement('span');
            error.id = errorId;
            error.className = 'form-error client-form-error';
            error.setAttribute('data-client-error-for', input.id);
            error.setAttribute('role', 'alert');
            container.appendChild(error);
        }

        error.textContent = text;
        error.hidden = false;
        input.classList.add('is-invalid');
        input.setAttribute('aria-invalid', 'true');
        setDescribedBy(input, errorId);
    }

    function clearInlineError(input) {
        var container = fieldContainer(input);
        var errorId = clientErrorId(input);
        var error = container.querySelector('[data-client-error-for="' + input.id + '"]');
        if (error) {
            error.textContent = '';
            error.hidden = true;
        }
        input.classList.remove('is-invalid');
        input.removeAttribute('aria-invalid');
        removeDescribedBy(input, errorId);
    }

    function setTopicError(text) {
        if (!topicGroup) {
            return;
        }

        var hint = topicGroup.querySelector('.review-tag-chips-hint');
        if (!hint) {
            return;
        }

        var error = topicGroup.querySelector('[data-community-topic-chips-error]');
        if (!error) {
            error = document.createElement('span');
            error.className = 'form-error client-form-error';
            error.setAttribute('data-community-topic-chips-error', 'true');
            error.setAttribute('role', 'alert');
            hint.parentNode.insertBefore(error, hint);
        }

        error.textContent = text;
        error.hidden = false;
    }

    function clearTopicError() {
        if (!topicGroup) {
            return;
        }
        var error = topicGroup.querySelector('[data-community-topic-chips-error]');
        if (error) {
            error.textContent = '';
            error.hidden = true;
        }
    }

    function checkedTopicCount() {
        return Array.prototype.reduce.call(topicCheckboxes, function (acc, checkbox) {
            return acc + (checkbox.checked ? 1 : 0);
        }, 0);
    }

    function validateRequiredText(input) {
        if (!input || input.value.trim()) {
            clearInlineError(input);
            return true;
        }

        setInlineError(input, requiredMessage(input));
        return false;
    }

    function validateTopics() {
        if (checkedTopicCount() > 0) {
            clearTopicError();
            return true;
        }

        setTopicError(message('topics-required'));
        return false;
    }

    function focusFirstInvalid() {
        var invalid = form.querySelector('[aria-invalid="true"]:not([type="hidden"])');
        if (invalid && typeof invalid.focus === 'function') {
            invalid.focus();
            return;
        }

        var topicError = topicGroup ? topicGroup.querySelector('[data-community-topic-chips-error]:not([hidden])') : null;
        if (topicError && typeof topicError.scrollIntoView === 'function') {
            topicError.scrollIntoView({ block: 'nearest' });
        }
    }

    function clearAllClientErrors() {
        Array.prototype.forEach.call(form.querySelectorAll('.client-form-error'), function (error) {
            error.textContent = '';
            error.hidden = true;
        });
        Array.prototype.forEach.call(form.querySelectorAll('[aria-invalid="true"]'), function (input) {
            input.classList.remove('is-invalid');
            input.removeAttribute('aria-invalid');
        });
    }

    var nameInput = document.getElementById('communityCreateName');
    var descriptionInput = document.getElementById('communityCreateDescription');

    [nameInput, descriptionInput].forEach(function (input) {
        if (!input) {
            return;
        }
        input.addEventListener('input', function () {
            if (input.value.trim()) {
                clearInlineError(input);
            }
        });
    });

    Array.prototype.forEach.call(topicCheckboxes, function (checkbox) {
        checkbox.addEventListener('change', function () {
            if (checkedTopicCount() > 0) {
                clearTopicError();
            }
        });
    });

    form.addEventListener('submit', function (event) {
        clearAllClientErrors();
        clearTopicError();

        var valid = true;
        valid = validateRequiredText(nameInput) && valid;
        valid = validateRequiredText(descriptionInput) && valid;
        valid = validateTopics() && valid;

        if (!valid) {
            event.preventDefault();
            focusFirstInvalid();
        }
    });
}());
