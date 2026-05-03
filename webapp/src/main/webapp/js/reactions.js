(function () {
    var nativeSubmit = HTMLFormElement.prototype.submit;

    function supportsFetchFormData() {
        return typeof window.fetch === 'function'
            && typeof window.FormData === 'function'
            && typeof window.Promise === 'function';
    }

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

    function getReviewLikeState(button) {
        var countNode = button.querySelector('[data-review-like-count]');
        var count = countNode ? parseInt(countNode.textContent, 10) : 0;

        if (isNaN(count)) {
            count = 0;
        }

        return {
            liked: button.getAttribute('data-liked') === 'true',
            count: count
        };
    }

    function applyReviewLikeState(button, state) {
        var countNode = button.querySelector('[data-review-like-count]');

        setPressedState(button, 'data-liked', state.liked);
        button.setAttribute('aria-label', state.liked ? 'Quitar like' : 'Dar like');

        if (countNode) {
            countNode.textContent = String(Math.max(0, state.count));
        }
    }

    function getToggledReviewLikeState(button) {
        var current = getReviewLikeState(button);

        return {
            liked: !current.liked,
            count: Math.max(0, current.count + (current.liked ? -1 : 1))
        };
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

        if (!supportsFetchFormData()) {
            nativeSubmit.call(form);
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
        applyReviewLikeState(button, getToggledReviewLikeState(button));
    }

    function normalizeAction(action) {
        try {
            var url = new URL(action, window.location.href);
            return url.pathname + url.search;
        } catch (ignored) {
            return action;
        }
    }

    function findReviewLikeButton(form) {
        return form.querySelector('[data-review-like-toggle]');
    }

    function getMatchingLikeForms(root, form, button) {
        var actionKey = normalizeAction(form.getAttribute('action') || form.action);
        var reviewId = button.getAttribute('data-review-id');
        var forms = root.querySelectorAll('.review-like-form');
        var matches = [];

        Array.prototype.forEach.call(forms, function (candidate) {
            var candidateButton = findReviewLikeButton(candidate);

            if (!candidateButton) {
                return;
            }

            if (normalizeAction(candidate.getAttribute('action') || candidate.action) !== actionKey) {
                return;
            }

            if (candidateButton.getAttribute('data-review-id') !== reviewId) {
                return;
            }

            matches.push({
                form: candidate,
                button: candidateButton
            });
        });

        return matches;
    }

    function setLikeFormsPending(items, pending) {
        Array.prototype.forEach.call(items, function (item) {
            item.form.dataset.loading = pending ? 'true' : 'false';
            item.button.setAttribute('aria-busy', String(pending));
        });
    }

    function isLoginRedirect(response) {
        if (!response || !response.redirected) {
            return false;
        }

        try {
            return new URL(response.url).pathname.indexOf('/login') >= 0;
        } catch (ignored) {
            return false;
        }
    }

    function parseLikeResponse(body) {
        if (!body) {
            return null;
        }
        var parts = body.trim().split('|');
        if (parts.length !== 2) {
            return null;
        }
        var count = parseInt(parts[1], 10);
        if (isNaN(count)) {
            return null;
        }
        return { liked: parts[0] === 'true', count: Math.max(0, count) };
    }

    function syncLikeStateFromResponse(body, sourceForm, sourceButton) {
        var nextState = parseLikeResponse(body);
        if (!nextState) {
            return;
        }
        var currentMatches = getMatchingLikeForms(document, sourceForm, sourceButton);
        Array.prototype.forEach.call(currentMatches, function (item) {
            applyReviewLikeState(item.button, nextState);
        });
    }

    function submitEnhancedLikeForm(form) {
        var button = findReviewLikeButton(form);

        if (!button || button.disabled || form.dataset.loading === 'true') {
            return;
        }

        var matches = getMatchingLikeForms(document, form, button);
        var previousStates = matches.map(function (item) {
            return {
                button: item.button,
                state: getReviewLikeState(item.button)
            };
        });
        var optimisticState = getToggledReviewLikeState(button);

        Array.prototype.forEach.call(matches, function (item) {
            applyReviewLikeState(item.button, optimisticState);
        });
        setLikeFormsPending(matches, true);

        fetch(form.action, {
            method: 'POST',
            body: new FormData(form),
            headers: {
                'Accept': 'text/plain',
                'X-Requested-With': 'XMLHttpRequest'
            },
            credentials: 'same-origin'
        }).then(function (response) {
            if (isLoginRedirect(response)) {
                window.location.href = response.url;
                return '';
            }
            if (response.status === 401) {
                window.location.href = '/login';
                return '';
            }
            if (!response.ok) {
                throw new Error('Review like request failed');
            }
            return response.text();
        }).then(function (body) {
            if (body) {
                syncLikeStateFromResponse(body, form, button);
            }
        }).catch(function () {
            Array.prototype.forEach.call(previousStates, function (item) {
                if (document.contains(item.button)) {
                    applyReviewLikeState(item.button, item.state);
                }
            });
        }).finally(function () {
            var currentMatches = getMatchingLikeForms(document, form, button);
            setLikeFormsPending(currentMatches, false);
        });
    }

    document.addEventListener('submit', function (event) {
        var form = event.target;

        if (!(form instanceof HTMLFormElement)) {
            return;
        }

        if (form.dataset.enhancedReviewLike === 'true') {
            if (!supportsFetchFormData()) {
                return;
            }
            event.preventDefault();
            submitEnhancedLikeForm(form);
            return;
        }

    });

    document.addEventListener('click', function (event) {
        var favoriteButton = findActionButton(event.target, 'data-favorite-toggle');
        if (favoriteButton) {
            if (!supportsFetchFormData()) {
                return;
            }
            event.preventDefault();
            event.stopPropagation();
            if (!favoriteButton.disabled) {
                submitFavorite(favoriteButton.form || favoriteButton.closest('form'), favoriteButton);
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

    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!hasAttribute(form, 'data-favorite-form')) {
            return;
        }
        var button = form.querySelector('[data-favorite-toggle]');
        if (!supportsFetchFormData()) {
            return;
        }
        event.preventDefault();
        if (button && !button.disabled) {
            submitFavorite(form, button);
        }
    });
}());
