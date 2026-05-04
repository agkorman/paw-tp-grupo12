(function () {
    var groups = document.querySelectorAll('[data-review-tag-chips]');
    if (!groups.length) {
        return;
    }

    Array.prototype.forEach.call(groups, function (group) {
        var checkboxes = group.querySelectorAll('input[type="checkbox"][name="tagIds"]');
        var positiveWrap = group.querySelector('.review-tag-chips-group--positive');
        var negativeWrap = group.querySelector('.review-tag-chips-group--negative');
        var maxSelected = parseInt(group.getAttribute('data-max-selected'), 10);
        if (!Number.isFinite(maxSelected) || maxSelected < 1) {
            maxSelected = 6;
        }

        function message(key) {
            return group.getAttribute('data-msg-' + key) || '';
        }

        function checkedCount() {
            return Array.prototype.reduce.call(checkboxes, function (acc, cb) {
                return acc + (cb.checked ? 1 : 0);
            }, 0);
        }

        function hasCheckedIn(container) {
            if (!container) return false;
            var cbs = container.querySelectorAll('input[type="checkbox"][name="tagIds"]');
            return Array.prototype.some.call(cbs, function (cb) { return cb.checked; });
        }

        function setGroupError(text) {
            var hint = group.querySelector('.review-tag-chips-hint');
            if (!hint) return;
            var node = group.querySelector('[data-review-tag-chips-error]');
            if (!node) {
                node = document.createElement('span');
                node.className = 'form-error client-form-error';
                node.setAttribute('data-review-tag-chips-error', 'true');
                node.setAttribute('role', 'alert');
                hint.parentNode.insertBefore(node, hint);
            }
            node.textContent = text || '';
            node.hidden = !text;
        }

        function clearGroupError() {
            setGroupError('');
        }

        function setDisabled(container, disabled) {
            if (!container) return;
            var cbs = container.querySelectorAll('input[type="checkbox"][name="tagIds"]');
            Array.prototype.forEach.call(cbs, function (cb) {
                if (!cb.checked) {
                    cb.disabled = !!disabled;
                }
                var label = cb.parentElement;
                if (label) {
                    label.classList.toggle('is-disabled', cb.disabled);
                }
            });
        }

        function refresh() {
            clearGroupError();

            Array.prototype.forEach.call(checkboxes, function (cb) {
                var label = cb.parentElement;
                if (label) {
                    label.classList.toggle('is-selected', cb.checked);
                }
            });

            var positiveSelected = hasCheckedIn(positiveWrap);
            var negativeSelected = hasCheckedIn(negativeWrap);
            if (positiveSelected && !negativeSelected) {
                setDisabled(negativeWrap, true);
                setDisabled(positiveWrap, false);
            } else if (negativeSelected && !positiveSelected) {
                setDisabled(positiveWrap, true);
                setDisabled(negativeWrap, false);
            } else {
                setDisabled(positiveWrap, false);
                setDisabled(negativeWrap, false);
            }
        }

        Array.prototype.forEach.call(checkboxes, function (cb) {
            cb.addEventListener('change', function () {
                clearGroupError();

                if (cb.checked) {
                    var total = checkedCount();
                    if (total > maxSelected) {
                        cb.checked = false;
                        setGroupError(message('max-selected').replace('{0}', String(maxSelected)));
                        refresh();
                        return;
                    }

                    var nowPositive = positiveWrap && positiveWrap.contains(cb);
                    var nowNegative = negativeWrap && negativeWrap.contains(cb);
                    if ((nowPositive && hasCheckedIn(negativeWrap)) || (nowNegative && hasCheckedIn(positiveWrap))) {
                        cb.checked = false;
                        setGroupError(message('opposites'));
                        refresh();
                        return;
                    }
                }

                refresh();
            });
        });
        refresh();
    });
}());
