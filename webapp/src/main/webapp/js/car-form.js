(function () {
    var page = document.getElementById('createCarFormPage');
    var form = document.getElementById('createCarForm');
    var fileInput = document.getElementById('modalCarFile');
    var fileStatus = document.getElementById('modalCarFileStatus');
    var filePreview = document.getElementById('modalCarImagePreview');
    var filePreviewImg = document.getElementById('modalCarImagePreviewImg');
    var filePreviewPrev = document.getElementById('modalCarImagePrev');
    var filePreviewNext = document.getElementById('modalCarImageNext');
    var filePreviewRemove = document.getElementById('modalCarImageRemove');
    var filePreviewCounter = document.getElementById('modalCarImageCounter');
    var filePreviewThumbnails = document.getElementById('modalCarImageThumbnails');
    var retainedImageInputs = document.getElementById('modalCarRetainedImageInputs');
    var fileUpload = fileInput ? fileInput.closest('.car-image-upload') : null;
    var canSyncFileInput = fileInput && typeof window.DataTransfer === 'function';

    if (!page || !form) {
        return;
    }

    var messages = page.dataset;
    var emptyFileStatus = messages.msgFileEmpty || (fileStatus ? fileStatus.textContent : '');
    var existingImageUrls = (page.dataset.existingImageUrls || '').split('|').filter(Boolean);
    var existingImageIds = (page.dataset.existingImageIds || '').split('|').filter(Boolean);
    var existingImageStatus = page.dataset.existingImageStatus || '';
    var existingImageCount = existingImageUrls.length;
    var previewImages = [];
    var previewObjectUrls = [];
    var previewIndex = 0;
    var accumulatedFiles = [];
    var maxTotalImages = parseInt(page.dataset.maxImageCount, 10);
    if (!Number.isFinite(maxTotalImages) || maxTotalImages < 1) {
        maxTotalImages = 5;
    }
    var msgImageMaxCount = page.dataset.msgImageMaxCount || '';

    function formatMessage(template) {
        var args = Array.prototype.slice.call(arguments, 1);
        return (template || '').replace(/\{(\d+)}/g, function (match, index) {
            return args[index] == null ? match : args[index];
        });
    }

    function selectedFiles(field) {
        if (!field || !field.files) {
            return [];
        }
        return Array.prototype.slice.call(field.files).filter(function (file) {
            return file && file.size > 0;
        });
    }

    function fileKey(file) {
        return file.name + '|' + file.size + '|' + file.lastModified;
    }

    function syncInputFromAccumulator() {
        if (!canSyncFileInput) {
            return;
        }
        var dt = new DataTransfer();
        accumulatedFiles.forEach(function (file) {
            dt.items.add(file);
        });
        fileInput.files = dt.files;
    }

    function maxNewSlotsBudget() {
        return Math.max(0, maxTotalImages - existingImageCount);
    }

    function syncRetainedImageInputs() {
        if (!retainedImageInputs) {
            return;
        }
        retainedImageInputs.textContent = '';
        existingImageIds.forEach(function (imageId) {
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'retainedImageIds';
            input.value = imageId;
            retainedImageInputs.appendChild(input);
        });
    }

    function appendToAccumulator(newFiles) {
        var maxNew = maxNewSlotsBudget();
        if (!canSyncFileInput) {
            accumulatedFiles = [];
            var seenLegacy = {};
            for (var i = 0; i < newFiles.length && accumulatedFiles.length < maxNew; i += 1) {
                var f = newFiles[i];
                var k = fileKey(f);
                if (!seenLegacy[k]) {
                    seenLegacy[k] = true;
                    accumulatedFiles.push(f);
                }
            }
            return;
        }
        var existing = {};
        accumulatedFiles.forEach(function (file) {
            existing[fileKey(file)] = true;
        });
        newFiles.forEach(function (file) {
            if (accumulatedFiles.length >= maxNew) {
                return;
            }
            var key = fileKey(file);
            if (!existing[key]) {
                existing[key] = true;
                accumulatedFiles.push(file);
            }
        });
        syncInputFromAccumulator();
    }

    function revokePreviewObjectUrls() {
        previewObjectUrls.forEach(function (url) {
            window.URL.revokeObjectURL(url);
        });
        previewObjectUrls = [];
    }

    function renderPreview() {
        if (!filePreview || !filePreviewImg || !fileUpload) {
            return;
        }
        if (previewImages.length === 0) {
            filePreviewImg.removeAttribute('src');
            filePreview.setAttribute('hidden', 'hidden');
            filePreview.setAttribute('aria-hidden', 'true');
            fileUpload.classList.remove('has-preview');
            if (filePreviewThumbnails) {
                filePreviewThumbnails.setAttribute('hidden', 'hidden');
                filePreviewThumbnails.textContent = '';
            }
            return;
        }
        previewIndex = (previewIndex + previewImages.length) % previewImages.length;
        filePreviewImg.src = previewImages[previewIndex];
        filePreview.removeAttribute('hidden');
        filePreview.setAttribute('aria-hidden', 'false');
        fileUpload.classList.add('has-preview');
        if (filePreviewCounter) {
            filePreviewCounter.textContent = (previewIndex + 1) + ' / ' + previewImages.length;
        }
        if (filePreviewPrev) {
            filePreviewPrev.toggleAttribute('hidden', previewImages.length <= 1);
        }
        if (filePreviewNext) {
            filePreviewNext.toggleAttribute('hidden', previewImages.length <= 1);
        }
        if (filePreviewRemove) {
            filePreviewRemove.toggleAttribute('hidden', !canSyncFileInput && previewIndex >= existingImageCount);
        }
        if (!filePreviewThumbnails) {
            return;
        }
        filePreviewThumbnails.textContent = '';
        previewImages.forEach(function (url, index) {
            var button = document.createElement('button');
            button.type = 'button';
            button.className = 'car-image-upload-thumb' + (index === previewIndex ? ' is-active' : '');
            button.setAttribute('data-upload-preview-index', String(index));
            button.setAttribute('aria-label', formatMessage(messages.msgImagePreview, index + 1));
            var img = document.createElement('img');
            img.src = url;
            img.alt = '';
            button.appendChild(img);
            filePreviewThumbnails.appendChild(button);
        });
        if (canSyncFileInput) {
            var addMore = document.createElement('button');
            addMore.type = 'button';
            addMore.className = 'car-image-upload-add-more';
            addMore.id = 'modalCarImageAddMore';
            addMore.setAttribute('aria-label', messages.msgImageAddMore);
            addMore.textContent = '+';
            filePreviewThumbnails.appendChild(addMore);
        }
        filePreviewThumbnails.toggleAttribute('hidden', previewImages.length <= 1);
    }

    function setPreviewImages(imageUrls, objectUrls, nextPreviewIndex) {
        revokePreviewObjectUrls();
        previewImages = imageUrls || [];
        previewIndex = typeof nextPreviewIndex === 'number' ? nextPreviewIndex : 0;
        previewObjectUrls = objectUrls || [];
        renderPreview();
    }

    function setPreviewFromFiles(files, nextPreviewIndex) {
        var objectUrls = files.map(function (file) {
            return window.URL.createObjectURL(file);
        });
        setPreviewImages(existingImageUrls.concat(objectUrls), objectUrls, nextPreviewIndex);
    }

    function updateFileState(nextPreviewIndex) {
        if (!fileInput || !fileStatus || !fileUpload) {
            return;
        }
        var files = selectedFiles(fileInput);
        var maxNew = maxNewSlotsBudget();
        if (files.length > maxNew && maxNew >= 0) {
            files = files.slice(0, maxNew);
        }
        fileUpload.classList.toggle('has-file', files.length > 0 || existingImageUrls.length > 0);
        if (files.length === 1) {
            fileStatus.textContent = existingImageCount > 0 ? files[0].name + ' ' + messages.msgImageAddSuffix : files[0].name;
            setPreviewFromFiles(files, nextPreviewIndex);
        } else if (files.length > 1) {
            fileStatus.textContent = formatMessage(messages.msgImageMultiple, files.length)
                + (existingImageCount > 0 ? ' ' + messages.msgImageAddSuffix : '');
            setPreviewFromFiles(files, nextPreviewIndex);
        } else if (existingImageUrls.length > 0) {
            fileStatus.textContent = existingImageStatus || (existingImageUrls.length === 1
                ? messages.msgImageLoadedOne
                : formatMessage(messages.msgImageLoadedMultiple, existingImageUrls.length));
            setPreviewImages(existingImageUrls, [], nextPreviewIndex);
        } else {
            fileStatus.textContent = emptyFileStatus;
            setPreviewImages([], []);
        }
    }

    if (fileInput) {
        fileInput.addEventListener('change', function (event) {
            var picked = Array.prototype.slice.call(event.target.files || []).filter(function (file) {
                return file && file.size > 0;
            });
            if (picked.length === 0) {
                syncInputFromAccumulator();
                return;
            }
            var room = Math.max(0, maxNewSlotsBudget() - accumulatedFiles.length);
            appendToAccumulator(picked);
            updateFileState();
            if (picked.length > room && msgImageMaxCount && fileStatus) {
                fileStatus.textContent = formatMessage(msgImageMaxCount, maxTotalImages);
            }
        });
    }
    if (filePreviewPrev) {
        filePreviewPrev.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            previewIndex -= 1;
            renderPreview();
        });
    }
    if (filePreviewNext) {
        filePreviewNext.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            previewIndex += 1;
            renderPreview();
        });
    }
    if (filePreviewRemove) {
        filePreviewRemove.addEventListener('click', function (event) {
            event.preventDefault();
            event.stopPropagation();
            var fileIndex = previewIndex - existingImageCount;
            if (previewIndex < existingImageCount) {
                existingImageUrls.splice(previewIndex, 1);
                existingImageIds.splice(previewIndex, 1);
                existingImageCount = existingImageUrls.length;
                syncRetainedImageInputs();
            } else if (canSyncFileInput && fileIndex >= 0 && fileIndex < accumulatedFiles.length) {
                accumulatedFiles.splice(fileIndex, 1);
                syncInputFromAccumulator();
            }
            if (previewIndex >= existingImageCount + accumulatedFiles.length) {
                previewIndex -= 1;
            }
            updateFileState(previewIndex);
        });
    }
    if (filePreviewThumbnails) {
        filePreviewThumbnails.addEventListener('click', function (event) {
            var addMore = event.target.closest('.car-image-upload-add-more');
            if (addMore) {
                event.preventDefault();
                event.stopPropagation();
                if (fileInput) {
                    fileInput.click();
                }
                return;
            }
            var thumb = event.target.closest('[data-upload-preview-index]');
            if (!thumb) {
                return;
            }
            event.preventDefault();
            event.stopPropagation();
            previewIndex = Number(thumb.getAttribute('data-upload-preview-index') || 0);
            renderPreview();
        });
    }

    form.noValidate = true;
    updateFileState();
}());
