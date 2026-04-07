(function () {
    var modal = document.getElementById('createCarModal');
    var form = document.getElementById('createCarForm');
    var fileInput = document.getElementById('modalCarFile');
    var fileName = document.getElementById('modalCarFileName');
    var fileUpload = fileInput ? fileInput.closest('.car-image-upload') : null;

    if (!modal || !form) {
        return;
    }

    var closeElements = Array.prototype.slice.call(modal.querySelectorAll('[data-close-car-modal]'));
    var emptyFileLabel = 'Ningún archivo seleccionado';
    var lastTrigger = null;
    var requiredMessages = {
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

        var selectedFile = fileInput.files && fileInput.files.length > 0 ? fileInput.files[0] : null;
        fileName.textContent = selectedFile ? selectedFile.name : emptyFileLabel;
        fileUpload.classList.toggle('has-file', !!selectedFile);
        validateField(fileInput);
    };

    var resetModalState = function () {
        form.reset();
        updateFileState();
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
        lastTrigger = trigger || lastTrigger;
        resetModalState();
        modal.removeAttribute('hidden');
        document.body.classList.add('modal-open');
        var firstInput = modal.querySelector('#modalCarBrand');
        if (firstInput) {
            firstInput.focus();
        }
    };

    document.addEventListener('click', function (event) {
        var trigger = event.target.closest('[data-open-create-car-modal]');
        if (!trigger) {
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
