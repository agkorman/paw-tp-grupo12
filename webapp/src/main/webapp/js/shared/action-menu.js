(function () {
    var closestByAttribute = function (element, attribute) {
        while (element && element !== document) {
            if (element.hasAttribute && element.hasAttribute(attribute)) {
                return element;
            }
            element = element.parentNode;
        }
        return null;
    };

    var closeActionMenus = function (exceptMenu) {
        var menus = document.querySelectorAll('[data-action-menu]');
        for (var i = 0; i < menus.length; i += 1) {
            if (menus[i] === exceptMenu) {
                continue;
            }
            var panel = menus[i].querySelector('[data-action-menu-panel]');
            var toggle = menus[i].querySelector('[data-action-menu-toggle]');
            if (panel) {
                panel.hidden = true;
            }
            if (toggle) {
                toggle.setAttribute('aria-expanded', 'false');
            }
        }
    };

    var toggleActionMenu = function (button) {
        var menu = closestByAttribute(button, 'data-action-menu');
        var panel = menu ? menu.querySelector('[data-action-menu-panel]') : null;
        if (!menu || !panel) {
            return;
        }

        var willOpen = panel.hidden;
        closeActionMenus(menu);
        panel.hidden = !willOpen;
        button.setAttribute('aria-expanded', String(willOpen));
    };

    document.addEventListener('click', function (event) {
        var toggle = closestByAttribute(event.target, 'data-action-menu-toggle');
        if (toggle) {
            event.preventDefault();
            toggleActionMenu(toggle);
            return;
        }

        if (!closestByAttribute(event.target, 'data-action-menu')) {
            closeActionMenus();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            closeActionMenus();
        }
    });

    window.PawActionMenus = {
        close: closeActionMenus
    };
})();
