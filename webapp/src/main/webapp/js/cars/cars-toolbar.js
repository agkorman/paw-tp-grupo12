(function () {
    var fields = document.querySelectorAll('.cars-toolbar-field');
    Array.prototype.forEach.call(fields, function (field) {
        var select = field.querySelector('select');
        var valueSpan = field.querySelector('[data-toolbar-select-value]');
        if (!select || !valueSpan) { return; }
        select.addEventListener('change', function () {
            var opt = select.options[select.selectedIndex];
            valueSpan.textContent = opt ? opt.textContent.trim() : '';
        });
    });
})();
