<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${statusCode}"/> · La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/landing.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/error.css'/>">
</head>
<body>

    <pa:nav activePage=""/>

    <main class="error-page">
        <section class="error-hero">
            <span class="error-status"><c:out value="${statusCode}"/></span>
            <h1 class="error-title"><c:out value="${title}"/></h1>
            <p class="error-text"><c:out value="${description}"/></p>
            <div class="error-actions">
                <a class="btn-primary" href="<c:url value='/'/>">Volver al inicio</a>
                <a class="btn-secondary" href="<c:url value='/cars'/>">Ir al catálogo</a>
            </div>
        </section>
    </main>

    <pa:footer/>

</body>
</html>
