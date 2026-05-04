<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="recommend.results.title" styles="/css/cars.css|/css/review-tags.css"/>
<body>
    <pa:nav activePage="recommend"/>

    <spring:message var="reviewsActionText" code="cars.card.review.view"/>

    <main class="recommend-page recommend-results-page">
        <section class="recommend-header">
            <p class="recommend-kicker"><spring:message code="recommend.results.kicker"/></p>
            <h1><spring:message code="recommend.results.heading"/></h1>
            <p><spring:message code="recommend.results.description"/></p>
        </section>

        <c:choose>
            <c:when test="${hasRecommendations}">
                <div class="recommend-results-grid">
                    <c:forEach var="recommendation" items="${recommendations}">
                        <c:set var="car" value="${recommendation.car}"/>
                        <c:set var="stats" value="${reviewStatsByCarId[car.id]}"/>
                        <article class="recommend-result">
                            <pa:recommendation-reason positives="${recommendation.positiveHighlights}" negatives="${recommendation.negativeHighlights}"/>
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
                                    actionText="${reviewsActionText}"/>
                        </article>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <section class="recommend-empty">
                    <h2><spring:message code="recommend.results.empty.title"/></h2>
                    <p><spring:message code="recommend.results.empty.description"/></p>
                    <a class="btn-primary" href="<c:url value='/cars/recommend'/>"><spring:message code="recommend.results.adjust"/></a>
                </section>
            </c:otherwise>
        </c:choose>
    </main>

    <pa:script src="/js/reactions.js"/>
    <pa:footer/>
</body>
</html>
