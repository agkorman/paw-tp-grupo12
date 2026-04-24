<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ingresar | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/auth.css'/>">
</head>
<body>
    <pa:nav/>

    <main class="auth-page">
        <section class="auth-panel" aria-labelledby="loginTitle">
            <div class="auth-header">
                <span class="auth-kicker">Acceso</span>
                <h1 id="loginTitle">Ingresá a tu cuenta</h1>
            </div>

            <c:if test="${not empty loginError}">
                <div class="alert alert-error" role="alert"><c:out value="${loginError}"/></div>
            </c:if>
            <c:if test="${not empty loginMessage}">
                <div class="alert alert-success" role="status"><c:out value="${loginMessage}"/></div>
            </c:if>

            <form class="auth-form" method="post" action="<c:url value='/login'/>">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <c:if test="${not empty loginRedirect}">
                    <input type="hidden" name="redirect" value="<c:out value='${loginRedirect}'/>">
                </c:if>
                <c:if test="${not empty loginIntent}">
                    <input type="hidden" name="intent" value="<c:out value='${loginIntent}'/>">
                </c:if>

                <div class="auth-field">
                    <label for="loginEmail">Email</label>
                    <input id="loginEmail" name="email" type="email" maxlength="100" autocomplete="email" required>
                </div>

                <div class="auth-field">
                    <label for="loginPassword">Contraseña</label>
                    <input id="loginPassword" name="password" type="password" autocomplete="current-password" required>
                </div>

                <label class="auth-check">
                    <input type="checkbox" name="remember-me">
                    <span>Recordar sesión</span>
                </label>

                <button type="submit" class="btn-primary auth-submit">Ingresar</button>
            </form>

            <p class="auth-switch">
                ¿No tenés cuenta?
                <a href="<c:url value='/register'/>">Registrate</a>
            </p>
        </section>
    </main>

</body>
</html>
