(function () {
    function closest(target, attrName) {
        var node = target;
        while (node && node !== document) {
            if (node.nodeType === 1 && node.hasAttribute(attrName)) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function isInteractive(target, card) {
        var node = target;
        while (node && node !== card) {
            if (node.nodeType === 1 && ['A', 'BUTTON', 'INPUT', 'SELECT', 'TEXTAREA', 'FORM', 'LABEL'].indexOf(node.tagName) >= 0) {
                return true;
            }
            node = node.parentNode;
        }
        return false;
    }

    document.addEventListener('click', function (event) {
        var card = closest(event.target, 'data-profile-card-link');
        if (card && !isInteractive(event.target, card)) {
            window.location.href = card.getAttribute('data-profile-card-link');
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key !== 'Enter' && event.key !== ' ') {
            return;
        }
        var card = closest(event.target, 'data-profile-card-link');
        if (card && !isInteractive(event.target, card)) {
            event.preventDefault();
            window.location.href = card.getAttribute('data-profile-card-link');
        }
    });
}());
