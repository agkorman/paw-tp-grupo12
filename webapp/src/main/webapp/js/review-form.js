(function () {
    function $(id) { return document.getElementById(id); }
    function $$(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }

    var form = $('createReviewForm');
    var modal = $('createReviewFormPage');
    if (!modal || !form) {
        return;
    }

    var STAR_FILLED = '#ff5719';
    var STAR_EMPTY = '#2e2e2e';
    var starSlots = $$('.star-slot', modal);
    var starInput = $('modalRating');
    var starWrap = modal.querySelector('.star-rating');
    var starLabel = modal.querySelector('.star-rating-value');
    var currentRating = 0;

    function starTextFor(value) {
        if (value === 0) return 'Sin puntuacion';
        if (value <= 1) return 'Malo';
        if (value <= 2) return 'Regular';
        if (value <= 3) return 'Bueno';
        if (value <= 4) return 'Muy bueno';
        return 'Excelente';
    }

    function paintStar(slot, starNum, rating) {
        var gradient = slot.querySelector('linearGradient');
        if (!gradient) {
            return;
        }
        var stops = gradient.querySelectorAll('stop');
        if (stops.length < 2) {
            return;
        }
        if (rating >= starNum) {
            stops[0].setAttribute('stop-color', STAR_FILLED);
            stops[0].setAttribute('offset', '100%');
            stops[1].setAttribute('stop-color', STAR_FILLED);
            stops[1].setAttribute('offset', '100%');
        } else if (rating >= starNum - 0.5) {
            stops[0].setAttribute('stop-color', STAR_FILLED);
            stops[0].setAttribute('offset', '50%');
            stops[1].setAttribute('stop-color', STAR_EMPTY);
            stops[1].setAttribute('offset', '50%');
        } else {
            stops[0].setAttribute('stop-color', STAR_EMPTY);
            stops[0].setAttribute('offset', '0%');
            stops[1].setAttribute('stop-color', STAR_EMPTY);
            stops[1].setAttribute('offset', '100%');
        }
    }

    function renderStars(rating) {
        starSlots.forEach(function (slot, index) {
            paintStar(slot, index + 1, rating);
        });
    }

    function setRating(value) {
        currentRating = value;
        if (starInput) {
            starInput.value = value;
        }
        if (starLabel) {
            starLabel.textContent = value + '/5 - ' + starTextFor(value);
            starLabel.style.color = '';
        }
        if (starWrap) {
            starWrap.setAttribute('aria-valuenow', value);
        }
        renderStars(value);
    }

    function resetRating() {
        currentRating = 0;
        if (starInput) {
            starInput.value = '';
        }
        if (starLabel) {
            starLabel.textContent = starTextFor(0);
            starLabel.style.color = '';
        }
        if (starWrap) {
            starWrap.setAttribute('aria-valuenow', 0);
        }
        renderStars(0);
    }

    function syncRatingFromInput() {
        var raw = starInput ? starInput.value : '';
        var value = raw ? Number(raw) : 0;
        if (value > 0 && value <= 5) {
            setRating(value);
        } else {
            resetRating();
        }
    }

    $$('.star-hit', modal).forEach(function (button) {
        button.addEventListener('click', function () {
            var star = parseInt(button.getAttribute('data-star'), 10);
            var isHalf = button.getAttribute('data-half') === 'true';
            var value = isHalf ? star - 0.5 : star;
            if (value === currentRating) {
                resetRating();
            } else {
                setRating(value);
            }
        });
    });

    if (starWrap) {
        starWrap.addEventListener('keydown', function (event) {
            if (event.key === 'ArrowRight' || event.key === 'ArrowUp') {
                event.preventDefault();
                setRating(Math.min(5, currentRating + 0.5));
            } else if (event.key === 'ArrowLeft' || event.key === 'ArrowDown') {
                event.preventDefault();
                if (currentRating <= 0.5) {
                    resetRating();
                } else {
                    setRating(currentRating - 0.5);
                }
            }
        });
    }

    form.noValidate = true;
    syncRatingFromInput();
}());
