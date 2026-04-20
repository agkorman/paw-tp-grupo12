(function () {
    function hasAttribute(node, attrName) {
        return node && node.nodeType === 1 && node.getAttribute(attrName) !== null;
    }

    function findActionButton(target, attrName) {
        var node = target;

        while (node && node !== document) {
            if (hasAttribute(node, attrName)) {
                return node;
            }
            node = node.parentNode;
        }

        return null;
    }

    function hasClass(node, className) {
        return (' ' + node.className + ' ').indexOf(' ' + className + ' ') >= 0;
    }

    function setClass(node, className, enabled) {
        if (enabled && !hasClass(node, className)) {
            node.className += ' ' + className;
        }
        if (!enabled && hasClass(node, className)) {
            node.className = (' ' + node.className + ' ')
                .replace(' ' + className + ' ', ' ')
                .replace(/^\s+|\s+$/g, '');
        }
    }

    function setPressedState(button, attrName, active) {
        button.setAttribute(attrName, String(active));
        button.setAttribute('aria-pressed', String(active));
        setClass(button, 'is-active', active);
    }

    function updateFavorite(button) {
        var isFavorited = button.getAttribute('data-favorited') === 'true';
        var nextState = !isFavorited;
        var label = button.querySelector('span');

        setPressedState(button, 'data-favorited', nextState);
        button.setAttribute('aria-label', nextState ? 'Quitar de favoritos' : 'Agregar a favoritos');
        if (label) {
            label.textContent = nextState ? 'Favorito' : 'Agregar';
        }
    }

    function updateReviewLike(button) {
        var isLiked = button.getAttribute('data-liked') === 'true';
        var nextState = !isLiked;
        var countNode = button.querySelector('[data-review-like-count]');
        var currentCount = countNode ? parseInt(countNode.textContent, 10) : 0;

        if (isNaN(currentCount)) {
            currentCount = 0;
        }

        setPressedState(button, 'data-liked', nextState);
        button.setAttribute('aria-label', nextState ? 'Quitar like de la review' : 'Likear review');

        if (countNode) {
            countNode.textContent = String(Math.max(0, currentCount + (nextState ? 1 : -1)));
        }
    }

    document.addEventListener('click', function (event) {
        var favoriteButton = findActionButton(event.target, 'data-favorite-toggle');
        if (favoriteButton) {
            event.preventDefault();
            event.stopPropagation();
            if (!favoriteButton.disabled) {
                updateFavorite(favoriteButton);
            }
            return;
        }

        var likeButton = findActionButton(event.target, 'data-review-like-toggle');
        if (likeButton) {
            if (likeButton.form && likeButton.form.getAttribute('action')) {
                return;
            }
            event.preventDefault();
            event.stopPropagation();
            if (!likeButton.disabled) {
                updateReviewLike(likeButton);
            }
        }
    });
}());
