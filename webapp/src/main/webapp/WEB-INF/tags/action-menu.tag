<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<div class="action-menu ${cssClass}" data-action-menu>
    <button
            type="button"
            class="action-menu-toggle"
            data-action-menu-toggle
            aria-haspopup="true"
            aria-expanded="false"
            aria-label="${empty label ? 'Abrir opciones' : label}">
        <pa:icon name="more-vertical" size="18"/>
    </button>
    <div class="action-menu-panel" data-action-menu-panel hidden>
        <jsp:doBody/>
    </div>
</div>
