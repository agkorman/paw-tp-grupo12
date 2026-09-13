<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="loginSubmittingLabel" code="auth.login.submitting"/>
<spring:message var="jsRequiredEmail" code="js.auth.required.email"/>
<spring:message var="jsRequiredPassword" code="js.auth.required.password"/>
<spring:message var="jsRequiredGeneric" code="js.form.required.generic"/>
<spring:message var="jsLengthMin" code="js.form.length.min"/>
<spring:message var="jsLengthMax" code="js.form.length.max"/>
<spring:message var="jsEmailInvalid" code="js.auth.email.invalid"/>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="auth.login.title" styles="/css/auth.css"/>
<body>
    <pa:nav/>

    <main class="auth-page">
        <section class="auth-panel" aria-labelledby="loginTitle">
            <div class="auth-header">
                <span class="auth-kicker"><spring:message code="auth.login.kicker"/></span>
                <h1 id="loginTitle"><spring:message code="auth.login.heading"/></h1>
            </div>

            <form id="loginForm" class="auth-form" method="post" action="<c:url value='/login'/>"
                  enctype="multipart/form-data" data-auth-form="login" data-submit-lock="true" novalidate="novalidate"
                  data-msg-required-generic="${fn:escapeXml(jsRequiredGeneric)}"
                  data-msg-required-login-email="${fn:escapeXml(jsRequiredEmail)}"
                  data-msg-required-login-password="${fn:escapeXml(jsRequiredPassword)}"
                  data-msg-length-min="${fn:escapeXml(jsLengthMin)}"
                  data-msg-length-max="${fn:escapeXml(jsLengthMax)}"
                  data-msg-email-invalid="${fn:escapeXml(jsEmailInvalid)}">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <c:if test="${not empty loginRedirect}">
                    <input type="hidden" name="redirect" value="<c:out value='${loginRedirect}'/>">
                </c:if>
                <c:if test="${not empty loginIntent}">
                    <input type="hidden" name="intent" value="<c:out value='${loginIntent}'/>">
                </c:if>

                <div class="auth-field">
                    <label for="loginEmail"><spring:message code="common.form.email"/></label>
                    <input id="loginEmail" name="email" type="email" maxlength="100" autocomplete="email" required>
                </div>

                <div class="auth-field">
                    <label for="loginPassword"><spring:message code="common.form.password"/></label>
                    <input id="loginPassword" name="password" type="password" maxlength="72" autocomplete="current-password" required>
                </div>

                <label class="auth-check">
                    <input type="checkbox" name="remember-me">
                    <span><spring:message code="auth.login.remember"/></span>
                </label>

                <button type="submit" class="btn-primary auth-submit" data-loading-label="${loginSubmittingLabel}">
                    <spring:message code="auth.login.submit"/>
                </button>
            </form>

            <p class="auth-switch">
                <spring:message code="auth.login.noAccount"/>
                <a href="<c:url value='/register'/>"><spring:message code="auth.login.registerLink"/></a>
            </p>
        </section>
    </main>

    <c:choose>
        <c:when test="${not empty loginErrorCode}">
            <pa:toast messageCode="${loginErrorCode}" type="error"/>
        </c:when>
        <c:when test="${not empty loginMessageCode}">
            <pa:toast messageCode="${loginMessageCode}"/>
        </c:when>
        <c:otherwise>
            <pa:toast/>
        </c:otherwise>
    </c:choose>
    <pa:script src="/js/auth/auth-form.js"/>
    <pa:script src="/js/shared/toast.js"/>
    <pa:footer/>
</body>
</html>
