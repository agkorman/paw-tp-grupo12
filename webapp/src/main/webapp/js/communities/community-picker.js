document.addEventListener('DOMContentLoaded', function () {
    var select = document.getElementById('communitySlugSelect');
    if (!select) return;

    var wrapper = document.createElement('div');
    wrapper.className = 'community-picker-wrapper';

    var input = document.createElement('input');
    input.type = 'text';
    input.className = 'community-picker-input';
    input.placeholder = select.options[0] ? select.options[0].textContent : '';
    input.setAttribute('autocomplete', 'off');

    var list = document.createElement('ul');
    list.className = 'community-picker-list';
    list.setAttribute('role', 'listbox');

    var options = [];
    for (var i = 1; i < select.options.length; i++) {
        var opt = select.options[i];
        options.push({ value: opt.value, label: opt.textContent.trim() });
        if (opt.selected) {
            input.value = opt.textContent.trim();
        }
    }

    function renderList(filter) {
        list.innerHTML = '';
        var lower = (filter || '').toLowerCase();
        var matches = options.filter(function (o) {
            return !lower || o.label.toLowerCase().indexOf(lower) !== -1;
        });
        matches.forEach(function (o) {
            var li = document.createElement('li');
            li.className = 'community-picker-option';
            li.setAttribute('role', 'option');
            li.textContent = o.label;
            li.dataset.value = o.value;
            if (o.value === select.value) {
                li.classList.add('community-picker-option--selected');
            }
            li.addEventListener('mousedown', function (e) {
                e.preventDefault();
                select.value = o.value;
                input.value = o.label;
                list.classList.remove('community-picker-list--open');
            });
            list.appendChild(li);
        });
    }

    input.addEventListener('focus', function () {
        renderList(input.value);
        list.classList.add('community-picker-list--open');
    });

    input.addEventListener('input', function () {
        renderList(input.value);
        list.classList.add('community-picker-list--open');
    });

    input.addEventListener('blur', function () {
        list.classList.remove('community-picker-list--open');
    });

    select.parentNode.insertBefore(wrapper, select);
    wrapper.appendChild(input);
    wrapper.appendChild(list);
    select.style.display = 'none';
    select.removeAttribute('required');
    input.setAttribute('required', 'required');
});
