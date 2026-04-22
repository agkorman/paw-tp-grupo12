<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="action-menu ${cssClass}" data-action-menu>
    <button
            type="button"
            class="action-menu-toggle"
            data-action-menu-toggle
            aria-haspopup="true"
            aria-expanded="false"
            aria-label="${empty label ? 'Abrir opciones' : label}">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="currentColor" aria-hidden="true" focusable="false">
            <circle cx="9" cy="4" r="1.6"/>
            <circle cx="9" cy="9" r="1.6"/>
            <circle cx="9" cy="14" r="1.6"/>
        </svg>
    </button>
    <div class="action-menu-panel" data-action-menu-panel hidden>
        <jsp:doBody/>
    </div>
</div>
