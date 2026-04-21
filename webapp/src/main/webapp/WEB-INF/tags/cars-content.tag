<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="cars" required="true" type="java.util.List" %>
<%@ attribute name="reviewStatsByCarId" required="true" type="java.util.Map" %>
<%@ attribute name="showHp" required="false" %>
<%@ attribute name="showSpeed" required="false" %>
<%@ attribute name="showConsumption" required="false" %>
<%@ attribute name="showAirbags" required="false" %>
<%@ attribute name="showTransmission" required="false" %>
<%@ attribute name="showFuelType" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<div id="carsCatalogContent" class="catalog-content">
    <section class="catalog-section">
        <div class="cars-grid">
            <c:forEach var="car" items="${cars}">
                <c:url var="reviewUrl" value="/reviews">
                    <c:param name="carId" value="${car.id}"/>
                </c:url>
                <pa:car-card
                    model="${car.brandName} ${car.model}"
                    bodyType="${car.bodyType}"
                    carId="${car.id}"
                    hasImage="${car.hasImage}"
                    href="${reviewUrl}"
                    favorited="${car.id mod 2 eq 0}"
                    averageRating="${reviewStatsByCarId[car.id].averageRating}"
                    reviewCount="${reviewStatsByCarId[car.id].reviewCount}"
                    horsepower="${car.horsepower}"
                    maxSpeedKmh="${car.maxSpeedKmh}"
                    fuelConsumption="${car.fuelConsumption}"
                    airbagCount="${car.airbagCount}"
                    transmission="${car.transmission}"
                    fuelType="${car.fuelType}"
                    showHp="${showHp}"
                    showSpeed="${showSpeed}"
                    showConsumption="${showConsumption}"
                    showAirbags="${showAirbags}"
                    showTransmission="${showTransmission}"
                    showFuelType="${showFuelType}"/>
            </c:forEach>

            <sec:authorize access="isAuthenticated()">
                <button type="button" class="car-request-card" data-open-create-car-modal>
                    <span class="car-request-card-icon" aria-hidden="true">
                        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round">
                            <path d="M12 5v14"/>
                            <path d="M5 12h14"/>
                        </svg>
                    </span>
                    <strong class="car-request-card-title">¿No encontrás el auto?</strong>
                    <span class="car-request-card-copy">Ayudanos a completar la galería con el modelo que falta.</span>
                    <span class="btn-primary car-request-card-action">Agregar auto</span>
                </button>
            </sec:authorize>
            <sec:authorize access="isAnonymous()">
                <c:url var="newCarUrl" value="/cars/new"/>
                <a class="car-request-card" href="${newCarUrl}">
                    <span class="car-request-card-icon" aria-hidden="true">
                        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round">
                            <path d="M12 5v14"/>
                            <path d="M5 12h14"/>
                        </svg>
                    </span>
                    <strong class="car-request-card-title">¿No encontrás el auto?</strong>
                    <span class="car-request-card-copy">Iniciá sesión para sumar el modelo que falta.</span>
                    <span class="btn-primary car-request-card-action">Ingresar</span>
                </a>
            </sec:authorize>
        </div>
    </section>

</div>
