(function () {
    var groups = document.querySelectorAll('[data-review-tag-chips]');
    if (!groups.length) {
        return;
    }

    Array.prototype.forEach.call(groups, function (group) {
        var max = parseInt(group.getAttribute('data-review-tag-max'), 10) || 6;
        var checkboxes = group.querySelectorAll('input[type="checkbox"][name="tagIds"]');

        function refresh() {
            var selected = [];
            var dimensions = {};
            Array.prototype.forEach.call(checkboxes, function (cb) {
                if (cb.checked) {
                    selected.push(cb);
                    dimensions[cb.dataset.dimension] = true;
                }
            });

            var atCap = selected.length >= max;
            Array.prototype.forEach.call(checkboxes, function (cb) {
                var label = cb.parentElement;
                var conflicting = !cb.checked && cb.dataset.dimension && dimensions[cb.dataset.dimension];
                var capped = !cb.checked && atCap;
                var disabled = conflicting || capped;
                cb.disabled = disabled;
                if (label) {
                    label.classList.toggle('is-disabled', disabled);
                    label.classList.toggle('is-selected', cb.checked);
                }
            });
        }

        Array.prototype.forEach.call(checkboxes, function (cb) {
            cb.addEventListener('change', refresh);
        });
        refresh();
    });
}());
