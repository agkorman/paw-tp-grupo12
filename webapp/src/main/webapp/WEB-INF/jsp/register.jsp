<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="registerSubmittingLabel" code="auth.register.submitting"/>
<spring:message var="jsRequiredUsername" code="js.auth.required.username"/>
<spring:message var="jsRequiredEmail" code="js.auth.required.email"/>
<spring:message var="jsRequiredPassword" code="js.auth.required.password"/>
<spring:message var="jsRequiredConfirmPassword" code="js.auth.required.confirmPassword"/>
<spring:message var="jsRequiredGeneric" code="js.form.required.generic"/>
<spring:message var="jsLengthMin" code="js.form.length.min"/>
<spring:message var="jsLengthMax" code="js.form.length.max"/>
<spring:message var="jsEmailInvalid" code="js.auth.email.invalid"/>
<spring:message var="jsUsernamePattern" code="js.auth.username.pattern"/>
<spring:message var="jsPasswordMatch" code="js.auth.password.match"/>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="auth.register.title" styles="/css/auth.css"/>
<body>
    <pa:nav/>

    <main class="auth-page">
        <section class="auth-panel" aria-labelledby="registerTitle">
            <div class="auth-header">
                <span class="auth-kicker"><spring:message code="auth.register.kicker"/></span>
                <h1 id="registerTitle"><spring:message code="auth.register.heading"/></h1>
            </div>

            <form id="registerForm" class="auth-form" method="post" action="<c:url value='/register'/>"
                  data-auth-form="register" data-submit-lock="true" novalidate="novalidate"
                  data-msg-required-generic="${fn:escapeXml(jsRequiredGeneric)}"
                  data-msg-required-register-username="${fn:escapeXml(jsRequiredUsername)}"
                  data-msg-required-register-email="${fn:escapeXml(jsRequiredEmail)}"
                  data-msg-required-register-password="${fn:escapeXml(jsRequiredPassword)}"
                  data-msg-required-register-confirm-password="${fn:escapeXml(jsRequiredConfirmPassword)}"
                  data-msg-length-min="${fn:escapeXml(jsLengthMin)}"
                  data-msg-length-max="${fn:escapeXml(jsLengthMax)}"
                  data-msg-email-invalid="${fn:escapeXml(jsEmailInvalid)}"
                  data-msg-username-pattern="${fn:escapeXml(jsUsernamePattern)}"
                  data-msg-password-match="${fn:escapeXml(jsPasswordMatch)}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                <div class="auth-field">
                    <label for="registerUsername"><spring:message code="common.form.username"/></label>
                    <input
                            id="registerUsername"
                            name="username"
                            type="text"
                            maxlength="50"
                            value="<c:out value='${username}'/>"
                            autocomplete="username"
                            required>
                </div>

                <div class="auth-field">
                    <label for="registerEmail"><spring:message code="common.form.email"/></label>
                    <input
                            id="registerEmail"
                            name="email"
                            type="email"
                            maxlength="100"
                            value="<c:out value='${email}'/>"
                            autocomplete="email"
                            required>
                </div>

                <div class="auth-field">
                    <label for="registerPassword"><spring:message code="common.form.password"/></label>
                    <input
                            id="registerPassword"
                            name="password"
                            type="password"
                            minlength="8"
                            maxlength="72"
                            autocomplete="new-password"
                            required>
                </div>

                <div class="auth-field">
                    <label for="registerConfirmPassword"><spring:message code="auth.register.confirmPassword"/></label>
                    <input
                            id="registerConfirmPassword"
                            name="confirmPassword"
                            type="password"
                            minlength="8"
                            maxlength="72"
                            autocomplete="new-password"
                            required>
                </div>

                <button type="submit" class="btn-primary auth-submit" data-loading-label="${registerSubmittingLabel}">
                    <spring:message code="auth.register.submit"/>
                </button>
            </form>

            <p class="auth-switch">
                <spring:message code="auth.register.hasAccount"/>
                <a href="<c:url value='/login'/>"><spring:message code="auth.register.loginLink"/></a>
            </p>
        </section>
    </main>

    <pa:toast messageCode="${registrationErrorCode}" type="error"/>
    <pa:script src="/js/auth/auth-form.js"/>
    <pa:script src="/js/shared/toast.js"/>
    <pa:footer/>
</body>
</html>
