<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="selectedCar" required="true" type="ar.edu.itba.paw.model.Car" %>
<%@ attribute name="averageRating" required="false" type="java.math.BigDecimal" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

<aside class="review-form-panel car-info-panel">
    <h2>Datos del auto</h2>
    <div class="car-info-list">
        <div class="car-info-row">
            <span class="car-info-label">Nombre</span>
            <span class="car-info-value"><c:out value="${selectedCar.model}"/></span>
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

    <button
            id="openReviewModalBtn"
            type="button"
            class="btn-primary add-review-btn"
            data-open-review-modal="create"
            data-review-car-id="${selectedCar.id}"
            data-auth-resume-intent="create-review"
            <c:if test="${not authenticated}">
                data-auth-required="true"
                data-auth-required-action="publicar una reseña"
                data-auth-required-intent="create-review"
            </c:if>>
        Agregar reseña
    </button>
</aside>
