(function () {
    var followButton = document.querySelector('[data-follow-toggle]');

    if (!followButton) {
        return;
    }

    followButton.addEventListener('click', function () {
        var isFollowing = followButton.getAttribute('data-following') === 'true';
        var nextState = !isFollowing;

        followButton.setAttribute('data-following', String(nextState));
        followButton.setAttribute('aria-pressed', String(nextState));
        followButton.classList.toggle('is-following', nextState);
        followButton.textContent = nextState ? 'Seguido' : 'Seguir';
    });
}());
