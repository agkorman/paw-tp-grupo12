<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="adminBaseUrl" value="/admin"/>

<div id="adminCatalogRequestModal"
     class="review-modal admin-catalog-request-review-modal"
     hidden
     data-admin-base-url="${adminBaseUrl}">
    <div class="review-modal-overlay" data-close-admin-catalog-request-modal></div>
    <section class="review-modal-dialog admin-catalog-request-review-dialog"
             role="dialog" aria-modal="true" aria-labelledby="adminCatalogRequestTitle">
        <header class="review-modal-header">
            <div>
                <span id="adminCatalogRequestKicker" class="review-modal-kicker">Solicitud</span>
                <h2 id="adminCatalogRequestTitle">Revisar solicitud</h2>
            </div>
            <button type="button" class="review-modal-close"
                    data-close-admin-catalog-request-modal aria-label="Cerrar modal">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <div class="admin-catalog-request-review-grid">
            <div class="review-modal-field">
                <label for="adminCatalogRequestName">Nombre</label>
                <input id="adminCatalogRequestName" type="text" readonly>
            </div>

            <div class="review-modal-field">
                <label for="adminCatalogRequestSubmitter">Enviado por</label>
                <input id="adminCatalogRequestSubmitter" type="text" readonly>
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="adminCatalogRequestComments">Comentarios del usuario</label>
                <textarea id="adminCatalogRequestComments" rows="4" readonly
                          placeholder="El usuario no dejó comentarios."></textarea>
            </div>
        </div>

        <div class="review-modal-actions">
            <form id="adminCatalogRejectForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="btn-secondary admin-reject-btn">Rechazar</button>
            </form>
            <form id="adminCatalogAcceptForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="name" id="adminCatalogAcceptName">
                <button type="submit" class="btn-primary">Aceptar</button>
            </form>
        </div>
    </section>
</div>
