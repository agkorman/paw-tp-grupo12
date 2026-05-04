(function () {
    var groups = document.querySelectorAll('[data-review-tag-chips]');
    if (!groups.length) {
        return;
    }

    Array.prototype.forEach.call(groups, function (group) {
        var checkboxes = group.querySelectorAll('input[type="checkbox"][name="tagIds"]');

        function refresh() {
            Array.prototype.forEach.call(checkboxes, function (cb) {
                var label = cb.parentElement;
                if (label) {
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
