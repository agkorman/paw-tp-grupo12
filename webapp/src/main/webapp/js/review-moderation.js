(function () {
    'use strict';

    var modal = document.querySelector('[data-hide-review-modal]');
    var form = document.getElementById('hideReviewForm');
    var reasonField = document.getElementById('hideReviewReason');
    var errorNode = modal ? modal.querySelector('[data-hide-review-error]') : null;
    var activeReviewId = null;

    if (!modal || !form || !reasonField) {
        return;
    }

    function setModalOpen(open) {
        modal.hidden = !open;
        document.body.classList.toggle('review-modal-open', open);
        if (open) {
            reasonField.focus();
        }
    }

    function clearError() {
        reasonField.classList.remove('is-invalid');
        if (errorNode) {
            errorNode.textContent = '';
            errorNode.hidden = true;
        }
    }

    function showError(message) {
        reasonField.classList.add('is-invalid');
        if (errorNode) {
            errorNode.textContent = message || modal.dataset.errorMessage || '';
            errorNode.hidden = false;
        }
    }

    function openModal(button) {
        activeReviewId = button.getAttribute('data-review-id');
        form.action = button.getAttribute('data-review-hide-action');
        reasonField.value = '';
        clearError();
        setModalOpen(true);
    }

    function closeModal() {
        setModalOpen(false);
        form.removeAttribute('action');
        activeReviewId = null;
        clearError();
    }

    function showToast(message, type) {
        if (window.PawToast && typeof window.PawToast.show === 'function') {
            window.PawToast.show(message, type);
        }
    }

    function removeReviewCard() {
        if (!activeReviewId) {
            return;
        }
        var reviewCard = document.getElementById('review-' + activeReviewId);
        if (reviewCard) {
            reviewCard.remove();
        }
        var latestReview = document.querySelector('[data-latest-review-id="' + activeReviewId + '"]');
        if (latestReview) {
            var latestSection = latestReview.closest('[data-latest-review-section]');
            var emptyMessage = latestSection ? latestSection.getAttribute('data-empty-message') : '';
            var emptyNode = document.createElement('div');
            emptyNode.className = 'last-review-empty';
            emptyNode.textContent = emptyMessage || '';
            latestReview.replaceWith(emptyNode);
        }
        var countLabel = document.querySelector('.review-count-label');
        if (countLabel) {
            var value = parseInt(countLabel.textContent, 10);
            if (!Number.isNaN(value) && value > 0) {
                countLabel.textContent = String(value - 1);
            }
        }
    }

    document.addEventListener('click', function (event) {
        var openButton = event.target.closest('[data-open-hide-review-modal]');
        if (openButton) {
            event.preventDefault();
            openModal(openButton);
            return;
        }

        if (event.target.closest('[data-close-hide-review-modal]')) {
            event.preventDefault();
            closeModal();
        }
    });

    reasonField.addEventListener('input', clearError);

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        clearError();

        var submitButton = form.querySelector('[type="submit"]');
        if (submitButton) {
            submitButton.disabled = true;
        }

        fetch(form.action, {
            method: 'POST',
            body: new FormData(form),
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'text/plain'
            },
            credentials: 'same-origin'
        }).then(function (response) {
            if (!response.ok) {
                return response.text().then(function (text) {
                    var error = new Error(response.status === 400 && text ? text : modal.dataset.errorMessage || '');
                    error.validation = response.status === 400;
                    throw error;
                });
            }
            removeReviewCard();
            closeModal();
            showToast(modal.dataset.successMessage, 'success');
        }).catch(function (error) {
            var message = error && error.message ? error.message : modal.dataset.errorMessage;
            if (error && error.validation && message) {
                showError(message);
            }
            showToast(modal.dataset.errorMessage, 'error');
        }).finally(function () {
            if (submitButton) {
                submitButton.disabled = false;
            }
        });
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) {
            closeModal();
        }
    });
})();
