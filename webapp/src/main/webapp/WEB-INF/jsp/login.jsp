<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="loginSubmittingLabel" code="auth.login.submitting"/>
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

            <c:if test="${not empty loginErrorCode}">
                <div class="alert alert-error" role="alert"><spring:message code="${loginErrorCode}"/></div>
            </c:if>
            <c:if test="${not empty loginMessageCode}">
                <div class="alert alert-success" role="status"><spring:message code="${loginMessageCode}"/></div>
            </c:if>

            <form id="loginForm" class="auth-form" method="post" action="<c:url value='/login'/>"
                  data-auth-form="login" data-submit-lock="true" novalidate="novalidate">
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

    <pa:script src="/js/auth-form.js"/>
    <pa:script src="/js/form-submit-lock.js"/>
    <pa:footer/>
</body>
</html>
