<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="cars" required="true" type="java.util.List" %>
<%@ attribute name="resultCount" required="true" %>
<%@ attribute name="reviewStatsByCarId" required="true" type="java.util.Map" %>
<%@ attribute name="showHp" required="false" %>
<%@ attribute name="showSpeed" required="false" %>
<%@ attribute name="showConsumption" required="false" %>
<%@ attribute name="showAirbags" required="false" %>
<%@ attribute name="showTransmission" required="false" %>
<%@ attribute name="showFuelType" required="false" %>
<%@ attribute name="showPrice" required="false" %>
<%@ attribute name="showYear" required="false" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="false" type="java.lang.Integer" %>
<%@ attribute name="criteria" required="false" type="ar.edu.itba.paw.model.CarSearchCriteria" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:url var="newCarUrl" value="/cars/new"/>
<c:set var="showCarRequestCard" value="${empty totalPages or totalPages <= 1 or currentPage >= totalPages}"/>

<div id="carsCatalogContent" class="catalog-content" data-result-count="${resultCount}">
    <section class="catalog-section">
        <div class="cars-grid">
            <c:forEach var="car" items="${cars}">
                <c:url var="reviewUrl" value="/reviews">
                    <c:param name="carId" value="${car.id}"/>
                </c:url>
                <pa:car-card
                    model="${car.brandName} ${car.model}"
                    year="${car.year}"
                    bodyType="${car.bodyType}"
                    carId="${car.id}"
                    hasImage="${car.hasImage}"
                    href="${reviewUrl}"
                    averageRating="${reviewStatsByCarId[car.id].averageRating}"
                    reviewCount="${reviewStatsByCarId[car.id].reviewCount}"
                    horsepower="${car.horsepower}"
                    maxSpeedKmh="${car.maxSpeedKmh}"
                    fuelConsumption="${car.fuelConsumption}"
                    airbagCount="${car.airbagCount}"
                    transmission="${car.transmission}"
                    fuelType="${car.fuelType}"
                    priceUsd="${car.priceUsd}"
                    showHp="${showHp}"
                    showSpeed="${showSpeed}"
                    showConsumption="${showConsumption}"
                    showAirbags="${showAirbags}"
                    showTransmission="${showTransmission}"
                    showFuelType="${showFuelType}"
                    showPrice="${showPrice}"
                    showYear="${showYear}"/>
            </c:forEach>

            <c:if test="${showCarRequestCard}">
                <sec:authorize access="isAuthenticated()">
                    <a href="${newCarUrl}" class="car-request-card" data-auth-resume-intent="create-car">
                        <span class="car-request-card-icon" aria-hidden="true">
                            <pa:icon name="plus" size="56"/>
                        </span>
                        <strong class="car-request-card-title">¿No encontrás el auto?</strong>
                        <span class="car-request-card-copy">Ayudanos a completar la galería con el modelo que falta.</span>
                        <span class="btn-primary car-request-card-action">Agregar auto</span>
                    </a>
                </sec:authorize>
                <sec:authorize access="isAnonymous()">
                    <a href="${newCarUrl}"
                       class="car-request-card"
                       data-auth-required="true"
                       data-auth-required-action="agregar un auto"
                       data-auth-required-intent="create-car"
                       data-auth-return-url="/cars/new">
                        <span class="car-request-card-icon" aria-hidden="true">
                            <pa:icon name="plus" size="56"/>
                        </span>
                        <strong class="car-request-card-title">¿No encontrás el auto?</strong>
                        <span class="car-request-card-copy">Iniciá sesión para sumar el modelo que falta.</span>
                        <span class="btn-primary car-request-card-action">Ingresar</span>
                    </a>
                </sec:authorize>
            </c:if>
        </div>

        <c:if test="${not empty totalPages and totalPages > 1}">
            <c:url var="carsBaseUrl" value="/cars"/>
            <c:url var="carsFragmentUrl" value="/cars/content"/>
            <jsp:useBean id="paginationParams" class="java.util.LinkedHashMap" scope="page"/>
            <c:if test="${not empty criteria}">
                <c:if test="${not empty criteria.q}"><c:set target="${paginationParams}" property="q" value="${criteria.q}"/></c:if>
                <c:if test="${not empty criteria.brand}"><c:set target="${paginationParams}" property="brand" value="${criteria.brand}"/></c:if>
                <c:if test="${not empty criteria.bodyType}"><c:set target="${paginationParams}" property="bodyType" value="${criteria.bodyType}"/></c:if>
                <c:if test="${not empty criteria.yearMin}"><c:set target="${paginationParams}" property="yearMin" value="${criteria.yearMin}"/></c:if>
                <c:if test="${not empty criteria.yearMax}"><c:set target="${paginationParams}" property="yearMax" value="${criteria.yearMax}"/></c:if>
                <c:if test="${not empty criteria.fuelType}"><c:set target="${paginationParams}" property="fuelType" value="${criteria.fuelType}"/></c:if>
                <c:if test="${not empty criteria.horsepowerMin}"><c:set target="${paginationParams}" property="horsepowerMin" value="${criteria.horsepowerMin}"/></c:if>
                <c:if test="${not empty criteria.horsepowerMax}"><c:set target="${paginationParams}" property="horsepowerMax" value="${criteria.horsepowerMax}"/></c:if>
                <c:if test="${not empty criteria.airbagMin}"><c:set target="${paginationParams}" property="airbagMin" value="${criteria.airbagMin}"/></c:if>
                <c:if test="${not empty criteria.transmission}"><c:set target="${paginationParams}" property="transmission" value="${criteria.transmission}"/></c:if>
                <c:if test="${not empty criteria.fuelConsumptionMax}"><c:set target="${paginationParams}" property="fuelConsumptionMax" value="${criteria.fuelConsumptionMax}"/></c:if>
                <c:if test="${not empty criteria.maxSpeedMin}"><c:set target="${paginationParams}" property="maxSpeedMin" value="${criteria.maxSpeedMin}"/></c:if>
                <c:if test="${not empty criteria.priceMin}"><c:set target="${paginationParams}" property="priceMin" value="${criteria.priceMin}"/></c:if>
                <c:if test="${not empty criteria.priceMax}"><c:set target="${paginationParams}" property="priceMax" value="${criteria.priceMax}"/></c:if>
                <c:if test="${not empty criteria.sortBy}"><c:set target="${paginationParams}" property="sortBy" value="${criteria.sortBy}"/></c:if>
            </c:if>
            <pa:pagination currentPage="${currentPage}"
                           totalPages="${totalPages}"
                           baseUrl="${carsBaseUrl}"
                           extraParams="${paginationParams}"
                           fragmentUrl="${carsFragmentUrl}"
                           target="#carsCatalogContent"
                           ariaLabel="Paginación de autos"/>
        </c:if>
    </section>

</div>
