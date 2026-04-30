(function () {
    var modal = document.getElementById('reviewDetailModal');

    if (!modal) {
        return;
    }

    var body = modal.querySelector('[data-review-detail-body]');
    var endpointBase = modal.getAttribute('data-detail-endpoint') || '/reviews/';
    var loadingHtml = body ? body.innerHTML : '';
    var lastTrigger = null;
    var currentRequestId = 0;

    function findOpenTrigger(target) {
        var node = target;
        while (node && node !== document) {
            if (node.nodeType === 1 && node.getAttribute && node.getAttribute('data-open-review-detail') !== null) {
                return node;
            }
            node = node.parentNode;
        }
        return null;
    }

    function isInteractiveDescendant(start, container) {
        var node = start;
        while (node && node !== container && node !== document) {
            if (node.nodeType === 1) {
                var tag = node.tagName;
                if (tag === 'BUTTON' || tag === 'A' || tag === 'INPUT'
                        || tag === 'TEXTAREA' || tag === 'SELECT' || tag === 'LABEL'
                        || tag === 'FORM') {
                    return true;
                }
                if (node.getAttribute && node.getAttribute('data-modal-ignore') !== null) {
                    return true;
                }
            }
            node = node.parentNode;
        }
        return false;
    }

    function buildDetailUrl(reviewId) {
        var base = endpointBase;
        if (base.charAt(base.length - 1) !== '/') {
            base = base + '/';
        }
        return base + encodeURIComponent(reviewId) + '/detail';
    }

    function showLoading() {
        if (body) {
            body.innerHTML = loadingHtml;
        }
    }

    function showError(message) {
        if (body) {
            body.innerHTML = '<div class="review-detail-modal-loading" role="alert">'
                    + (message || 'No pudimos cargar la reseña.')
                    + '</div>';
        }
    }

    function injectFragment(html) {
        if (!body) {
            return;
        }
        var parsed = new DOMParser().parseFromString(html, 'text/html');
        var content = parsed.querySelector('[data-review-detail-content]');
        if (!content) {
            showError();
            return;
        }
        body.innerHTML = '';
        body.appendChild(document.adoptNode(content));
    }

    function openModal(trigger) {
        lastTrigger = trigger;
        modal.removeAttribute('hidden');
        modal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('modal-open');
        if (body) {
            body.scrollTop = 0;
        }
    }

    function closeModal() {
        modal.setAttribute('hidden', 'hidden');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('modal-open');
        currentRequestId += 1;
        showLoading();
        if (lastTrigger && document.contains(lastTrigger) && typeof lastTrigger.focus === 'function') {
            lastTrigger.focus();
        }
        lastTrigger = null;
    }

    function loadDetail(reviewId) {
        currentRequestId += 1;
        var requestId = currentRequestId;
        showLoading();

        if (typeof window.fetch !== 'function') {
            showError();
            return;
        }

        window.fetch(buildDetailUrl(reviewId), {
            method: 'GET',
            credentials: 'same-origin',
            headers: {
                'Accept': 'text/html',
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('detail-request-failed');
            }
            return response.text();
        }).then(function (html) {
            if (requestId !== currentRequestId) {
                return;
            }
            injectFragment(html);
        }).catch(function () {
            if (requestId !== currentRequestId) {
                return;
            }
            showError();
        });
    }

    document.addEventListener('click', function (event) {
        var trigger = findOpenTrigger(event.target);

        if (!trigger) {
            return;
        }

        if (isInteractiveDescendant(event.target, trigger)) {
            return;
        }

        var reviewId = trigger.getAttribute('data-review-id');
        if (!reviewId) {
            return;
        }

        event.preventDefault();
        openModal(trigger);
        loadDetail(reviewId);
    });

    Array.prototype.forEach.call(modal.querySelectorAll('[data-close-review-detail-modal]'), function (element) {
        element.addEventListener('click', closeModal);
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            closeModal();
        }
    });
}());
