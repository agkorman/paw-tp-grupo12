<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="adminBaseUrl" value="/admin"/>

<div id="adminRequestReviewModal"
     class="review-modal admin-request-review-modal"
     hidden
     data-admin-base-url="${adminBaseUrl}">
    <div class="review-modal-overlay" data-close-admin-request-review-modal></div>
    <section class="review-modal-dialog admin-request-review-dialog"
             role="dialog" aria-modal="true" aria-labelledby="adminRequestReviewTitle">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker">Postulación</span>
                <h2 id="adminRequestReviewTitle">Revisar postulación</h2>
            </div>
            <button type="button" class="review-modal-close"
                    data-close-admin-request-review-modal aria-label="Cerrar modal">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="car-modal-subtitle">
            Postulación de <strong id="adminRequestReviewSubmitter"></strong>.
        </p>

        <div class="admin-request-review-fields">
            <div class="review-modal-field review-modal-field-wide">
                <label for="adminRequestReviewMotivation">¿Por qué querés ser moderador?</label>
                <textarea id="adminRequestReviewMotivation" rows="4" readonly></textarea>
            </div>
            <div class="review-modal-field review-modal-field-wide">
                <label for="adminRequestReviewBio">Contanos sobre vos</label>
                <textarea id="adminRequestReviewBio" rows="4" readonly></textarea>
            </div>
            <div class="review-modal-field review-modal-field-wide">
                <label for="adminRequestReviewJustification">¿Por qué te deberíamos aceptar?</label>
                <textarea id="adminRequestReviewJustification" rows="4" readonly></textarea>
            </div>
        </div>

        <div class="review-modal-actions">
            <form id="adminRequestRejectForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="btn-secondary admin-reject-btn">Rechazar</button>
            </form>
            <form id="adminRequestAcceptForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="btn-primary">Aceptar</button>
            </form>
        </div>
    </section>
</div>
