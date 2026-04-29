<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recomendaciones | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/cars.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-tags.css'/>">
</head>
<body>
    <pa:nav activePage="recommend"/>

    <main class="recommend-page recommend-results-page">
        <section class="recommend-header">
            <p class="recommend-kicker">Resultados</p>
            <h1>Tus recomendaciones</h1>
            <p>Ordenamos autos con reseñas según las preferencias que marcaste.</p>
        </section>

        <c:choose>
            <c:when test="${hasRecommendations}">
                <div class="recommend-results-grid">
                    <c:forEach var="recommendation" items="${recommendations}">
                        <c:set var="car" value="${recommendation.car}"/>
                        <c:set var="stats" value="${reviewStatsByCarId[car.id]}"/>
                        <article class="recommend-result">
                            <pa:recommendation-reason highlights="${recommendation.highlights}"/>
                            <pa:car-card
                                    model="${car.brandName} ${car.model}"
                                    bodyType="${car.bodyType}"
                                    carId="${car.id}"
                                    hasImage="${car.hasImage}"
                                    href="/reviews?carId=${car.id}"
                                    averageRating="${stats.averageRating}"
                                    reviewCount="${recommendation.reviewCount}"
                                    horsepower="${car.horsepower}"
                                    maxSpeedKmh="${car.maxSpeedKmh}"
                                    fuelConsumption="${car.fuelConsumption}"
                                    airbagCount="${car.airbagCount}"
                                    transmission="${car.transmission}"
                                    fuelType="${car.fuelType}"
                                    showHp="true"
                                    showSpeed="true"
                                    showConsumption="true"
                                    showFuelType="true"
                                    actionText="Ver reseñas"/>
                        </article>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <section class="recommend-empty">
                    <h2>No encontramos recomendaciones todavía</h2>
                    <p>Elegí al menos una preferencia concreta o probá con menos filtros.</p>
                    <a class="btn-primary" href="<c:url value='/cars/recommend'/>">Ajustar respuestas</a>
                </section>
            </c:otherwise>
        </c:choose>
    </main>

    <script src="<c:url value='/js/reactions.js'/>"></script>
</body>
</html>
