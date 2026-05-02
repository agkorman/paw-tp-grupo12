(function () {
    var nativeSubmit = HTMLFormElement.prototype.submit;

    var syncCatalogCount = function (root) {
        if (!root) {
            return;
        }

        var countTarget = document.querySelector('.cars-toolbar-count');
        if (!countTarget) {
            return;
        }

        var resultCount = root.getAttribute('data-result-count');
        if (resultCount == null) {
            return;
        }

        countTarget.textContent = resultCount + ' vehículos encontrados';
    };

    var syncToolbarSelectValue = function (select) {
        if (!(select instanceof HTMLSelectElement) || !select.name) {
            return;
        }

        var valueTarget = document.querySelector('[data-toolbar-select-value="' + select.name + '"]');
        if (!valueTarget) {
            return;
        }

        var selectedOption = select.options[select.selectedIndex];
        valueTarget.textContent = selectedOption ? selectedOption.textContent.trim() : '';
    };

    var syncToolbarSelectValues = function (scope) {
        var root = scope || document;
        var selects = root.querySelectorAll('.cars-toolbar-select-overlay');

        Array.prototype.forEach.call(selects, syncToolbarSelectValue);
    };

    var buildSearchParams = function (form) {
        var formData = new FormData(form);
        var params = new URLSearchParams();

        formData.forEach(function (value, key) {
            if (typeof value === 'string') {
                var trimmed = value.trim();
                if (trimmed.length > 0) {
                    params.append(key, trimmed);
                }
            } else if (value != null) {
                params.append(key, value);
            }
        });

        return params;
    };

    var setControlsDisabled = function (form, disabled) {
        var controls = form.querySelectorAll('button, select, textarea, input:not([type="search"])');
        Array.prototype.forEach.call(controls, function (control) {
            control.disabled = disabled;
        });
    };

    var scrollToTarget = function (targetSelector, root) {
        if (targetSelector === '#carsCatalogContent') {
            window.scrollTo({ top: 0, behavior: 'smooth' });
            return;
        }

        if (!root) {
            return;
        }

        var scrollTarget = root;
        var stickyOffset = 88;
        var top = scrollTarget.getBoundingClientRect().top + window.pageYOffset - stickyOffset;

        window.scrollTo({
            top: Math.max(top, 0),
            behavior: 'smooth'
        });
    };

    var submitEnhancedForm = function (form) {
        if (!form) {
            return;
        }
        if (form.dataset.loading === 'true') {
            form.dataset.pendingSubmit = 'true';
            return;
        }

        var fragmentUrl = form.dataset.fragmentUrl;
        var targetSelector = form.dataset.target;
        var target = document.querySelector(targetSelector);

        if (!fragmentUrl || !targetSelector || !target) {
            nativeSubmit.call(form);
            return;
        }

        var skipScroll = form.dataset.skipNextScroll === 'true';
        var suppressFallbackSubmit = form.dataset.suppressFallbackSubmit === 'true';
        var quietLoading = form.dataset.quietLoading === 'true';
        delete form.dataset.skipNextScroll;
        delete form.dataset.suppressFallbackSubmit;
        delete form.dataset.quietLoading;

        var params = buildSearchParams(form);
        var actionUrl = new URL(form.action, window.location.href);
        var fetchUrl = new URL(fragmentUrl, window.location.href);
        var query = params.toString();
        if (query.length > 0) {
            actionUrl.search = query;
            fetchUrl.search = query;
        } else {
            actionUrl.search = '';
            fetchUrl.search = '';
        }

        form.dataset.loading = 'true';
        setControlsDisabled(form, true);
        if (!quietLoading) {
            target.classList.add('is-loading');
        }
        target.setAttribute('aria-busy', 'true');

        fetch(fetchUrl.toString(), {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('Fragment request failed');
            }
            return response.text();
        }).then(function (html) {
            var parsed = new DOMParser().parseFromString(html, 'text/html');
            var replacement = parsed.querySelector(targetSelector);
            var currentTarget = document.querySelector(targetSelector);

            if (!replacement || !currentTarget) {
                throw new Error('Fragment target not found');
            }

            currentTarget.replaceWith(replacement);
            observeShowMoreSentinels(replacement);
            syncCatalogCount(replacement);
            window.history.replaceState({}, '', actionUrl.pathname + actionUrl.search);
            if (!skipScroll) {
                scrollToTarget(targetSelector, document.querySelector(targetSelector));
            }
        }).catch(function () {
            if (!suppressFallbackSubmit) {
                nativeSubmit.call(form);
            }
        }).finally(function () {
            delete form.dataset.loading;
            if (document.contains(form)) {
                setControlsDisabled(form, false);
                if (form.dataset.pendingSubmit === 'true') {
                    delete form.dataset.pendingSubmit;
                    submitEnhancedForm(form);
                    return;
                }
            }
            var currentTarget = document.querySelector(targetSelector);
            if (currentTarget) {
                currentTarget.classList.remove('is-loading');
                currentTarget.removeAttribute('aria-busy');
            }
        });
    };

    document.addEventListener('input', function (event) {
        var target = event.target;
        if (!(target instanceof HTMLInputElement) || target.type !== 'search') {
            return;
        }

        var form = target.form;
        if (!form || form.dataset.enhancedFilter !== 'true' || form.dataset.autoSubmit !== 'true') {
            return;
        }

        clearTimeout(form._searchDebounce);
        form._searchDebounce = setTimeout(function () {
            submitEnhancedForm(form);
        }, 300);
    });

    document.addEventListener('change', function (event) {
        var target = event.target;
        if (!(target instanceof HTMLSelectElement)) {
            return;
        }

        var form = target.form;
        if (!form || form.dataset.enhancedFilter !== 'true' || form.dataset.autoSubmit !== 'true') {
            return;
        }

        syncToolbarSelectValue(target);
        submitEnhancedForm(form);
    });

    document.addEventListener('submit', function (event) {
        var form = event.target;
        if (!(form instanceof HTMLFormElement) || form.dataset.enhancedFilter !== 'true') {
            return;
        }

        event.preventDefault();
        submitEnhancedForm(form);
    });

    var navigateFragment = function (link) {
        var fragmentUrl = link.dataset.fragmentUrl;
        var targetSelector = link.dataset.target;
        var target = targetSelector ? document.querySelector(targetSelector) : null;

        if (!fragmentUrl || !targetSelector || !target) {
            return false;
        }

        var actionUrl = new URL(link.href, window.location.href);
        var fetchUrl = new URL(fragmentUrl, window.location.href);
        fetchUrl.search = actionUrl.search;

        target.classList.add('is-loading');
        target.setAttribute('aria-busy', 'true');

        fetch(fetchUrl.toString(), {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('Fragment request failed');
            }
            return response.text();
        }).then(function (html) {
            var parsed = new DOMParser().parseFromString(html, 'text/html');
            var replacement = parsed.querySelector(targetSelector);
            var currentTarget = document.querySelector(targetSelector);

            if (!replacement || !currentTarget) {
                throw new Error('Fragment target not found');
            }

            currentTarget.replaceWith(replacement);
            observeShowMoreSentinels(replacement);
            syncCatalogCount(replacement);
            window.history.replaceState({}, '', actionUrl.pathname + actionUrl.search);
            scrollToTarget(targetSelector, document.querySelector(targetSelector));
        }).catch(function () {
            window.location.href = link.href;
        }).finally(function () {
            var currentTarget = document.querySelector(targetSelector);
            if (currentTarget) {
                currentTarget.classList.remove('is-loading');
                currentTarget.removeAttribute('aria-busy');
            }
        });

        return true;
    };

    var showMoreReviews = function (link) {
        var fragmentUrl = link.dataset.fragmentUrl;
        var targetSelector = link.dataset.target;
        var feed = targetSelector ? document.querySelector(targetSelector) : null;
        var listSelector = link.dataset.listSelector || '.review-list';
        var itemSelector = link.dataset.itemSelector || '.review-list > .review-item';
        var previewListSelector = link.dataset.previewListSelector;
        var previewItemSelector = link.dataset.previewItemSelector;
        var reviewList = feed ? feed.querySelector(listSelector) : null;
        var previewList = previewListSelector && feed ? feed.querySelector(previewListSelector) : null;

        if (!fragmentUrl || !targetSelector || !feed || !reviewList) {
            return false;
        }

        var actionUrl = new URL(link.href, window.location.href);
        var fetchUrl = new URL(fragmentUrl, window.location.href);
        fetchUrl.search = actionUrl.search;

        var controls = feed.querySelector('.reviews-feed-more');
        if (controls) {
            controls.classList.add('is-loading');
            controls.setAttribute('aria-busy', 'true');
        }

        fetch(fetchUrl.toString(), {
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        }).then(function (response) {
            if (!response.ok) {
                throw new Error('Review fragment request failed');
            }
            return response.text();
        }).then(function (html) {
            var parsed = new DOMParser().parseFromString(html, 'text/html');
            var replacement = parsed.querySelector(targetSelector);
            var replacementItems = replacement ? replacement.querySelectorAll(itemSelector) : [];
            var replacementPreviewItems = replacement && previewItemSelector ? replacement.querySelectorAll(previewItemSelector) : [];
            var replacementControls = replacement ? replacement.querySelector('.reviews-feed-more') : null;
            var currentControls = feed.querySelector('.reviews-feed-more');

            if (!replacement || replacementItems.length === 0) {
                throw new Error('Review fragment content not found');
            }

            var controlsInFeed = currentControls && reviewList && reviewList.contains(currentControls);
            Array.prototype.forEach.call(replacementItems, function (item) {
                if (controlsInFeed) {
                    reviewList.insertBefore(item, currentControls);
                } else {
                    reviewList.appendChild(item);
                }
            });

            if (previewList && replacementPreviewItems.length > 0) {
                Array.prototype.forEach.call(replacementPreviewItems, function (item) {
                    previewList.appendChild(item);
                });
            }

            if (currentControls && replacementControls) {
                currentControls.replaceWith(replacementControls);
            } else if (currentControls) {
                currentControls.remove();
            } else if (replacementControls) {
                feed.appendChild(replacementControls);
            }

            observeShowMoreSentinels(feed);

        }).catch(function () {
            window.location.href = link.href;
        }).finally(function () {
            var currentControls = feed.querySelector('.reviews-feed-more');
            if (currentControls) {
                currentControls.classList.remove('is-loading');
                currentControls.removeAttribute('aria-busy');
            }
        });

        return true;
    };

    document.addEventListener('click', function (event) {
        if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) {
            return;
        }

        var showMoreLink = event.target.closest && event.target.closest('a[data-review-show-more="true"]');
        if (showMoreLink) {
            if (showMoreReviews(showMoreLink)) {
                event.preventDefault();
            }
            return;
        }

        var link = event.target.closest && event.target.closest('a[data-pagination-link="true"]');
        if (!link) {
            return;
        }

        if (link.classList.contains('is-disabled') || link.classList.contains('is-current')) {
            event.preventDefault();
            return;
        }

        if (navigateFragment(link)) {
            event.preventDefault();
        }
    });

    syncToolbarSelectValues(document);

    var observeShowMoreSentinels = function (scope) {
        if (!('IntersectionObserver' in window)) {
            return;
        }

        var root = scope || document;
        var sentinels = root.querySelectorAll ? root.querySelectorAll('.reviews-feed-more') : [];
        Array.prototype.forEach.call(sentinels, function (sentinel) {
            if (sentinel.dataset.infiniteScrollObserved === 'true') {
                return;
            }
            sentinel.dataset.infiniteScrollObserved = 'true';

            var observer = new IntersectionObserver(function (entries) {
                entries.forEach(function (entry) {
                    if (!entry.isIntersecting) {
                        return;
                    }
                    var link = sentinel.querySelector('a[data-review-show-more="true"]');
                    if (!link) {
                        return;
                    }
                    observer.disconnect();
                    showMoreReviews(link);
                });
            }, { rootMargin: '200px' });

            observer.observe(sentinel);
        });
    };

    observeShowMoreSentinels();

})();
