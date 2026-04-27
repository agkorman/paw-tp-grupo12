<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<div id="deleteReviewModal" class="profile-modal" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="deleteReviewTitle">
        <div class="profile-modal-header">
            <h2 id="deleteReviewTitle">Eliminar review</h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="Cerrar modal">
                <pa:icon name="close" size="18"/>
            </button>
        </div>

        <form id="deleteReviewForm" class="profile-delete-form" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <p>Vas a eliminar esta review de forma permanente.</p>
            <p class="profile-delete-review-title" data-delete-review-title></p>

            <div class="profile-modal-actions">
                <button type="button" class="btn-secondary" data-close-profile-modal>Cancelar</button>
                <button type="submit" class="btn-primary profile-delete-confirm-button">Eliminar</button>
            </div>
        </form>
    </section>
</div>
