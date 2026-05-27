(function () {
    'use strict';

    var lightbox = document.getElementById('reviewImageLightbox');
    if (!lightbox) {
        return;
    }
    var img = lightbox.querySelector('[data-lightbox-img]');
    var counter = lightbox.querySelector('[data-lightbox-count]');
    var prevBtn = lightbox.querySelector('[data-lightbox-prev]');
    var nextBtn = lightbox.querySelector('[data-lightbox-next]');

    var urls = [];
    var index = 0;

    function render() {
        if (!urls.length) { return; }
        if (index < 0) { index = urls.length - 1; }
        if (index >= urls.length) { index = 0; }
        img.src = urls[index];
        if (counter) { counter.textContent = (index + 1) + ' / ' + urls.length; }
        var multi = urls.length > 1;
        if (prevBtn) { prevBtn.hidden = !multi; }
        if (nextBtn) { nextBtn.hidden = !multi; }
        if (counter) { counter.hidden = !multi; }
    }

    function open(urlList, startIndex) {
        urls = urlList.slice();
        index = Math.max(0, Math.min(startIndex || 0, urls.length - 1));
        render();
        lightbox.hidden = false;
        lightbox.setAttribute('aria-hidden', 'false');
        document.body.classList.add('review-image-lightbox-open');
    }

    function close() {
        lightbox.hidden = true;
        lightbox.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('review-image-lightbox-open');
        img.src = '';
        urls = [];
        index = 0;
    }

    document.addEventListener('click', function (e) {
        var thumb = e.target.closest('.review-image-thumb-button');
        if (!thumb) { return; }
        var row = thumb.closest('.review-images-row');
        if (!row) { return; }
        var csv = row.getAttribute('data-review-image-urls') || '';
        if (!csv) { return; }
        e.preventDefault();
        var urlList = csv.split('|').filter(function (u) { return u; });
        var startIdx = parseInt(thumb.getAttribute('data-review-image-index'), 10);
        if (isNaN(startIdx)) { startIdx = 0; }
        open(urlList, startIdx);
    });

    lightbox.addEventListener('click', function (e) {
        if (e.target.closest('[data-lightbox-close]')) {
            e.preventDefault();
            close();
            return;
        }
        if (e.target.closest('[data-lightbox-prev]')) {
            e.preventDefault();
            index -= 1;
            render();
            return;
        }
        if (e.target.closest('[data-lightbox-next]')) {
            e.preventDefault();
            index += 1;
            render();
        }
    });

    document.addEventListener('keydown', function (e) {
        if (lightbox.hidden) { return; }
        if (e.key === 'Escape') { close(); return; }
        if (e.key === 'ArrowLeft') { index -= 1; render(); return; }
        if (e.key === 'ArrowRight') { index += 1; render(); }
    });
}());
