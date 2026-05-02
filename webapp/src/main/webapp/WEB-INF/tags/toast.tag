<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="toastCloseLabel" code="toast.close"/>
<div id="globalToast" class="global-toast" role="status" aria-live="polite" aria-atomic="true" hidden>
    <span class="global-toast-icon" aria-hidden="true">
        <pa:icon name="close" size="14"/>
    </span>
    <span class="global-toast-message" data-toast-message></span>
    <button type="button" class="global-toast-close" data-toast-close aria-label="${fn:escapeXml(toastCloseLabel)}">
        <pa:icon name="close" size="14"/>
    </button>
</div>
