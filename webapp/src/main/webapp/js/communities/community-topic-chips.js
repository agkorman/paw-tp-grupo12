(function () {
    var groups = document.querySelectorAll('[data-community-topic-chips]');
    if (!groups.length) {
        return;
    }

    Array.prototype.forEach.call(groups, function (group) {
        var checkboxes = group.querySelectorAll('input[type="checkbox"][name="selectedTopicIds"]');
        var maxSelected = parseInt(group.getAttribute('data-max-selected'), 10);
        if (!Number.isFinite(maxSelected) || maxSelected < 1) {
            maxSelected = 4;
        }

        function message(key) {
            return group.getAttribute('data-msg-' + key) || '';
        }

        function checkedCount() {
            return Array.prototype.reduce.call(checkboxes, function (acc, checkbox) {
                return acc + (checkbox.checked ? 1 : 0);
            }, 0);
        }

        function setGroupError(text) {
            var hint = group.querySelector('.review-tag-chips-hint');
            if (!hint) {
                return;
            }

            var node = group.querySelector('[data-community-topic-chips-error]');
            if (!node) {
                node = document.createElement('span');
                node.className = 'form-error client-form-error';
                node.setAttribute('data-community-topic-chips-error', 'true');
                node.setAttribute('role', 'alert');
                hint.parentNode.insertBefore(node, hint);
            }
            node.textContent = text || '';
            node.hidden = !text;
        }

        function clearGroupError() {
            setGroupError('');
        }

        function refresh() {
            var atMax = checkedCount() >= maxSelected;
            Array.prototype.forEach.call(checkboxes, function (checkbox) {
                var label = checkbox.parentElement;
                if (label) {
                    label.classList.toggle('is-selected', checkbox.checked);
                    label.classList.toggle('is-disabled', !checkbox.checked && atMax);
                }
                if (!checkbox.checked) {
                    checkbox.disabled = atMax;
                } else {
                    checkbox.disabled = false;
                }
            });
        }

        Array.prototype.forEach.call(checkboxes, function (checkbox) {
            checkbox.addEventListener('change', function () {
                clearGroupError();
                if (checkbox.checked && checkedCount() > maxSelected) {
                    checkbox.checked = false;
                    setGroupError(message('max-selected').replace('{0}', String(maxSelected)));
                }
                refresh();
            });
        });

        refresh();
    });
}());
