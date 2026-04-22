<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="loginUrl" value="/login"/>

<div id="authRequiredModal"
     class="auth-required-modal"
     hidden
     data-login-url="${loginUrl}"
     data-context-path="${pageContext.request.contextPath}">
    <div class="auth-required-modal-overlay" data-close-auth-required-modal></div>
    <section class="auth-required-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="authRequiredTitle">
        <header class="auth-required-modal-header">
            <div>
                <span class="auth-required-modal-kicker">Acceso requerido</span>
                <h2 id="authRequiredTitle">Necesitás iniciar sesión</h2>
            </div>
            <button type="button" class="auth-required-modal-close" data-close-auth-required-modal aria-label="Cerrar modal">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true" focusable="false">
                    <line x1="4" y1="4" x2="14" y2="14"/>
                    <line x1="14" y1="4" x2="4" y2="14"/>
                </svg>
            </button>
        </header>

        <p class="auth-required-modal-copy">
            Necesitás iniciar sesión para <span data-auth-required-action>hacer esta acción</span>.
        </p>

        <div class="auth-required-modal-actions">
            <button type="button" class="btn-secondary" data-close-auth-required-modal>Cancelar</button>
            <a class="btn-primary" href="${loginUrl}" data-auth-required-login>Iniciar sesión</a>
        </div>
    </section>
</div>
