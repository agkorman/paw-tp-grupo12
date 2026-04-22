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

    function updateFavoriteButton(button, favorited) {
        var label = button.querySelector('span');

        setPressedState(button, 'data-favorited', favorited);
        button.setAttribute('aria-label', favorited ? 'Quitar de favoritos' : 'Agregar a favoritos');
        if (label) {
            label.textContent = favorited ? 'Favorito' : 'Agregar';
        }
    }

    function updateFavoriteForms(carId, favorited) {
        var selector = '[data-favorite-toggle][data-car-id="' + carId + '"]';
        Array.prototype.forEach.call(document.querySelectorAll(selector), function (button) {
            var form = button.form || button.closest('form');
            var nextValue = form ? form.querySelector('[data-favorite-next-value]') : null;
            updateFavoriteButton(button, favorited);
            if (nextValue) {
                nextValue.value = favorited ? 'false' : 'true';
            }
        });
    }

    function submitFavorite(form, button) {
        if (!form || !button || button.disabled) {
            return;
        }

        button.disabled = true;
        window.fetch(form.getAttribute('action'), {
            method: 'POST',
            body: new window.FormData(form),
            credentials: 'same-origin',
            headers: {
                'Accept': 'text/plain',
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).then(function (response) {
            if (response.redirected) {
                window.location.href = response.url;
                return null;
            }
            if (response.status === 401) {
                window.location.href = '/login';
                return null;
            }
            if (!response.ok) {
                throw new Error('favorite-request-failed');
            }
            return response.text();
        }).then(function (body) {
            if (body === null || body === undefined || body === '') {
                return;
            }
            updateFavoriteForms(button.getAttribute('data-car-id'), body.trim() === 'true');
        }).catch(function () {
            form.submit();
        }).finally(function () {
            button.disabled = false;
        });
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
                submitFavorite(favoriteButton.form || favoriteButton.closest('form'), favoriteButton);
            }
            return;
        }

        var likeButton = findActionButton(event.target, 'data-review-like-toggle');
        if (likeButton) {
            event.preventDefault();
            event.stopPropagation();
            if (!likeButton.disabled) {
                updateReviewLike(likeButton);
            }
        }
    });

    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!hasAttribute(form, 'data-favorite-form')) {
            return;
        }
        var button = form.querySelector('[data-favorite-toggle]');
        event.preventDefault();
        if (button && !button.disabled) {
            submitFavorite(form, button);
        }
    });
}());
