<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="messageCode" required="true" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div class="submitted-toast" id="submittedToast" role="status" aria-live="polite">
    <pa:icon name="check-circle" size="20" cssClass="submitted-toast-icon"/>
    <span class="submitted-toast-text"><spring:message code="${messageCode}"/></span>
    <button type="button" class="submitted-toast-action" data-dismiss-submitted-toast>
        <spring:message code="cars.submittedToast.dismiss"/>
    </button>
</div>
