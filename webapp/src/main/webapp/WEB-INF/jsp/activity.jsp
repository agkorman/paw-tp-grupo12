<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Actividad | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/activity.css'/>">
</head>
<body>
    <pa:nav activePage="activity"/>

    <main class="activity-page">
        <c:choose>
            <c:when test="${empty activityReviews}">
                <div class="activity-empty-state">
                    <p>No hay reseñas recientes para mostrar.</p>
                </div>
            </c:when>
            <c:otherwise>
                <section class="activity-feed" aria-label="Reseñas recientes">
                    <c:forEach var="activityReview" items="${activityReviews}">
                        <pa:activity-review-card reviewCard="${activityReview}"/>
                    </c:forEach>
                </section>
            </c:otherwise>
        </c:choose>
    </main>
</body>
</html>
