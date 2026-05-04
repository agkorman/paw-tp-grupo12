<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="loginSubmittingLabel" code="auth.login.submitting"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="auth.login.title"/></title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/auth.css'/>">
</head>
<body>
    <pa:nav/>

    <main class="auth-page">
        <section class="auth-panel" aria-labelledby="loginTitle">
            <div class="auth-header">
                <span class="auth-kicker"><spring:message code="auth.login.kicker"/></span>
                <h1 id="loginTitle"><spring:message code="auth.login.heading"/></h1>
            </div>

            <form id="loginForm" class="auth-form" method="post" action="<c:url value='/login'/>"
                  data-submit-lock="true" novalidate="novalidate">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <c:if test="${not empty loginRedirect}">
                    <input type="hidden" name="redirect" value="<c:out value='${loginRedirect}'/>">
                </c:if>
                <c:if test="${not empty loginIntent}">
                    <input type="hidden" name="intent" value="<c:out value='${loginIntent}'/>">
                </c:if>

                <div class="auth-field">
                    <label for="loginEmail"><spring:message code="common.form.email"/></label>
                    <input id="loginEmail" name="email" type="email" autocomplete="email">
                </div>

                <div class="auth-field">
                    <label for="loginPassword"><spring:message code="common.form.password"/></label>
                    <input id="loginPassword" name="password" type="password" autocomplete="current-password">
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
    </c:choose>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
    <script src="<c:url value='/js/toast.js'/>"></script>
    <pa:footer/>
</body>
</html>
