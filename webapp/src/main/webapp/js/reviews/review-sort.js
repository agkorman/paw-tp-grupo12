(function () {
    'use strict';

    var select = document.getElementById('reviewSortSelect');
    if (!select || select.dataset.autoSubmit !== 'true') {
        return;
    }

    select.addEventListener('change', function () {
        if (select.form) {
            select.form.submit();
        }
    });
})();
