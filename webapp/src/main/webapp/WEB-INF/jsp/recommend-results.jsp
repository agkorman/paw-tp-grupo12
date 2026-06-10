<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="recommend.results.title" styles="/css/cars.css|/css/review-tags.css|/css/recommend-results.css"/>
<body>
    <pa:nav activePage="recommend"/>

    <main class="recommend-page recommend-results-page">
        <section class="recommend-header">
            <div class="recommend-header-copy">
                <p class="recommend-kicker"><spring:message code="recommend.results.kicker"/></p>
                <h1><spring:message code="recommend.results.heading"/></h1>
                <p><spring:message code="recommend.results.description"/></p>
            </div>
        </section>

        <c:choose>
            <c:when test="${hasRecommendations}">
                <div class="recommend-results-grid">
                    <c:forEach var="recommendation" items="${recommendations}" varStatus="rank">
                        <c:set var="car" value="${recommendation.car}"/>
                        <c:set var="stats" value="${reviewStatsByCarId[car.id]}"/>
                        <c:url var="recommendationReviewUrl" value="/reviews/car/${car.id}"/>
                        <article class="recommend-result">
                            <span class="recommend-rank">#${rank.index + 1}</span>
                            <pa:recommendation-reason positives="${recommendation.positiveHighlights}" negatives="${recommendation.negativeHighlights}"/>
                            <pa:car-card
                                    model="${car.brandName} ${car.model}"
                                    year="${car.year}"
                                    bodyType="${car.bodyType}"
                                    carId="${car.id}"
                                    hasImage="${car.hasImage}"
                                    href="${recommendationReviewUrl}"
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

    <pa:footer/>
</body>
</html>
