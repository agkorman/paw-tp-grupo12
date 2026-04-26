(function () {
    var modal = document.getElementById('createCarModal') || document.getElementById('createCarFormPage');
    var form = document.getElementById('createCarForm');
    var fileInput = document.getElementById('modalCarFile');
    var fileStatus = document.getElementById('modalCarFileStatus');
    var filePreview = document.getElementById('modalCarImagePreview');
    var filePreviewImg = document.getElementById('modalCarImagePreviewImg');
    var filePreviewPrev = document.getElementById('modalCarImagePrev');
    var filePreviewNext = document.getElementById('modalCarImageNext');
    var filePreviewCounter = document.getElementById('modalCarImageCounter');
    var filePreviewThumbnails = document.getElementById('modalCarImageThumbnails');
    var fileUpload = fileInput ? fileInput.closest('.car-image-upload') : null;
    var isAdminMode = modal ? modal.dataset.adminMode === 'true' : false;
    var rejectForm = document.getElementById('rejectCarRequestForm');
    var modalKicker = document.getElementById('createCarModalKicker');
    var modalTitle = document.getElementById('createCarModalTitle');
    var modalSubtitle = document.getElementById('createCarModalSubtitle');
    var createActions = document.getElementById('createCarCreateActions');
    var reviewActions = document.getElementById('createCarReviewActions');
    var editActions = document.getElementById('createCarEditActions');
    var submitterEmailField = document.getElementById('modalCarSubmitterEmailField');
    var fileTitle = document.getElementById('modalCarFileTitle');
    var fileHelp = document.getElementById('modalCarFileHelp');
    var fileAction = document.getElementById('modalCarFileAction');
    var currentMode = isAdminMode ? 'review' : 'create';
    var createAction = form ? form.getAttribute('action') : '';

    if (!modal || !form) {
        return;
    }

    var isPageMode = modal.id === 'createCarFormPage';
    var closeElements = Array.prototype.slice.call(modal.querySelectorAll('[data-close-car-modal]'));
    var emptyFileStatus = 'Ninguna imagen seleccionada';
    var previewImages = [];
    var previewObjectUrls = [];
    var previewIndex = 0;
    var lastTrigger = null;
    var requiredMessages = {
        modalCarBrand: 'Seleccioná una marca.',
        modalCarBodyType: 'Seleccioná un tipo de carrocería.',
        modalCarModel: 'Ingresá el modelo.',
        modalCarDescription: 'Ingresá una descripción.',
        modalCarHorsepower: 'Ingresá la potencia.',
        modalCarAirbagCount: 'Ingresá la cantidad de airbags.',
        modalCarFuelConsumption: 'Ingresá el consumo.',
        modalCarMaxSpeed: 'Ingresá la velocidad máxima.',
        modalCarFile: 'Seleccioná al menos una imagen del auto.'
    };

    // Must mirror validateUploadedImages in CarController.java.
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

    var isMissingRequiredValue = function (field) {
        if (field.type === 'file') {
            return selectedFiles(field).length === 0;
        }
        return !field.value || field.value.trim() === '';
    };

    var fieldKey = function (field) {
        if (field.type === 'radio') {
            return field.name || field.id || 'field';
        }
        return field.id || field.name || 'field';
    };

    var fieldContainer = function (field) {
        var node = field;
        while (node && node !== modal) {
            if (node.classList && node.classList.contains('review-modal-field')) {
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
        if (!field || !errorId) {
            return;
        }
        var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (ids.indexOf(errorId) === -1) {
            ids.push(errorId);
            field.setAttribute('aria-describedby', ids.join(' '));
        }
    };

    var removeDescribedBy = function (field, errorId) {
        if (!field || !errorId) {
            return;
        }
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

    var isRadioGroupMissing = function (field) {
        return !radioGroup(field).some(function (radio) {
            return radio.checked;
        });
    };

    var validateField = function (field) {
        if (!field || field.disabled) {
            return true;
        }

        clearInlineError(field);

        if ((field.required || hasRequiredRadioGroup(field)) && isMissingRequiredValue(field)) {
            setInlineError(field, requiredMessages[field.id] || 'Completá este campo.');
            return false;
        }

        if (field.type === 'radio' && hasRequiredRadioGroup(field) && isRadioGroupMissing(field)) {
            setInlineError(field, 'Seleccioná una opción.');
            return false;
        }

        if (field.type === 'email' && field.value && !EMAIL_PATTERN.test(field.value.trim())) {
            setInlineError(field, 'Ingresá un email válido.');
            return false;
        }

        if (field.type === 'number' && field.value) {
            var parsed = Number(field.value);
            if (!Number.isFinite(parsed)) {
                setInlineError(field, 'Ingresá un valor numérico.');
                return false;
            }
            if (field.min !== '' && parsed < Number(field.min)) {
                setInlineError(field, 'Ingresá un valor mayor o igual a ' + field.min + '.');
                return false;
            }
            if (field.max !== '' && parsed > Number(field.max)) {
                setInlineError(field, 'Ingresá un valor menor o igual a ' + field.max + '.');
                return false;
            }
        }

        if (field.type === 'file') {
            var files = selectedFiles(field);
            if (files.length > MAX_IMAGE_COUNT) {
                setInlineError(field, 'Podés cargar hasta ' + MAX_IMAGE_COUNT + ' imágenes.');
                return false;
            }
            for (var i = 0; i < files.length; i++) {
                if (!files[i].type || ALLOWED_IMAGE_TYPES.indexOf(files[i].type) === -1) {
                    setInlineError(field, 'Tipo de imagen no soportado. Usá JPEG, PNG o WEBP.');
                    return false;
                }
                if (files[i].size > MAX_IMAGE_BYTES) {
                    setInlineError(field, 'La imagen no debe superar los 10 MB.');
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

    var focusFirstInvalid = function () {
        var invalid = form.querySelector('[aria-invalid="true"]');
        if (invalid && typeof invalid.focus === 'function') {
            invalid.focus();
        }
    };

    var clearAllClientErrors = function () {
        var errorIds = Array.prototype.slice.call(form.querySelectorAll('.client-form-error')).map(function (error) {
            return error.id;
        }).filter(Boolean);
        Array.prototype.slice.call(form.querySelectorAll('.client-form-error')).forEach(function (error) {
            error.textContent = '';
            error.hidden = true;
        });
        Array.prototype.slice.call(form.querySelectorAll('[aria-invalid="true"]')).forEach(function (field) {
            field.classList.remove('is-invalid');
            field.removeAttribute('aria-invalid');
        });
        Array.prototype.slice.call(form.querySelectorAll('[aria-describedby]')).forEach(function (field) {
            var ids = (field.getAttribute('aria-describedby') || '').split(/\s+/).filter(function (id) {
                return id && errorIds.indexOf(id) === -1;
            });
            if (ids.length) {
                field.setAttribute('aria-describedby', ids.join(' '));
            } else {
                field.removeAttribute('aria-describedby');
            }
        });
    };

    var revokePreviewObjectUrls = function () {
        if (!window.URL || typeof window.URL.revokeObjectURL !== 'function') {
            previewObjectUrls = [];
            return;
        }
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
        if (filePreviewThumbnails) {
            filePreviewThumbnails.textContent = '';
            previewImages.forEach(function (url, index) {
                var button = document.createElement('button');
                button.type = 'button';
                button.className = 'car-image-upload-thumb' + (index === previewIndex ? ' is-active' : '');
                button.setAttribute('data-upload-preview-index', String(index));
                button.setAttribute('aria-label', 'Ver imagen ' + (index + 1));

                var img = document.createElement('img');
                img.src = url;
                img.alt = '';
                button.appendChild(img);
                filePreviewThumbnails.appendChild(button);
            });
            filePreviewThumbnails.toggleAttribute('hidden', previewImages.length <= 1);
        }
    };

    var setPreviewImages = function (imageUrls, shouldRevoke) {
        revokePreviewObjectUrls();
        previewImages = imageUrls || [];
        previewIndex = 0;
        if (shouldRevoke) {
            previewObjectUrls = previewImages.slice();
        }
        renderPreview();
    };

    var clearPreviewImages = function () {
        setPreviewImages([], false);
    };

    var setPreviewFromFiles = function (files) {
        if (!window.URL || typeof window.URL.createObjectURL !== 'function') {
            clearPreviewImages();
            return;
        }
        setPreviewImages(files.map(function (file) {
            return window.URL.createObjectURL(file);
        }), true);
    };

    var updateFileState = function () {
        if (!fileInput || !fileStatus || !fileUpload) {
            return;
        }

        if (currentMode === 'review') {
            fileUpload.classList.toggle('has-file', previewImages.length > 0);
            return;
        }

        var files = selectedFiles(fileInput);
        fileUpload.classList.toggle('has-file', files.length > 0);

        if (files.length === 1) {
            fileStatus.textContent = files[0].name;
            setPreviewFromFiles(files);
        } else if (files.length > 1) {
            fileStatus.textContent = files.length + ' imágenes seleccionadas';
            setPreviewFromFiles(files);
        } else {
            fileStatus.textContent = emptyFileStatus;
            clearPreviewImages();
        }

        clearInlineError(fileInput);
    };

    var resetModalState = function () {
        form.reset();
        clearAllClientErrors();
        if (fileStatus) {
            fileStatus.textContent = emptyFileStatus;
        }
        clearPreviewImages();
        updateFileState();
    };

    var setText = function (element, value) {
        if (element) {
            element.textContent = value;
        }
    };

    var setFieldValue = function (id, value) {
        var field = document.getElementById(id);
        if (field) {
            field.value = value || '';
        }
    };

    var setAdminAction = function (requestId) {
        if (!isAdminMode || !requestId) {
            return;
        }

        var baseUrl = modal.dataset.adminBaseUrl || '/admin';
        form.setAttribute('action', baseUrl + '/requests/' + requestId + '/accept');
        if (rejectForm) {
            rejectForm.setAttribute('action', baseUrl + '/requests/' + requestId + '/reject');
        }
    };

    var setCarEditAction = function (action) {
        if (!isAdminMode || !action) {
            return;
        }

        form.setAttribute('action', action);
    };

    var setFieldReadonly = function (id, readonly) {
        var field = document.getElementById(id);
        if (!field) {
            return;
        }
        if (readonly) {
            field.setAttribute('readonly', 'readonly');
        } else {
            field.removeAttribute('readonly');
        }
    };

    var setFieldDisabled = function (id, disabled) {
        var field = document.getElementById(id);
        if (!field) {
            return;
        }
        if (disabled) {
            field.setAttribute('disabled', 'disabled');
        } else {
            field.removeAttribute('disabled');
        }
    };

    var setRadioGroupReadonly = function (name, readonly) {
        var fields = form.querySelectorAll('input[type="radio"][name="' + name + '"]');
        if (!fields.length) { return; }
        var group = fields[0].closest('.segmented-control-radio-group');
        if (!group) { return; }
        group.classList.toggle('modal-radio-group--readonly', readonly);
        Array.prototype.forEach.call(fields, function (field) {
            if (readonly) {
                field.setAttribute('tabindex', '-1');
            } else {
                field.removeAttribute('tabindex');
            }
        });
    };

    var setRadioGroupValue = function (name, value) {
        var fields = form.querySelectorAll('input[type="radio"][name="' + name + '"]');
        Array.prototype.forEach.call(fields, function (field) {
            field.checked = field.value === value;
        });
    };

    var setSpecFieldsReadonly = function (readonly) {
        setFieldReadonly('modalCarHorsepower', readonly);
        setFieldReadonly('modalCarAirbagCount', readonly);
        setFieldReadonly('modalCarFuelConsumption', readonly);
        setFieldReadonly('modalCarMaxSpeed', readonly);
    };

    var setActionMode = function (mode) {
        if (createActions) {
            createActions.toggleAttribute('hidden', mode !== 'create');
        }
        if (reviewActions) {
            reviewActions.toggleAttribute('hidden', mode !== 'review');
        }
        if (editActions) {
            editActions.toggleAttribute('hidden', mode !== 'edit');
        }
    };

    var setSubmitterEmailVisibility = function (visible) {
        if (submitterEmailField) {
            submitterEmailField.toggleAttribute('hidden', !visible);
            submitterEmailField.style.display = visible ? '' : 'none';
        }
    };

    var hasSubmitterEmail = function (value) {
        return !!value && EMAIL_PATTERN.test(value.trim());
    };

    var setCreateMode = function () {
        currentMode = 'create';
        form.setAttribute('action', createAction);
        setText(modalKicker, 'Nuevo vehículo');
        setText(modalTitle, 'Agregá un auto');
        setText(modalSubtitle, 'Completá los datos del auto. Esta carga se registrará desde el panel de administración.');
        setActionMode('create');
        setSubmitterEmailVisibility(false);

        setFieldReadonly('modalCarSubmitterEmail', false);
        setFieldDisabled('modalCarSubmitterEmail', false);
        setFieldReadonly('modalCarModel', false);
        setFieldReadonly('modalCarDescription', false);
        setSpecFieldsReadonly(false);
        setFieldDisabled('modalCarBrand', false);
        setFieldDisabled('modalCarBodyType', false);
        setFieldDisabled('modalCarFile', false);
        setRadioGroupReadonly('fuelType', false);
        setRadioGroupReadonly('transmission', false);
        if (fileInput) {
            fileInput.setAttribute('required', 'required');
        }
        setText(fileTitle, 'Arrastrá o elegí una imagen del auto');
        setText(fileHelp, 'JPEG, PNG o WEBP. Máximo 10 MB.');
        setText(fileAction, 'Buscar');
        if (fileUpload) {
            fileUpload.classList.remove('is-readonly', 'has-file');
        }
        if (fileStatus) {
            fileStatus.textContent = emptyFileStatus;
        }
        clearPreviewImages();
    };

    var setReviewMode = function () {
        currentMode = 'review';
        setText(modalKicker, 'Solicitud pendiente');
        setText(modalTitle, 'Revisar y editar formulario');
        setText(modalSubtitle, 'Corregí los datos que haga falta antes de aprobar la solicitud.');
        setActionMode('review');
        setSubmitterEmailVisibility(false);

        setFieldReadonly('modalCarSubmitterEmail', true);
        setFieldDisabled('modalCarSubmitterEmail', true);
        setFieldReadonly('modalCarModel', false);
        setFieldReadonly('modalCarDescription', false);
        setSpecFieldsReadonly(false);
        setFieldDisabled('modalCarBrand', false);
        setFieldDisabled('modalCarBodyType', false);
        setFieldDisabled('modalCarFile', false);
        setRadioGroupReadonly('fuelType', false);
        setRadioGroupReadonly('transmission', false);
        if (fileInput) {
            fileInput.removeAttribute('required');
        }
        setText(fileTitle, 'Imagen enviada por el usuario');
        setText(fileHelp, 'Podés reemplazarla cargando una imagen nueva.');
        setText(fileAction, 'Buscar');
        if (fileUpload) {
            fileUpload.classList.remove('is-readonly');
        }
    };

    var setEditCarMode = function () {
        currentMode = 'edit-car';
        setText(modalKicker, 'Catálogo');
        setText(modalTitle, 'Editar auto');
        setText(modalSubtitle, 'Modificá los datos del auto publicado. Si no cargás una imagen nueva, se conserva la actual.');
        setActionMode('edit');
        setSubmitterEmailVisibility(false);

        setFieldReadonly('modalCarSubmitterEmail', true);
        setFieldDisabled('modalCarSubmitterEmail', true);
        setFieldReadonly('modalCarModel', false);
        setFieldReadonly('modalCarDescription', false);
        setSpecFieldsReadonly(false);
        setFieldDisabled('modalCarBrand', false);
        setFieldDisabled('modalCarBodyType', false);
        setFieldDisabled('modalCarFile', false);
        setRadioGroupReadonly('fuelType', false);
        setRadioGroupReadonly('transmission', false);
        if (fileInput) {
            fileInput.removeAttribute('required');
        }
        setText(fileTitle, 'Imagen actual del auto');
        setText(fileHelp, 'Opcional: cargá una nueva imagen para reemplazarla.');
        setText(fileAction, 'Buscar');
        if (fileUpload) {
            fileUpload.classList.remove('is-readonly');
        }
    };

    var parseImageUrls = function (data) {
        var imageUrls = data.requestImageUrls || data.requestImageUrl || '';
        if (!imageUrls) {
            return [];
        }
        return imageUrls.split('|').filter(function (url) {
            return !!url;
        });
    };

    var populateAdminForm = function (trigger) {
        if (!isAdminMode || !trigger) {
            return;
        }

        var data = trigger.dataset;
        var imageUrls = parseImageUrls(data);
        var submitterEmail = hasSubmitterEmail(data.requestSubmitter) ? data.requestSubmitter : '';
        setFieldValue('modalCarSubmitterEmail', submitterEmail);
        setSubmitterEmailVisibility(!!submitterEmail);
        setFieldValue('modalCarBrand', data.requestBrand);
        setFieldValue('modalCarBodyType', data.requestBodyType);
        setFieldValue('modalCarModel', data.requestModel);
        setFieldValue('modalCarDescription', data.requestDescription);
        setRadioGroupValue('fuelType', data.requestFuelType);
        setRadioGroupValue('transmission', data.requestTransmission);
        setFieldValue('modalCarHorsepower', data.requestHorsepower);
        setFieldValue('modalCarAirbagCount', data.requestAirbagCount);
        setFieldValue('modalCarFuelConsumption', data.requestFuelConsumption);
        setFieldValue('modalCarMaxSpeed', data.requestMaxSpeedKmh);
        setAdminAction(data.requestId);

        if (fileStatus) {
            fileStatus.textContent = imageUrls.length > 0
                    ? imageUrls.length + ' imagen' + (imageUrls.length === 1 ? '' : 'es') + ' cargada' + (imageUrls.length === 1 ? '' : 's') + ' en la solicitud #' + data.requestId
                    : 'Sin imágenes cargadas';
        }
        if (fileUpload) {
            fileUpload.classList.toggle('has-file', imageUrls.length > 0);
        }
        setPreviewImages(imageUrls, false);
    };

    var populateCarForm = function (trigger) {
        if (!isAdminMode || !trigger) {
            return;
        }

        var data = trigger.dataset;
        setFieldValue('modalCarSubmitterEmail', '');
        setSubmitterEmailVisibility(false);
        setFieldValue('modalCarBrand', data.carBrand);
        setFieldValue('modalCarBodyType', data.carBodyType);
        setFieldValue('modalCarModel', data.carModel);
        setFieldValue('modalCarDescription', data.carDescription);
        setRadioGroupValue('fuelType', data.carFuelType);
        setRadioGroupValue('transmission', data.carTransmission);
        setFieldValue('modalCarHorsepower', data.carHorsepower);
        setFieldValue('modalCarAirbagCount', data.carAirbagCount);
        setFieldValue('modalCarFuelConsumption', data.carFuelConsumption);
        setFieldValue('modalCarMaxSpeed', data.carMaxSpeedKmh);
        setCarEditAction(data.carAction);

        if (fileStatus) {
            fileStatus.textContent = data.carImageUrl
                    ? 'Imagen actual del auto #' + data.carId
                    : 'Sin imagen cargada';
        }
        if (fileUpload) {
            fileUpload.classList.toggle('has-file', !!data.carImageUrl);
        }
        setPreviewImages(data.carImageUrl ? [data.carImageUrl] : [], false);
    };

    var closeModal = function () {
        if (isPageMode) {
            return;
        }
        modal.setAttribute('hidden', 'hidden');
        document.body.classList.remove('modal-open');
        resetModalState();
        if (lastTrigger && document.contains(lastTrigger)) {
            lastTrigger.focus();
        }
        lastTrigger = null;
    };

    var openModal = function (trigger) {
        var triggerMode = trigger ? trigger.getAttribute('data-open-create-car-modal') : '';
        lastTrigger = trigger || lastTrigger;
        if (isAdminMode && (!trigger || triggerMode === 'create')) {
            setCreateMode();
            resetModalState();
        } else if (isAdminMode && triggerMode === 'edit-car') {
            setEditCarMode();
            resetModalState();
            populateCarForm(trigger);
        } else if (isAdminMode) {
            setReviewMode();
            resetModalState();
            populateAdminForm(trigger);
        } else {
            currentMode = 'create';
            resetModalState();
        }
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
        var firstInput = modal.querySelector('#modalCarBrand');
        if (firstInput) {
            firstInput.focus();
        }
    };

    document.addEventListener('click', function (event) {
        var trigger = event.target.closest('[data-open-create-car-modal]');
        if (!trigger || trigger.getAttribute('data-open-create-car-modal') === 'false') {
            return;
        }

        event.preventDefault();
        openModal(trigger);
    });

    closeElements.forEach(function (element) {
        element.addEventListener('click', closeModal);
    });

    if (fileInput) {
        fileInput.addEventListener('change', function () {
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

    if (filePreviewThumbnails) {
        filePreviewThumbnails.addEventListener('click', function (event) {
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

    document.addEventListener('keydown', function (event) {
        if (!isPageMode && event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            closeModal();
        }
    });

    form.addEventListener('submit', function (event) {
        if (!validateFields()) {
            event.preventDefault();
            focusFirstInvalid();
        }
    });

    if (modal.dataset.autoOpen === 'true') {
        openModal(null);
    } else {
        updateFileState();
    }
})();
