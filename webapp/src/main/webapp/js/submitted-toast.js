(function () {
    var toast = document.getElementById('submittedToast');
    if (!toast) {
        return;
    }

    var dismissButton = toast.querySelector('[data-dismiss-submitted-toast]');

    if (dismissButton) {
        dismissButton.addEventListener('click', function () {
            toast.remove();
        });
    }

    window.setTimeout(function () {
        if (!toast.isConnected) {
            return;
        }
        toast.classList.add('submitted-toast--hiding');
        window.setTimeout(function () {
            if (toast.isConnected) {
                toast.remove();
            }
        }, 300);
    }, 6000);
})();
