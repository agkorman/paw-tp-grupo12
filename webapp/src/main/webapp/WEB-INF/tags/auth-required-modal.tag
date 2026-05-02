<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="loginUrl" value="/login"/>
<spring:message var="closeModalLabel" code="common.action.close"/>

<div id="authRequiredModal"
     class="auth-required-modal"
     hidden
     data-login-url="${loginUrl}"
     data-context-path="${pageContext.request.contextPath}">
    <div class="auth-required-modal-overlay" data-close-auth-required-modal></div>
    <section class="auth-required-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="authRequiredTitle">
        <header class="auth-required-modal-header">
            <div>
                <span class="auth-required-modal-kicker"><spring:message code="auth.required.title"/></span>
                <h2 id="authRequiredTitle"><spring:message code="auth.required.heading"/></h2>
            </div>
            <button type="button" class="auth-required-modal-close" data-close-auth-required-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="auth-required-modal-copy">
            <spring:message code="auth.required.prefix"/> <span data-auth-required-action><spring:message code="auth.required.defaultAction"/></span>.
        </p>

        <div class="auth-required-modal-actions">
            <button type="button" class="btn-secondary" data-close-auth-required-modal><spring:message code="common.action.cancel"/></button>
            <a class="btn-primary" href="${loginUrl}" data-auth-required-login><spring:message code="common.action.login"/></a>
        </div>
    </section>
</div>
