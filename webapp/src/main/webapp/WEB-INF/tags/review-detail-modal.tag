<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="reviewDetailEndpoint" value="/reviews/"/>

<div id="reviewDetailModal"
     class="review-detail-modal"
     hidden
     data-detail-endpoint="${reviewDetailEndpoint}"
     aria-hidden="true">
    <div class="review-detail-modal-overlay" data-close-review-detail-modal></div>
    <section class="review-detail-modal-dialog"
             role="dialog"
             aria-modal="true"
             aria-labelledby="reviewDetailModalTitle">
        <header class="review-detail-modal-topbar">
            <h2 id="reviewDetailModalTitle" class="visually-hidden">Detalle de reseña</h2>
            <button type="button"
                    class="review-detail-modal-close"
                    data-close-review-detail-modal
                    aria-label="Cerrar modal">
                <pa:icon name="close" size="18"/>
            </button>
        </header>
        <div class="review-detail-modal-body" data-review-detail-body aria-live="polite">
            <div class="review-detail-modal-loading" data-review-detail-loading>
                Cargando reseña...
            </div>
        </div>
    </section>
</div>
