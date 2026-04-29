<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="requestAdminUrl" value="/admin-requests"/>

<div id="requestAdminModal" class="review-modal admin-request-submit-modal" hidden>
    <div class="review-modal-overlay" data-close-request-admin-modal></div>
    <section class="review-modal-dialog admin-request-submit-dialog"
             role="dialog" aria-modal="true" aria-labelledby="requestAdminModalTitle">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker">Postulación</span>
                <h2 id="requestAdminModalTitle">Postulate como moderador</h2>
            </div>
            <button type="button" class="review-modal-close"
                    data-close-request-admin-modal aria-label="Cerrar modal">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="car-modal-subtitle">
            Contanos por qué te interesa el rol. Un administrador va a revisar tu postulación.
        </p>

        <form id="requestAdminForm" class="admin-request-submit-form" method="post" action="${requestAdminUrl}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestAdminMotivation">¿Por qué querés ser moderador?</label>
                <textarea id="requestAdminMotivation" name="motivation" rows="5"
                          maxlength="2000" required
                          placeholder="Contanos qué te motiva a sumarte al equipo de moderación."></textarea>
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestAdminBio">Contanos sobre vos</label>
                <textarea id="requestAdminBio" name="bio" rows="5"
                          maxlength="2000" required
                          placeholder="A qué te dedicás, qué autos manejás, cuánto tiempo llevás en La Posta..."></textarea>
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestAdminJustification">¿Por qué te deberíamos aceptar?</label>
                <textarea id="requestAdminJustification" name="justification" rows="5"
                          maxlength="2000" required
                          placeholder="¿Qué aportás vos que otros no?"></textarea>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-request-admin-modal>Cancelar</button>
                <button type="submit" class="btn-primary">Enviar postulación</button>
            </div>
        </form>
    </section>
</div>
