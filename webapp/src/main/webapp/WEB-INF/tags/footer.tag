<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<%-- Global double-submit guard. Included here so every page is covered.
     Must stay non-defer: it self-defers to DOMContentLoaded so it always
     registers its submit listener after parse-time validation handlers. --%>
<pa:script src="/js/shared/form-submit-lock.js"/>

<footer>
    <div class="footer-brand">
        <span class="footer-brand-name"><spring:message code="footer.brand"/></span>
        <span class="footer-brand-tagline"><spring:message code="footer.brand.tagline"/></span>
    </div>
    <span class="footer-center"><spring:message code="footer.center"/></span>
    <div class="footer-copy">
        <span><spring:message code="footer.copyright"/></span>
        <span><spring:message code="footer.rights"/></span>
    </div>
</footer>
