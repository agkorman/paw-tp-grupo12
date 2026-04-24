<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="profile" required="true" type="ar.edu.itba.paw.webapp.controller.ProfileController.ProfileData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="editProfileModal" class="profile-modal" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-edit-dialog" role="dialog" aria-modal="true" aria-labelledby="editProfileTitle">
        <header class="profile-modal-header">
            <h2 id="editProfileTitle">Editar perfil</h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="Cerrar modal">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" aria-hidden="true">
                    <path d="M18 6 6 18M6 6l12 12"/>
                </svg>
            </button>
        </header>

        <form class="profile-edit-form" method="post" action="#">
            <div class="profile-edit-fields">
                <label class="profile-edit-field" for="profileNameInput">
                    <span>Nombre de usuario</span>
                    <input id="profileNameInput" name="displayName" type="text" value="${fn:escapeXml(profile.name)}" maxlength="80">
                </label>

                <div class="profile-edit-field">
                    <span>Email</span>
                    <div class="profile-readonly-value" aria-label="Email">
                        <c:out value="${profile.email}"/>
                    </div>
                </div>
            </div>

            <div class="profile-modal-actions">
                <button type="button" class="btn-secondary" data-close-profile-modal>Cancelar</button>
                <button type="button" class="btn-primary" data-close-profile-modal>Guardar cambios</button>
            </div>
        </form>

        <c:url var="logoutUrl" value="/logout"/>
        <form class="profile-logout-form" method="post" action="${logoutUrl}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="btn-secondary profile-logout-button">Cerrar Sesión</button>
        </form>
    </section>
</div>
