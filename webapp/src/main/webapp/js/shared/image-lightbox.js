(function () {
    'use strict';

    var lightbox = document.getElementById('sharedImageLightbox');
    if (!lightbox) {
        return;
    }

    var image = lightbox.querySelector('[data-image-lightbox-img]');
    var counter = lightbox.querySelector('[data-image-lightbox-count]');
    var prevButton = lightbox.querySelector('[data-image-lightbox-prev]');
    var nextButton = lightbox.querySelector('[data-image-lightbox-next]');
    var urls = [];
    var index = 0;
    var lastFocused = null;

    function render() {
        if (!urls.length) {
            return;
        }
        if (index < 0) {
            index = urls.length - 1;
        }
        if (index >= urls.length) {
            index = 0;
        }
        image.src = urls[index];
        if (counter) {
            counter.textContent = (index + 1) + ' / ' + urls.length;
            counter.hidden = urls.length <= 1;
        }
        if (prevButton) {
            prevButton.hidden = urls.length <= 1;
        }
        if (nextButton) {
            nextButton.hidden = urls.length <= 1;
        }
    }

    function open(urlList, startIndex) {
        urls = urlList.slice();
        index = Math.max(0, Math.min(startIndex || 0, urls.length - 1));
        render();
        lastFocused = document.activeElement;
        lightbox.hidden = false;
        lightbox.setAttribute('aria-hidden', 'false');
        document.body.classList.add('image-lightbox-open');
        var closeButton = lightbox.querySelector('[data-image-lightbox-close]');
        if (closeButton) {
            closeButton.focus();
        }
    }

    function close() {
        lightbox.hidden = true;
        lightbox.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('image-lightbox-open');
        image.removeAttribute('src');
        urls = [];
        index = 0;
        if (lastFocused) {
            lastFocused.focus();
            lastFocused = null;
        }
    }

    document.addEventListener('click', function (event) {
        var thumb = event.target.closest('.image-gallery-thumb-button');
        if (thumb) {
            var gallery = thumb.closest('[data-image-gallery]');
            if (!gallery) {
                return;
            }
            var csv = gallery.getAttribute('data-image-gallery-urls') || '';
            if (!csv) {
                return;
            }
            event.preventDefault();
            open(csv.split('|').filter(function (url) { return !!url; }),
                parseInt(thumb.getAttribute('data-image-gallery-index'), 10) || 0);
            return;
        }

        if (lightbox.hidden) {
            return;
        }
        if (event.target.closest('[data-image-lightbox-close]')) {
            event.preventDefault();
            close();
            return;
        }
        if (event.target.closest('[data-image-lightbox-prev]')) {
            event.preventDefault();
            index -= 1;
            render();
            return;
        }
        if (event.target.closest('[data-image-lightbox-next]')) {
            event.preventDefault();
            index += 1;
            render();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (lightbox.hidden) {
            return;
        }
        if (event.key === 'Escape') {
            close();
            return;
        }
        if (event.key === 'ArrowLeft') {
            index -= 1;
            render();
            return;
        }
        if (event.key === 'ArrowRight') {
            index += 1;
            render();
        }
    });
}());
