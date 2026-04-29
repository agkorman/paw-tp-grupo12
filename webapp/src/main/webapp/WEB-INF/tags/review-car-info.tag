<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="selectedCar" required="true" type="ar.edu.itba.paw.model.Car" %>
<%@ attribute name="averageRating" required="false" type="java.math.BigDecimal" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
<c:url var="newReviewUrl" value="/reviews/new">
    <c:param name="carId" value="${selectedCar.id}"/>
</c:url>
<c:url var="newReviewLoginUrl" value="/login">
    <c:param name="redirect" value="/reviews/new?carId=${selectedCar.id}"/>
    <c:param name="intent" value="create-review"/>
</c:url>

<aside class="review-form-panel car-info-panel">
    <h2>Datos del auto</h2>
    <div class="car-info-list">
        <c:if test="${not empty selectedCar.priceUsd}">
        <div class="car-info-row">
            <span class="car-info-label">Precio 0 km</span>
            <span class="car-info-value">USD <fmt:formatNumber value="${selectedCar.priceUsd}" groupingUsed="true" maxFractionDigits="0"/></span>
        </div>
        </c:if>
        <div class="car-info-row">
            <span class="car-info-label">Nombre</span>
            <span class="car-info-value"><c:out value="${selectedCar.model}"/></span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Año modelo</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${not empty selectedCar.year}"><c:out value="${selectedCar.year}"/></c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Marca</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${not empty selectedCar.brandName}"><c:out value="${selectedCar.brandName}"/></c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Tipo</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${not empty selectedCar.bodyType}"><c:out value="${selectedCar.bodyType}"/></c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Motorización</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${selectedCar.fuelType eq 'combustion'}">Combustión</c:when>
                    <c:when test="${selectedCar.fuelType eq 'hybrid'}">Híbrido</c:when>
                    <c:when test="${selectedCar.fuelType eq 'electric'}">Eléctrico</c:when>
                    <c:when test="${not empty selectedCar.fuelType}"><c:out value="${selectedCar.fuelType}"/></c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Transmisión</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${selectedCar.transmission eq 'manual'}">Manual</c:when>
                    <c:when test="${selectedCar.transmission eq 'automatic'}">Automática</c:when>
                    <c:when test="${not empty selectedCar.transmission}"><c:out value="${selectedCar.transmission}"/></c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Potencia</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${not empty selectedCar.horsepower}"><c:out value="${selectedCar.horsepower}"/> HP</c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Airbags</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${not empty selectedCar.airbagCount}"><c:out value="${selectedCar.airbagCount}"/></c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Consumo</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${not empty selectedCar.fuelConsumption}"><c:out value="${selectedCar.fuelConsumption}"/> L/100 km</c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row">
            <span class="car-info-label">Velocidad máxima</span>
            <span class="car-info-value">
                <c:choose>
                    <c:when test="${not empty selectedCar.maxSpeedKmh}"><c:out value="${selectedCar.maxSpeedKmh}"/> km/h</c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
        <div class="car-info-row car-info-row-description">
            <span class="car-info-label">Descripción</span>
            <span class="car-info-value car-info-description">
                <c:choose>
                    <c:when test="${not empty selectedCar.description}"><c:out value="${selectedCar.description}"/></c:when>
                    <c:otherwise>N/A</c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>

    <c:choose>
        <c:when test="${authenticated}">
            <a id="openReviewFormBtn"
               href="${newReviewUrl}"
               class="btn-primary add-review-btn"
               data-auth-resume-intent="create-review">
                Agregar reseña
            </a>
        </c:when>
        <c:otherwise>
            <a id="openReviewFormBtn"
               href="${newReviewLoginUrl}"
               class="btn-primary add-review-btn"
               data-auth-required="true"
               data-auth-required-action="publicar una reseña"
               data-auth-required-intent="create-review">
                Agregar reseña
            </a>
        </c:otherwise>
    </c:choose>
</aside>
