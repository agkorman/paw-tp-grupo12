(function () {
    'use strict';

    // Shared image picker used by every <pa:image-upload> root. Owns file
    // accumulation, the preview carousel, existing-image-in-carousel editing
    // (retainedImageIds), and client-side file validation. Forms differ only
    // through the data-* config the tag renders (max count, validation limits).
    var ALLOWED_IMAGE_TYPES_DEFAULT = ['image/jpeg', 'image/png', 'image/webp'];
    var MAX_IMAGE_BYTES_DEFAULT = 10 * 1024 * 1024;

    var registry = {};

    function formatMessage(template) {
        var args = Array.prototype.slice.call(arguments, 1);
        return (template || '').replace(/\{(\d+)}/g, function (match, index) {
            return args[index] == null ? match : args[index];
        });
    }

    function createPicker(root) {
        var prefix = root.getAttribute('data-name-prefix');
        if (!prefix) {
            return null;
        }

        var byId = function (suffix) {
            return document.getElementById(prefix + suffix);
        };

        var fileInput = byId('File');
        if (!fileInput) {
            return null;
        }

        var fileStatus = byId('FileStatus');
        var filePreview = byId('ImagePreview');
        var filePreviewImg = byId('ImagePreviewImg');
        var filePreviewPrev = byId('ImagePrev');
        var filePreviewNext = byId('ImageNext');
        var filePreviewRemove = byId('ImageRemove');
        var filePreviewCounter = byId('ImageCounter');
        var filePreviewThumbnails = byId('ImageThumbnails');
        var retainedImageInputs = byId('RetainedImageInputs');
        var fileUpload = fileInput.closest('.car-image-upload');
        var form = fileInput.form;
        var canSyncFileInput = typeof window.DataTransfer === 'function';

        var messages = root.dataset;
        var emptyFileStatus = messages.msgFileEmpty || (fileStatus ? fileStatus.textContent : '');

        var splitList = function (raw) {
            return (raw || '').split('|').filter(function (value) {
                return !!value;
            });
        };

        var existingImageUrls = splitList(messages.existingImageUrls);
        var existingImageIds = splitList(messages.existingImageIds);
        var existingImageCount = existingImageUrls.length;
        var existingImageStatus = messages.existingImageStatus || '';
        var requireImage = messages.requireImage === 'true';

        var allowedTypes = messages.allowedTypes
            ? splitList(messages.allowedTypes)
            : ALLOWED_IMAGE_TYPES_DEFAULT;
        var maxImageBytes = messages.maxBytes ? parseInt(messages.maxBytes, 10) : MAX_IMAGE_BYTES_DEFAULT;
        if (!Number.isFinite(maxImageBytes) || maxImageBytes < 1) {
            maxImageBytes = MAX_IMAGE_BYTES_DEFAULT;
        }
        var maxTotalImages = parseInt(messages.maxImageCount, 10);
        if (!Number.isFinite(maxTotalImages) || maxTotalImages < 1) {
            maxTotalImages = 5;
        }

        var previewImages = [];
        var previewObjectUrls = [];
        var previewIndex = 0;
        var accumulatedFiles = [];

        var selectedFiles = function () {
            if (!fileInput.files) {
                return [];
            }
            return Array.prototype.slice.call(fileInput.files).filter(function (file) {
                return file && file.size > 0;
            });
        };

        // --- Inline error rendering (mirrors the forms' .client-form-error pattern) ---
        var errorId = fileInput.id + 'ClientError';

        var fieldContainer = function () {
            var node = fileInput;
            while (node && node !== form && node !== document.body) {
                if (node.classList && node.classList.contains('modal-field')) {
                    return node;
                }
                node = node.parentNode;
            }
            return fileInput.parentNode || form;
        };

        var setDescribedBy = function () {
            var ids = (fileInput.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
            if (ids.indexOf(errorId) === -1) {
                ids.push(errorId);
                fileInput.setAttribute('aria-describedby', ids.join(' '));
            }
        };

        var removeDescribedBy = function () {
            var ids = (fileInput.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
                return id && id !== errorId;
            });
            if (ids.length) {
                fileInput.setAttribute('aria-describedby', ids.join(' '));
            } else {
                fileInput.removeAttribute('aria-describedby');
            }
        };

        var setInlineError = function (message) {
            var container = fieldContainer();
            var error = container.querySelector('[data-client-error-for="' + fileInput.id + '"]');
            if (!error) {
                error = document.createElement('span');
                error.id = errorId;
                error.className = 'form-error client-form-error';
                error.setAttribute('data-client-error-for', fileInput.id);
                error.setAttribute('role', 'alert');
                container.appendChild(error);
            }
            error.textContent = message;
            error.hidden = false;
            fileInput.classList.add('is-invalid');
            fileInput.setAttribute('aria-invalid', 'true');
            setDescribedBy();
        };

        var clearInlineError = function () {
            var container = fieldContainer();
            var error = container.querySelector('[data-client-error-for="' + fileInput.id + '"]');
            if (error) {
                error.textContent = '';
                error.hidden = true;
            }
            fileInput.classList.remove('is-invalid');
            fileInput.removeAttribute('aria-invalid');
            removeDescribedBy();
        };

        // --- File accumulation + retained existing-image inputs ---
        var fileKey = function (file) {
            return file.name + '|' + file.size + '|' + file.lastModified;
        };

        var syncInputFromAccumulator = function () {
            if (!canSyncFileInput) {
                return;
            }
            var dt = new DataTransfer();
            accumulatedFiles.forEach(function (file) {
                dt.items.add(file);
            });
            fileInput.files = dt.files;
        };

        var syncRetainedImageInputs = function () {
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
        };

        var appendToAccumulator = function (newFiles) {
            if (!canSyncFileInput) {
                accumulatedFiles = newFiles.slice(0, Math.max(0, maxTotalImages - existingImageCount));
                return;
            }

            var existing = {};
            accumulatedFiles.forEach(function (file) {
                existing[fileKey(file)] = true;
            });
            var added = false;
            newFiles.forEach(function (file) {
                if (existingImageCount + accumulatedFiles.length >= maxTotalImages) {
                    return;
                }
                var key = fileKey(file);
                if (existing[key]) {
                    return;
                }
                existing[key] = true;
                accumulatedFiles.push(file);
                added = true;
            });
            if (added) {
                syncInputFromAccumulator();
            }
        };

        // --- Preview carousel ---
        var revokePreviewObjectUrls = function () {
            previewObjectUrls.forEach(function (url) {
                window.URL.revokeObjectURL(url);
            });
            previewObjectUrls = [];
        };

        var renderPreview = function () {
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

            var totalVisibleImages = existingImageCount + accumulatedFiles.length;
            var showAddMore = canSyncFileInput && totalVisibleImages >= 1 && totalVisibleImages < maxTotalImages;
            if (showAddMore) {
                var addMore = document.createElement('button');
                addMore.type = 'button';
                addMore.className = 'car-image-upload-add-more';
                addMore.id = prefix + 'ImageAddMore';
                addMore.setAttribute('aria-label', messages.msgImageAddMore || '');
                addMore.textContent = '+';
                filePreviewThumbnails.appendChild(addMore);
            }

            filePreviewThumbnails.toggleAttribute('hidden', !(previewImages.length > 1 || showAddMore));
        };

        var setPreviewImages = function (imageUrls, objectUrls, nextPreviewIndex) {
            revokePreviewObjectUrls();
            previewImages = imageUrls || [];
            previewIndex = typeof nextPreviewIndex === 'number' ? nextPreviewIndex : 0;
            previewObjectUrls = objectUrls || [];
            renderPreview();
        };

        var setPreviewFromFiles = function (files, nextPreviewIndex) {
            var objectUrls = files.map(function (file) {
                return window.URL.createObjectURL(file);
            });
            setPreviewImages(existingImageUrls.concat(objectUrls), objectUrls, nextPreviewIndex);
        };

        var updateFileState = function (nextPreviewIndex) {
            if (!fileStatus || !fileUpload) {
                return;
            }

            var files = selectedFiles();
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

            clearInlineError();
        };

        // --- Client-side validation (host calls validate() at submit) ---
        var validate = function () {
            clearInlineError();
            var files = selectedFiles();
            var totalCount = existingImageCount + files.length;

            if (requireImage && totalCount === 0) {
                setInlineError(messages.msgRequiredImage || messages.msgRequiredGeneric || '');
                return false;
            }
            if (totalCount > maxTotalImages) {
                setInlineError(formatMessage(messages.msgImageMaxCount, maxTotalImages));
                return false;
            }
            for (var i = 0; i < files.length; i++) {
                if (!files[i].type || allowedTypes.indexOf(files[i].type) === -1) {
                    setInlineError(messages.msgImageUnsupportedType);
                    return false;
                }
                if (files[i].size > maxImageBytes) {
                    setInlineError(messages.msgImageTooLarge);
                    return false;
                }
            }
            return true;
        };

        // --- Event wiring ---
        fileInput.addEventListener('change', function (event) {
            var picked = Array.prototype.slice.call(event.target.files || []).filter(function (file) {
                return file && file.size > 0;
            });
            if (picked.length === 0) {
                syncInputFromAccumulator();
                return;
            }
            appendToAccumulator(picked);
            updateFileState();
            validate();
        });

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
                } else {
                    if (!canSyncFileInput) {
                        return;
                    }
                    if (fileIndex < 0 || fileIndex >= accumulatedFiles.length) {
                        return;
                    }
                    accumulatedFiles.splice(fileIndex, 1);
                    syncInputFromAccumulator();
                }

                if (previewIndex >= existingImageCount + accumulatedFiles.length) {
                    previewIndex -= 1;
                }
                updateFileState(previewIndex);
                validate();
            });
        }

        if (filePreviewThumbnails) {
            filePreviewThumbnails.addEventListener('click', function (event) {
                var addMore = event.target.closest('.car-image-upload-add-more');
                if (addMore) {
                    event.preventDefault();
                    event.stopPropagation();
                    fileInput.click();
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

        syncRetainedImageInputs();
        updateFileState();

        return {
            validate: validate,
            isValid: function () {
                return validate();
            }
        };
    }

    function initAll() {
        Array.prototype.slice.call(document.querySelectorAll('[data-image-picker]')).forEach(function (root) {
            var prefix = root.getAttribute('data-name-prefix');
            if (!prefix || registry[prefix]) {
                return;
            }
            var picker = createPicker(root);
            if (picker) {
                registry[prefix] = picker;
            }
        });
    }

    window.ImageUploadPicker = window.ImageUploadPicker || {};
    window.ImageUploadPicker.get = function (prefix) {
        return registry[prefix] || null;
    };
    window.ImageUploadPicker.init = initAll;

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAll);
    } else {
        initAll();
    }
}());
