<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="messageCode" required="false" %>
<%@ attribute name="type" required="false" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<spring:message var="toastCloseLabel" code="toast.close"/>
<c:if test="${not empty messageCode}">
    <spring:message var="initialToastMessage" code="${messageCode}"/>
</c:if>
<div id="globalToast"
     class="global-toast"
     role="status"
     aria-live="polite"
     aria-atomic="true"
     data-toast-initial-message="${not empty initialToastMessage ? fn:escapeXml(initialToastMessage) : ''}"
     data-toast-initial-type="${not empty initialToastMessage ? fn:escapeXml(empty type ? 'success' : type) : ''}"
    data-toast-initial-timeout="${not empty initialToastMessage ? '6000' : ''}"
     hidden>
    <span class="global-toast-icon" aria-hidden="true">
        <span data-toast-icon-success><pa:icon name="check-circle" size="20"/></span>
        <span data-toast-icon-error><pa:icon name="error-circle" size="20"/></span>
    </span>
    <span class="global-toast-message" data-toast-message></span>
    <button type="button" class="global-toast-close" data-toast-close aria-label="${fn:escapeXml(toastCloseLabel)}">
        <pa:icon name="close" size="16"/>
    </button>
</div>
