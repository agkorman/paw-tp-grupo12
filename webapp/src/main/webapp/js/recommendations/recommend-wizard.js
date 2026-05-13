(function () {
    'use strict';

    const form = document.getElementById('recommend-wizard');
    if (!form) {
        return;
    }

    const steps = Array.from(form.querySelectorAll('.wizard-step'));
    const progressBar = form.querySelector('.wizard-progress-bar');
    const progressLabel = form.querySelector('.wizard-progress-label');
    const fallbackActions = form.querySelector('.wizard-fallback-actions');
    const progressShell = form.querySelector('.wizard-progress');
    const questionIndexTemplate = progressShell
        ? (progressShell.getAttribute('data-recommend-question-index-template') || '')
        : '';
    const requiredAnswerMessage = form.getAttribute('data-msg-required-answer') || '';
    const QUESTION_INDEX_CUR = '__PAW_0__';
    const QUESTION_INDEX_TOTAL = '__PAW_1__';

    if (steps.length === 0) {
        return;
    }

    form.classList.add('wizard-form--js');
    if (fallbackActions) {
        fallbackActions.hidden = true;
    }

    const totalQuestions = steps.filter(step => step.dataset.stepType === 'question').length;
    let currentIndex = 0;
    let advanceTimer = null;

    function clampIndex(index) {
        if (index < 0) return 0;
        if (index > steps.length - 1) return steps.length - 1;
        return index;
    }

    function updateProgress() {
        const step = steps[currentIndex];
        const type = step.dataset.stepType;
        let answeredQuestions = 0;
        for (let i = 0; i < currentIndex; i++) {
            if (steps[i].dataset.stepType === 'question') {
                answeredQuestions++;
            }
        }
        const totalSlots = totalQuestions + 1;
        let filled;
        let labelText;
        if (type === 'intro') {
            filled = 0;
            labelText = '';
        } else if (questionIndexTemplate.indexOf(QUESTION_INDEX_CUR) !== -1
            && questionIndexTemplate.indexOf(QUESTION_INDEX_TOTAL) !== -1) {
            if (type === 'question') {
                filled = answeredQuestions + 1;
            } else {
                filled = totalSlots;
            }
            labelText = questionIndexTemplate.split(QUESTION_INDEX_CUR).join(String(filled))
                .split(QUESTION_INDEX_TOTAL).join(String(totalSlots));
        } else if (type === 'question') {
            filled = answeredQuestions + 1;
            labelText = '';
        } else {
            filled = totalSlots;
            labelText = '';
        }
        const ratio = filled / totalSlots;
        if (progressBar) {
            progressBar.style.transform = 'scaleX(' + ratio.toFixed(3) + ')';
        }
        if (progressLabel) {
            progressLabel.textContent = labelText;
        }
    }

    function focusActiveStep(step) {
        const target = step.querySelector('.wizard-answer input[type="radio"]:checked')
            || step.querySelector('.wizard-answer input[type="radio"]')
            || step.querySelector('button, select, [tabindex]');
        if (target && typeof target.focus === 'function') {
            window.requestAnimationFrame(() => {
                target.focus({ preventScroll: true });
            });
        }
    }

    function showStep(index) {
        if (advanceTimer) {
            clearTimeout(advanceTimer);
            advanceTimer = null;
        }
        currentIndex = clampIndex(index);
        steps.forEach((step, i) => {
            const active = i === currentIndex;
            step.classList.toggle('wizard-step--active', active);
            step.toggleAttribute('hidden', !active);
        });
        updateProgress();
        focusActiveStep(steps[currentIndex]);
    }

    function goNext() {
        if (!validateCurrentQuestion(true)) {
            return;
        }
        if (currentIndex < steps.length - 1) {
            showStep(currentIndex + 1);
        }
    }

    function goPrev() {
        if (currentIndex > 0) {
            showStep(currentIndex - 1);
        }
    }

    function questionRadios(step) {
        return Array.from(step.querySelectorAll('input[type="radio"]'));
    }

    function questionError(step) {
        let error = step.querySelector('[data-recommend-client-error]');
        const body = step.querySelector('.wizard-step-body') || step;
        if (!error) {
            error = document.createElement('p');
            error.className = 'recommend-field-error client-form-error';
            error.setAttribute('data-recommend-client-error', 'true');
            error.setAttribute('role', 'alert');
            error.hidden = true;
            body.appendChild(error);
        }
        if (!error.id) {
            error.id = (step.getAttribute('data-question-id') || 'recommend-question') + 'ClientError';
        }
        return error;
    }

    function setRadioDescribedBy(radio, errorId) {
        const ids = (radio.getAttribute('aria-describedby') || '').split(/\s+/).filter(Boolean);
        if (ids.indexOf(errorId) === -1) {
            ids.push(errorId);
            radio.setAttribute('aria-describedby', ids.join(' '));
        }
    }

    function removeRadioDescribedBy(radio, errorId) {
        const ids = (radio.getAttribute('aria-describedby') || '').split(/\s+/).filter(id => id && id !== errorId);
        if (ids.length) {
            radio.setAttribute('aria-describedby', ids.join(' '));
        } else {
            radio.removeAttribute('aria-describedby');
        }
    }

    function clearQuestionError(step) {
        const error = step.querySelector('[data-recommend-client-error]');
        const errorId = error ? error.id : '';
        questionRadios(step).forEach(radio => {
            radio.removeAttribute('aria-invalid');
            if (errorId) {
                removeRadioDescribedBy(radio, errorId);
            }
        });
        if (error) {
            error.textContent = '';
            error.hidden = true;
        }
    }

    function validateQuestionStep(step, shouldFocus) {
        if (!step || step.dataset.stepType !== 'question') {
            return true;
        }
        const radios = questionRadios(step);
        if (radios.length === 0 || radios.some(radio => radio.checked)) {
            clearQuestionError(step);
            return true;
        }

        const error = questionError(step);
        error.textContent = requiredAnswerMessage;
        error.hidden = false;
        radios.forEach(radio => {
            radio.setAttribute('aria-invalid', 'true');
            setRadioDescribedBy(radio, error.id);
        });
        if (shouldFocus && radios[0] && typeof radios[0].focus === 'function') {
            radios[0].focus({ preventScroll: true });
        }
        return false;
    }

    function validateCurrentQuestion(shouldFocus) {
        return validateQuestionStep(steps[currentIndex], shouldFocus);
    }

    form.addEventListener('click', event => {
        const trigger = event.target.closest('[data-wizard-action]');
        if (!trigger || !form.contains(trigger)) {
            return;
        }
        event.preventDefault();
        const action = trigger.dataset.wizardAction;
        if (action === 'next') {
            goNext();
        } else if (action === 'prev') {
            goPrev();
        }
    });

    form.addEventListener('change', event => {
        const target = event.target;
        if (!(target instanceof HTMLInputElement) || target.type !== 'radio') {
            return;
        }
        const step = target.closest('.wizard-step');
        if (!step || step !== steps[currentIndex] || step.dataset.stepType !== 'question') {
            return;
        }
        if (advanceTimer) {
            clearTimeout(advanceTimer);
        }
        clearQuestionError(step);
        advanceTimer = window.setTimeout(() => {
            advanceTimer = null;
            goNext();
        }, 280);
    });

    form.addEventListener('keydown', event => {
        if (event.key !== 'Enter') {
            return;
        }
        const activeStep = steps[currentIndex];
        if (!activeStep) {
            return;
        }
        if (activeStep.dataset.stepType === 'filters') {
            return;
        }
        const target = event.target;
        if (target && target.tagName === 'BUTTON' && target.type === 'submit') {
            return;
        }
        event.preventDefault();
        goNext();
    });

    form.addEventListener('submit', event => {
        const allQuestionsValid = steps
            .filter(step => step.dataset.stepType === 'question')
            .reduce((valid, step) => validateQuestionStep(step, false) && valid, true);
        if (!allQuestionsValid) {
            event.preventDefault();
            const firstInvalid = steps.find(step => step.dataset.stepType === 'question'
                && questionRadios(step).some(radio => radio.getAttribute('aria-invalid') === 'true'));
            if (firstInvalid) {
                showStep(steps.indexOf(firstInvalid));
                validateQuestionStep(firstInvalid, true);
            }
        }
    });

    showStep(0);
})();
