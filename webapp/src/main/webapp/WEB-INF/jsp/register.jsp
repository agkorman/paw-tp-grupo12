<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="auth.register.title"/></title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/auth.css'/>">
</head>
<body>
    <pa:nav/>

    <main class="auth-page">
        <section class="auth-panel" aria-labelledby="registerTitle">
            <div class="auth-header">
                <span class="auth-kicker"><spring:message code="auth.register.kicker"/></span>
                <h1 id="registerTitle"><spring:message code="auth.register.heading"/></h1>
            </div>

            <c:if test="${not empty registrationError}">
                <div class="alert alert-error" role="alert"><c:out value="${registrationError}"/></div>
            </c:if>

            <form id="registerForm" class="auth-form" method="post" action="<c:url value='/register'/>"
                  data-auth-form="register" data-submit-lock="true" novalidate="novalidate">
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

                <button type="submit" class="btn-primary auth-submit"><spring:message code="auth.register.submit"/></button>
            </form>

            <p class="auth-switch">
                <spring:message code="auth.register.hasAccount"/>
                <a href="<c:url value='/login'/>"><spring:message code="auth.register.loginLink"/></a>
            </p>
        </section>
    </main>

    <script src="<c:url value='/js/auth-form.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
