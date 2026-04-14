(function () {
    var modal = document.getElementById('createCarModal');
    var form = document.getElementById('createCarForm');
    var fileInput = document.getElementById('modalCarFile');
    var fileName = document.getElementById('modalCarFileName');
    var fileUpload = fileInput ? fileInput.closest('.car-image-upload') : null;
    var isAdminMode = modal ? modal.dataset.adminMode === 'true' : false;
    var acceptForm = document.getElementById('acceptCarRequestForm');
    var rejectForm = document.getElementById('rejectCarRequestForm');
    var modalKicker = document.getElementById('createCarModalKicker');
    var modalTitle = document.getElementById('createCarModalTitle');
    var modalSubtitle = document.getElementById('createCarModalSubtitle');
    var createActions = document.getElementById('createCarCreateActions');
    var reviewActions = document.getElementById('createCarReviewActions');
    var currentMode = isAdminMode ? 'review' : 'create';

    if (!modal || !form) {
        return;
    }

    var closeElements = Array.prototype.slice.call(modal.querySelectorAll('[data-close-car-modal]'));
    var emptyFileLabel = 'Ningún archivo seleccionado';
    var lastTrigger = null;
    var requiredMessages = {
        modalCarSubmitterEmail: 'Ingresá tu email.',
        modalCarBrand: 'Seleccioná una marca.',
        modalCarBodyType: 'Seleccioná un tipo de carrocería.',
        modalCarModel: 'Ingresá el modelo.',
        modalCarDescription: 'Ingresá una descripción.',
        modalCarFile: 'Seleccioná una imagen del auto.'
    };

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
        } else if (field.validity.typeMismatch && field.type === 'email') {
            field.setCustomValidity('Ingresá un email válido.');
        }

        return field.checkValidity();
    };

    var validateRequiredFields = function () {
        return Array.prototype.slice.call(form.querySelectorAll('[required]')).reduce(function (isValid, field) {
            return validateField(field) && isValid;
        }, true);
    };

    var updateFileState = function () {
        if (!fileInput || !fileName || !fileUpload) {
            return;
        }

        if (currentMode === 'review') {
            fileUpload.classList.add('has-file');
            return;
        }

        var selectedFile = fileInput.files && fileInput.files.length > 0 ? fileInput.files[0] : null;
        fileName.textContent = selectedFile ? selectedFile.name : emptyFileLabel;
        fileUpload.classList.toggle('has-file', !!selectedFile);
        validateField(fileInput);
    };

    var resetModalState = function () {
        form.reset();
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
        if (acceptForm) {
            acceptForm.setAttribute('action', baseUrl + '/requests/' + requestId + '/accept');
        }
        if (rejectForm) {
            rejectForm.setAttribute('action', baseUrl + '/requests/' + requestId + '/reject');
        }
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

    var setActionVisibility = function (showCreateActions) {
        if (createActions) {
            createActions.toggleAttribute('hidden', !showCreateActions);
        }
        if (reviewActions) {
            reviewActions.toggleAttribute('hidden', showCreateActions);
        }
    };

    var setCreateMode = function () {
        currentMode = 'create';
        setText(modalKicker, 'Nuevo vehículo');
        setText(modalTitle, 'Agregá un auto');
        setText(modalSubtitle, 'Completá los datos del auto. Esta carga se registrará desde el panel de administración.');
        setActionVisibility(true);

        setFieldReadonly('modalCarSubmitterEmail', false);
        setFieldReadonly('modalCarModel', false);
        setFieldReadonly('modalCarDescription', false);
        setFieldDisabled('modalCarBrand', false);
        setFieldDisabled('modalCarBodyType', false);
        setFieldDisabled('modalCarFile', false);
        if (fileInput) {
            fileInput.setAttribute('required', 'required');
        }
        if (fileUpload) {
            fileUpload.classList.remove('is-readonly', 'has-file');
        }
        if (fileName) {
            fileName.textContent = emptyFileLabel;
        }
    };

    var setReviewMode = function () {
        currentMode = 'review';
        setText(modalKicker, 'Solicitud pendiente');
        setText(modalTitle, 'Revisar formulario');
        setText(modalSubtitle, 'Revisá los datos enviados por el usuario antes de aprobar o rechazar la solicitud.');
        setActionVisibility(false);

        setFieldReadonly('modalCarSubmitterEmail', true);
        setFieldReadonly('modalCarModel', true);
        setFieldReadonly('modalCarDescription', true);
        setFieldDisabled('modalCarBrand', true);
        setFieldDisabled('modalCarBodyType', true);
        setFieldDisabled('modalCarFile', true);
        if (fileInput) {
            fileInput.removeAttribute('required');
        }
        if (fileUpload) {
            fileUpload.classList.add('is-readonly');
        }
    };

    var populateAdminForm = function (trigger) {
        if (!isAdminMode || !trigger) {
            return;
        }

        var data = trigger.dataset;
        setFieldValue('modalCarSubmitterEmail', data.requestSubmitter);
        setFieldValue('modalCarBrand', data.requestBrand);
        setFieldValue('modalCarBodyType', data.requestBodyType);
        setFieldValue('modalCarModel', data.requestModel);
        setFieldValue('modalCarDescription', data.requestDescription);
        setAdminAction(data.requestId);

        if (fileName) {
            fileName.textContent = data.requestImageUrl
                    ? 'Imagen cargada en la solicitud #' + data.requestId
                    : 'Sin imagen cargada';
        }
        if (fileUpload) {
            fileUpload.classList.toggle('has-file', !!data.requestImageUrl);
        }
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
        if (isAdminMode && triggerMode === 'create') {
            setCreateMode();
            resetModalState();
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
        var firstInput = currentMode === 'review' ? modal.querySelector('.review-modal-close') : modal.querySelector('#modalCarSubmitterEmail');
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
        if (currentMode === 'review') {
            event.preventDefault();
            return;
        }
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
