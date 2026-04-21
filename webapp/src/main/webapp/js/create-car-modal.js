(function () {
    var modal = document.getElementById('createCarModal');
    var form = document.getElementById('createCarForm');
    var fileInput = document.getElementById('modalCarFile');
    var fileStatus = document.getElementById('modalCarFileStatus');
    var filePreview = document.getElementById('modalCarImagePreview');
    var filePreviewImg = document.getElementById('modalCarImagePreviewImg');
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

    var closeElements = Array.prototype.slice.call(modal.querySelectorAll('[data-close-car-modal]'));
    var emptyFileStatus = 'Ninguna imagen seleccionada';
    var previewUrl = null;
    var lastTrigger = null;
    var requiredMessages = {
        modalCarBrand: 'Seleccioná una marca.',
        modalCarBodyType: 'Seleccioná un tipo de carrocería.',
        modalCarModel: 'Ingresá el modelo.',
        modalCarDescription: 'Ingresá una descripción.',
        modalCarFile: 'Seleccioná una imagen del auto.'
    };

    // Must mirror validateUploadedImage in CarController.java.
    var EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    var ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
    var MAX_IMAGE_BYTES = 10 * 1024 * 1024;

    var isMissingRequiredValue = function (field) {
        if (field.type === 'file') {
            return !field.files || field.files.length === 0;
        }
        return !field.value || field.value.trim() === '';
    };

    var validateField = function (field) {
        field.setCustomValidity('');

        if (field.required && isMissingRequiredValue(field)) {
            field.setCustomValidity(requiredMessages[field.id] || 'Completá este campo.');
            return field.checkValidity();
        }

        if (field.type === 'email' && field.value && !EMAIL_PATTERN.test(field.value.trim())) {
            field.setCustomValidity('Ingresá un email válido.');
            return field.checkValidity();
        }

        if (field.type === 'file' && field.files && field.files.length > 0) {
            var f = field.files[0];
            if (!f.type || ALLOWED_IMAGE_TYPES.indexOf(f.type) === -1) {
                field.setCustomValidity('Tipo de imagen no soportado. Usá JPEG, PNG o WEBP.');
                return field.checkValidity();
            }
            if (f.size > MAX_IMAGE_BYTES) {
                field.setCustomValidity('La imagen no debe superar los 10 MB.');
                return field.checkValidity();
            }
        }

        return field.checkValidity();
    };

    var validateRequiredFields = function () {
        return Array.prototype.slice.call(form.querySelectorAll('[required]')).reduce(function (isValid, field) {
            return validateField(field) && isValid;
        }, true);
    };

    var setPreviewImage = function (imageUrl, shouldRevoke) {
        if (previewUrl) {
            window.URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }

        if (!filePreview || !filePreviewImg || !fileUpload) {
            return;
        }

        if (!imageUrl) {
            filePreviewImg.removeAttribute('src');
            filePreview.setAttribute('hidden', 'hidden');
            filePreview.setAttribute('aria-hidden', 'true');
            fileUpload.classList.remove('has-preview');
            return;
        }

        filePreviewImg.src = imageUrl;
        filePreview.removeAttribute('hidden');
        filePreview.setAttribute('aria-hidden', 'false');
        fileUpload.classList.add('has-preview');

        if (shouldRevoke) {
            previewUrl = imageUrl;
        }
    };

    var clearPreviewImage = function () {
        setPreviewImage(null, false);
    };

    var updateFileState = function () {
        if (!fileInput || !fileStatus || !fileUpload) {
            return;
        }

        var selectedFile = fileInput.files && fileInput.files.length > 0 ? fileInput.files[0] : null;
        fileUpload.classList.toggle('has-file', !!selectedFile);

        if (selectedFile) {
            fileStatus.textContent = selectedFile.name;
            if (window.URL && typeof window.URL.createObjectURL === 'function') {
                setPreviewImage(window.URL.createObjectURL(selectedFile), true);
            } else {
                clearPreviewImage();
            }
        } else {
            fileStatus.textContent = emptyFileStatus;
            clearPreviewImage();
        }

        validateField(fileInput);
    };

    var resetModalState = function () {
        form.reset();
        if (fileStatus) {
            fileStatus.textContent = emptyFileStatus;
        }
        clearPreviewImage();
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
        setFieldDisabled('modalCarBrand', false);
        setFieldDisabled('modalCarBodyType', false);
        setFieldDisabled('modalCarFile', false);
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
        clearPreviewImage();
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
        setFieldDisabled('modalCarBrand', false);
        setFieldDisabled('modalCarBodyType', false);
        setFieldDisabled('modalCarFile', false);
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
        setFieldDisabled('modalCarBrand', false);
        setFieldDisabled('modalCarBodyType', false);
        setFieldDisabled('modalCarFile', false);
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

    var populateAdminForm = function (trigger) {
        if (!isAdminMode || !trigger) {
            return;
        }

        var data = trigger.dataset;
        var submitterEmail = hasSubmitterEmail(data.requestSubmitter) ? data.requestSubmitter : '';
        setFieldValue('modalCarSubmitterEmail', submitterEmail);
        setSubmitterEmailVisibility(!!submitterEmail);
        setFieldValue('modalCarBrand', data.requestBrand);
        setFieldValue('modalCarBodyType', data.requestBodyType);
        setFieldValue('modalCarModel', data.requestModel);
        setFieldValue('modalCarDescription', data.requestDescription);
        setAdminAction(data.requestId);

        if (fileStatus) {
            fileStatus.textContent = data.requestImageUrl
                    ? 'Imagen cargada en la solicitud #' + data.requestId
                    : 'Sin imagen cargada';
        }
        if (fileUpload) {
            fileUpload.classList.toggle('has-file', !!data.requestImageUrl);
        }
        setPreviewImage(data.requestImageUrl || null, false);
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
        setCarEditAction(data.carAction);

        if (fileStatus) {
            fileStatus.textContent = data.carImageUrl
                    ? 'Imagen actual del auto #' + data.carId
                    : 'Sin imagen cargada';
        }
        if (fileUpload) {
            fileUpload.classList.toggle('has-file', !!data.carImageUrl);
        }
        setPreviewImage(data.carImageUrl || null, false);
    };

    var closeModal = function () {
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
        fileInput.addEventListener('change', updateFileState);
    }

    Array.prototype.slice.call(form.querySelectorAll('[required]')).forEach(function (field) {
        var eventName = field.tagName === 'SELECT' || field.type === 'file' ? 'change' : 'input';
        field.addEventListener(eventName, function () {
            validateField(field);
        });
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hasAttribute('hidden')) {
            closeModal();
        }
    });

    form.addEventListener('submit', function (event) {
        if (!validateRequiredFields()) {
            event.preventDefault();
            form.reportValidity();
            return;
        }
        if (!form.reportValidity()) {
            event.preventDefault();
        }
    });

    if (modal.dataset.autoOpen === 'true') {
        openModal(null);
    } else {
        updateFileState();
    }
})();
