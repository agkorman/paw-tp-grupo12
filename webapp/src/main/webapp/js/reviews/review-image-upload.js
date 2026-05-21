(function () {
    'use strict';

    var fileInput = document.getElementById('reviewFile');
    if (!fileInput) {
        return;
    }

    var fileUpload = fileInput.closest('.car-image-upload');
    var statusEl = document.getElementById('reviewFileStatus');
    var preview = document.getElementById('reviewImagePreview');
    var previewImg = document.getElementById('reviewImagePreviewImg');
    var prevBtn = document.getElementById('reviewImagePrev');
    var nextBtn = document.getElementById('reviewImageNext');
    var counter = document.getElementById('reviewImageCounter');
    var removeBtn = document.getElementById('reviewImageRemove');
    var thumbnails = document.getElementById('reviewImageThumbnails');
    var emptyStatusText = statusEl ? statusEl.textContent : '';
    var maxCount = 3;
    var canSyncFiles = typeof window.DataTransfer === 'function';

    var objectUrls = [];
    var previewIndex = 0;
    var files = [];

    function fileKey(file) {
        return file.name + '::' + file.size + '::' + (file.lastModified || 0);
    }

    function release() {
        objectUrls.forEach(function (u) {
            try { URL.revokeObjectURL(u); } catch (e) { /* ignore */ }
        });
        objectUrls = [];
    }

    function syncInput() {
        if (!canSyncFiles) { return; }
        var dt = new DataTransfer();
        files.forEach(function (f) { dt.items.add(f); });
        fileInput.files = dt.files;
    }

    function buildThumbnails() {
        if (!thumbnails) { return; }
        thumbnails.innerHTML = '';
        if (!files.length) {
            thumbnails.hidden = true;
            return;
        }
        thumbnails.hidden = false;
        files.forEach(function (f, idx) {
            var btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'car-image-upload-thumb' + (idx === previewIndex ? ' is-active' : '');
            btn.setAttribute('data-thumb-index', idx);
            var img = document.createElement('img');
            img.src = objectUrls[idx];
            img.alt = '';
            btn.appendChild(img);
            var x = document.createElement('span');
            x.className = 'car-image-upload-thumb-remove';
            x.textContent = '×';
            x.setAttribute('data-thumb-remove', idx);
            btn.appendChild(x);
            thumbnails.appendChild(btn);
        });
        if (files.length < maxCount) {
            var addMore = document.createElement('button');
            addMore.type = 'button';
            addMore.className = 'car-image-upload-add-more';
            addMore.setAttribute('data-add-more', '');
            addMore.textContent = '+';
            addMore.setAttribute('aria-label', 'Agregar más imágenes');
            thumbnails.appendChild(addMore);
        }
    }

    function render() {
        release();
        files.forEach(function (f) { objectUrls.push(URL.createObjectURL(f)); });
        if (!files.length) {
            if (preview) { preview.hidden = true; preview.setAttribute('aria-hidden', 'true'); }
            if (fileUpload) { fileUpload.classList.remove('has-preview'); fileUpload.classList.remove('has-file'); }
            if (statusEl) { statusEl.textContent = emptyStatusText; }
            buildThumbnails();
            return;
        }
        if (previewIndex >= files.length) { previewIndex = files.length - 1; }
        if (previewIndex < 0) { previewIndex = 0; }
        if (preview && previewImg) {
            previewImg.src = objectUrls[previewIndex];
            preview.hidden = false;
            preview.setAttribute('aria-hidden', 'false');
        }
        if (counter) { counter.textContent = (previewIndex + 1) + ' / ' + files.length; }
        if (prevBtn) { prevBtn.hidden = files.length <= 1; }
        if (nextBtn) { nextBtn.hidden = files.length <= 1; }
        if (removeBtn) { removeBtn.hidden = false; }
        if (fileUpload) { fileUpload.classList.add('has-preview'); fileUpload.classList.add('has-file'); }
        if (statusEl) {
            statusEl.textContent = files.length === 1
                ? files[0].name
                : files.length + ' imágenes seleccionadas';
        }
        buildThumbnails();
    }

    fileInput.addEventListener('change', function () {
        var picked = Array.prototype.slice.call(fileInput.files || []);
        var seen = {};
        files.forEach(function (f) { seen[fileKey(f)] = true; });
        picked.forEach(function (f) {
            if (files.length >= maxCount) { return; }
            var key = fileKey(f);
            if (seen[key]) { return; }
            seen[key] = true;
            files.push(f);
        });
        // Allow the user to pick the same file again next time and have change fire.
        fileInput.value = '';
        syncInput();
        if (previewIndex >= files.length) { previewIndex = Math.max(0, files.length - 1); }
        render();
    });

    if (prevBtn) {
        prevBtn.addEventListener('click', function (e) {
            e.preventDefault();
            if (!files.length) { return; }
            previewIndex = (previewIndex - 1 + files.length) % files.length;
            render();
        });
    }
    if (nextBtn) {
        nextBtn.addEventListener('click', function (e) {
            e.preventDefault();
            if (!files.length) { return; }
            previewIndex = (previewIndex + 1) % files.length;
            render();
        });
    }
    if (removeBtn) {
        removeBtn.addEventListener('click', function (e) {
            e.preventDefault();
            if (!files.length) { return; }
            files.splice(previewIndex, 1);
            if (previewIndex >= files.length) { previewIndex = Math.max(0, files.length - 1); }
            syncInput();
            render();
        });
    }

    if (thumbnails) {
        thumbnails.addEventListener('click', function (e) {
            var addMore = e.target.closest('[data-add-more]');
            if (addMore) {
                e.preventDefault();
                fileInput.click();
                return;
            }
            var removeEl = e.target.closest('[data-thumb-remove]');
            if (removeEl) {
                e.preventDefault();
                e.stopPropagation();
                var ri = parseInt(removeEl.getAttribute('data-thumb-remove'), 10);
                if (!isNaN(ri) && ri >= 0 && ri < files.length) {
                    files.splice(ri, 1);
                    if (previewIndex >= files.length) { previewIndex = Math.max(0, files.length - 1); }
                    syncInput();
                    render();
                }
                return;
            }
            var thumb = e.target.closest('[data-thumb-index]');
            if (thumb) {
                e.preventDefault();
                var idx = parseInt(thumb.getAttribute('data-thumb-index'), 10);
                if (!isNaN(idx) && idx >= 0 && idx < files.length) {
                    previewIndex = idx;
                    render();
                }
            }
        });
    }
}());
