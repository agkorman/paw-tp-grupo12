(function () {
    'use strict';

    var select = document.getElementById('communitySortSelect');
    if (!select || select.dataset.autoSubmit !== 'true') {
        return;
    }

    select.addEventListener('change', function () {
        if (select.form) {
            select.form.submit();
        }
    });
})();
