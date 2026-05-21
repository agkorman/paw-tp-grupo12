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
    var emptyStatusText = statusEl ? statusEl.textContent : '';
    var maxCount = 3;

    var objectUrls = [];
    var previewIndex = 0;
    var files = [];

    function release() {
        objectUrls.forEach(function (u) {
            try { URL.revokeObjectURL(u); } catch (e) { /* ignore */ }
        });
        objectUrls = [];
    }

    function render() {
        release();
        if (!files.length) {
            if (preview) { preview.hidden = true; preview.setAttribute('aria-hidden', 'true'); }
            if (fileUpload) { fileUpload.classList.remove('has-preview'); fileUpload.classList.remove('has-file'); }
            if (statusEl) { statusEl.textContent = emptyStatusText; }
            return;
        }
        files.forEach(function (f) { objectUrls.push(URL.createObjectURL(f)); });
        if (previewIndex >= files.length) { previewIndex = 0; }
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
                : files.length + ' archivos seleccionados';
        }
    }

    function syncInput() {
        if (typeof window.DataTransfer !== 'function') { return; }
        var dt = new DataTransfer();
        files.forEach(function (f) { dt.items.add(f); });
        fileInput.files = dt.files;
    }

    fileInput.addEventListener('change', function () {
        var picked = Array.prototype.slice.call(fileInput.files || []);
        files = picked.slice(0, maxCount);
        previewIndex = 0;
        syncInput();
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
}());
