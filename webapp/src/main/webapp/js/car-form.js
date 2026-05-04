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
    var formatMessage = function (template) {
        var args = Array.prototype.slice.call(arguments, 1);
        return (template || '').replace(/\{(\d+)}/g, function (match, index) {
            return args[index] == null ? match : args[index];
        });
    };
    var emptyFileStatus = messages.msgFileEmpty || (fileStatus ? fileStatus.textContent : '');
    var existingImageUrls = (page.dataset.existingImageUrls || '').split('|').filter(function (url) {
        return !!url;
    });
    var existingImageIds = (page.dataset.existingImageIds || '').split('|').filter(function (id) {
        return !!id;
    });
    var existingImageCount = existingImageUrls.length;
    var existingImageStatus = page.dataset.existingImageStatus || '';
    var adminMode = page.dataset.adminMode === 'true';
    var previewImages = [];
    var previewObjectUrls = [];
    var previewIndex = 0;
    var accumulatedFiles = [];
    var requiredMessages = {
        modalCarBrand: messages.msgRequiredBrand,
        modalCarBodyType: messages.msgRequiredBodyType,
        modalCarModel: messages.msgRequiredModel,
        modalCarDescription: messages.msgRequiredDescription,
        modalCarHorsepower: messages.msgRequiredHorsepower,
        modalCarAirbagCount: messages.msgRequiredAirbags,
        modalCarFuelConsumption: messages.msgRequiredConsumption,
        modalCarMaxSpeed: messages.msgRequiredMaxSpeed,
        modalCarFile: messages.msgRequiredImage
    };

    // Must mirror server-side uploaded image validation.
    var EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    var ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
    var MAX_IMAGE_BYTES = 10 * 1024 * 1024;
    var MAX_IMAGE_COUNT = 5;

    var selectedFiles = function (field) {
        if (!field || !field.files) {
            return [];
        }
        return Array.prototype.slice.call(field.files).filter(function (file) {
            return file && file.size > 0;
        });
    };

    var fieldKey = function (field) {
        if (field.type === 'radio') {
            return field.name || field.id || 'field';
        }
        return field.id || field.name || 'field';
    };

    var fieldContainer = function (field) {
        var node = field;
        while (node && node !== page) {
            if (node.classList && node.classList.contains('modal-field')) {
                return node;
            }
            node = node.parentNode;
        }
        return field ? field.parentNode : form;
    };

    var clientErrorId = function (field) {
        return fieldKey(field) + 'ClientError';
    };

    var setDescribedBy = function (field, errorId) {
        var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (ids.indexOf(errorId) === -1) {
            ids.push(errorId);
            field.setAttribute('aria-describedby', ids.join(' '));
        }
    };

    var removeDescribedBy = function (field, errorId) {
        var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
            return id && id !== errorId;
        });
        if (ids.length) {
            field.setAttribute('aria-describedby', ids.join(' '));
        } else {
            field.removeAttribute('aria-describedby');
        }
    };

    var radioGroup = function (field) {
        return Array.prototype.slice.call(form.querySelectorAll('input[type="radio"][name="' + field.name + '"]'));
    };

    var setInlineError = function (field, message) {
        if (!field) {
            return;
        }
        var container = fieldContainer(field);
        var key = fieldKey(field);
        var errorId = clientErrorId(field);
        var error = container.querySelector('[data-client-error-for="' + key + '"]');
        if (!error) {
            error = document.createElement('span');
            error.id = errorId;
            error.className = 'form-error client-form-error';
            error.setAttribute('data-client-error-for', key);
            error.setAttribute('role', 'alert');
            container.appendChild(error);
        }
        error.textContent = message;
        error.hidden = false;

        if (field.type === 'radio') {
            radioGroup(field).forEach(function (radio) {
                radio.setAttribute('aria-invalid', 'true');
                setDescribedBy(radio, errorId);
            });
        } else {
            field.classList.add('is-invalid');
            field.setAttribute('aria-invalid', 'true');
            setDescribedBy(field, errorId);
        }
    };

    var clearInlineError = function (field) {
        if (!field) {
            return;
        }
        var container = fieldContainer(field);
        var key = fieldKey(field);
        var errorId = clientErrorId(field);
        var error = container.querySelector('[data-client-error-for="' + key + '"]');
        if (error) {
            error.textContent = '';
            error.hidden = true;
        }

        if (field.type === 'radio') {
            radioGroup(field).forEach(function (radio) {
                radio.removeAttribute('aria-invalid');
                removeDescribedBy(radio, errorId);
            });
        } else {
            field.classList.remove('is-invalid');
            field.removeAttribute('aria-invalid');
            removeDescribedBy(field, errorId);
        }
    };

    var hasRequiredRadioGroup = function (field) {
        return field.type === 'radio' && radioGroup(field).some(function (radio) {
            return radio.required;
        });
    };

    var isMissingRequiredValue = function (field) {
        if (field.type === 'file') {
            return selectedFiles(field).length === 0;
        }
        return !field.value || field.value.trim() === '';
    };

    var validateField = function (field) {
        if (!field || field.disabled) {
            return true;
        }

        clearInlineError(field);

        if ((field.required || hasRequiredRadioGroup(field)) && isMissingRequiredValue(field)) {
            setInlineError(field, requiredMessages[field.id] || messages.msgRequiredGeneric);
            return false;
        }

        if (field.type === 'radio' && hasRequiredRadioGroup(field)
                && !radioGroup(field).some(function (radio) { return radio.checked; })) {
            setInlineError(field, messages.msgRadioRequired);
            return false;
        }

        if (field.type === 'email' && field.value && !EMAIL_PATTERN.test(field.value.trim())) {
            setInlineError(field, messages.msgEmailInvalid);
            return false;
        }

        if (field.type === 'number' && field.value) {
            var parsed = Number(field.value);
            if (!Number.isFinite(parsed)) {
                setInlineError(field, messages.msgNumberInvalid);
                return false;
            }
            if (field.min !== '' && parsed < Number(field.min)) {
                setInlineError(field, formatMessage(messages.msgNumberMin, field.min));
                return false;
            }
            if (field.max !== '' && parsed > Number(field.max)) {
                setInlineError(field, formatMessage(messages.msgNumberMax, field.max));
                return false;
            }
        }

        if (field.type === 'file') {
            var files = selectedFiles(field);
            if ((field.required || adminMode || existingImageCount === 0)
                    && existingImageCount + files.length === 0) {
                setInlineError(field, requiredMessages[field.id] || messages.msgRequiredGeneric);
                return false;
            }
            if (existingImageCount + files.length > MAX_IMAGE_COUNT) {
                setInlineError(field, formatMessage(messages.msgImageMaxCount, MAX_IMAGE_COUNT));
                return false;
            }
            for (var i = 0; i < files.length; i++) {
                if (!files[i].type || ALLOWED_IMAGE_TYPES.indexOf(files[i].type) === -1) {
                    setInlineError(field, messages.msgImageUnsupportedType);
                    return false;
                }
                if (files[i].size > MAX_IMAGE_BYTES) {
                    setInlineError(field, messages.msgImageTooLarge);
                    return false;
                }
            }
        }

        return true;
    };

    var validateFields = function () {
        var seenRadioGroups = {};
        return Array.prototype.slice.call(form.querySelectorAll('input, textarea, select')).reduce(function (isValid, field) {
            if (field.type === 'hidden') {
                return isValid;
            }
            if (field.type === 'radio') {
                if (seenRadioGroups[field.name]) {
                    return isValid;
                }
                seenRadioGroups[field.name] = true;
            }
            return validateField(field) && isValid;
        }, true);
    };

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
            accumulatedFiles = newFiles.slice(0, Math.max(0, MAX_IMAGE_COUNT - existingImageCount));
            return;
        }

        var existing = {};
        accumulatedFiles.forEach(function (file) {
            existing[fileKey(file)] = true;
        });
        var added = false;
        newFiles.forEach(function (file) {
            if (existingImageCount + accumulatedFiles.length >= MAX_IMAGE_COUNT) {
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
        var showAddMore = canSyncFileInput && totalVisibleImages >= 1 && totalVisibleImages < MAX_IMAGE_COUNT;
        if (showAddMore) {
            var addMore = document.createElement('button');
            addMore.type = 'button';
            addMore.className = 'car-image-upload-add-more';
            addMore.id = 'modalCarImageAddMore';
            addMore.setAttribute('aria-label', messages.msgImageAddMore);
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
        if (!fileInput || !fileStatus || !fileUpload) {
            return;
        }

        var files = selectedFiles(fileInput);
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

        clearInlineError(fileInput);
    };

    if (fileInput) {
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
            validateField(fileInput);
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
            validateField(fileInput);
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

    Array.prototype.slice.call(form.querySelectorAll('input, textarea, select')).forEach(function (field) {
        if (field.type === 'hidden') {
            return;
        }
        var eventName = field.tagName === 'SELECT' || field.type === 'file' || field.type === 'radio' ? 'change' : 'input';
        field.addEventListener(eventName, function () {
            validateField(field);
        });
    });

    form.addEventListener('submit', function (event) {
        if (!validateFields()) {
            event.preventDefault();
            var invalid = form.querySelector('[aria-invalid="true"]');
            if (invalid && typeof invalid.focus === 'function') {
                invalid.focus();
            }
        }
    });

    updateFileState();
})();
