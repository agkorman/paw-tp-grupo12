(function () {
    var carousels = Array.prototype.slice.call(document.querySelectorAll('[data-car-image-carousel]'));

    var setActiveSlide = function (carousel, index) {
        var slides = Array.prototype.slice.call(carousel.querySelectorAll('[data-carousel-slide]'));
        var thumbs = Array.prototype.slice.call(carousel.querySelectorAll('[data-carousel-thumb]'));
        var count = carousel.querySelector('[data-carousel-count]');

        if (slides.length === 0) {
            return;
        }

        var nextIndex = (index + slides.length) % slides.length;
        slides.forEach(function (slide, slideIndex) {
            slide.toggleAttribute('hidden', slideIndex !== nextIndex);
        });
        thumbs.forEach(function (thumb, thumbIndex) {
            thumb.classList.toggle('is-active', thumbIndex === nextIndex);
        });
        if (count) {
            count.textContent = (nextIndex + 1) + ' / ' + slides.length;
        }
        carousel.dataset.activeSlide = String(nextIndex);
    };

    carousels.forEach(function (carousel) {
        var slides = carousel.querySelectorAll('[data-carousel-slide]');
        if (slides.length <= 1) {
            return;
        }

        carousel.dataset.activeSlide = '0';

        var previous = carousel.querySelector('[data-carousel-prev]');
        var next = carousel.querySelector('[data-carousel-next]');

        if (previous) {
            previous.addEventListener('click', function () {
                setActiveSlide(carousel, Number(carousel.dataset.activeSlide || 0) - 1);
            });
        }
        if (next) {
            next.addEventListener('click', function () {
                setActiveSlide(carousel, Number(carousel.dataset.activeSlide || 0) + 1);
            });
        }

        Array.prototype.slice.call(carousel.querySelectorAll('[data-carousel-thumb]')).forEach(function (thumb) {
            thumb.addEventListener('click', function () {
                setActiveSlide(carousel, Number(thumb.getAttribute('data-carousel-thumb') || 0));
            });
        });

        carousel.addEventListener('keydown', function (event) {
            if (event.key === 'ArrowLeft') {
                event.preventDefault();
                event.stopPropagation();
                setActiveSlide(carousel, Number(carousel.dataset.activeSlide || 0) - 1);
            } else if (event.key === 'ArrowRight') {
                event.preventDefault();
                event.stopPropagation();
                setActiveSlide(carousel, Number(carousel.dataset.activeSlide || 0) + 1);
            }
        });
    });
})();
