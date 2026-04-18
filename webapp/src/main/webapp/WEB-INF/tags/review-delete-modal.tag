<%@ tag language="java" pageEncoding="UTF-8" %>

<div id="deleteReviewModal" class="profile-modal" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="deleteReviewTitle">
        <div class="profile-modal-header">
            <h2 id="deleteReviewTitle">Eliminar review</h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="Cerrar modal">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false"><line x1="4" y1="4" x2="14" y2="14"/><line x1="14" y1="4" x2="4" y2="14"/></svg>
            </button>
        </div>

        <form id="deleteReviewForm" class="profile-delete-form" method="post">
            <p>Vas a eliminar esta review de forma permanente.</p>
            <p class="profile-delete-review-title" data-delete-review-title></p>

            <div class="profile-modal-actions">
                <button type="button" class="btn-secondary" data-close-profile-modal>Cancelar</button>
                <button type="submit" class="btn-primary profile-delete-confirm-button">Eliminar</button>
            </div>
        </form>
    </section>
</div>
