<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro | La Posta Autos</title>
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
        <section class="auth-panel" aria-labelledby="registerTitle">
            <div class="auth-header">
                <span class="auth-kicker">Registro</span>
                <h1 id="registerTitle">Creá tu cuenta</h1>
            </div>

            <c:if test="${not empty registrationError}">
                <div class="alert alert-error" role="alert"><c:out value="${registrationError}"/></div>
            </c:if>

            <form id="registerForm" class="auth-form" method="post" action="<c:url value='/register'/>"
                  data-auth-form="register" data-submit-lock="true" novalidate="novalidate">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

                <div class="auth-field">
                    <label for="registerUsername">Usuario</label>
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
                    <label for="registerEmail">Email</label>
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
                    <label for="registerPassword">Contraseña</label>
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
                    <label for="registerConfirmPassword">Confirmar contraseña</label>
                    <input
                            id="registerConfirmPassword"
                            name="confirmPassword"
                            type="password"
                            minlength="8"
                            maxlength="72"
                            autocomplete="new-password"
                            required>
                </div>

                <button type="submit" class="btn-primary auth-submit">Crear cuenta</button>
            </form>

            <p class="auth-switch">
                ¿Ya tenés cuenta?
                <a href="<c:url value='/login'/>">Ingresá</a>
            </p>
        </section>
    </main>

    <script src="<c:url value='/js/auth-form.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
