<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="selectedCar" required="true" type="ar.edu.itba.paw.model.Car" %>
<%@ attribute name="averageRating" required="false" type="java.math.BigDecimal" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

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
    </div>

    <sec:authorize access="isAuthenticated()">
        <button id="openReviewModalBtn" type="button" class="btn-primary add-review-btn">Agregar reseña</button>
    </sec:authorize>
    <sec:authorize access="isAnonymous()">
        <c:url var="newReviewUrl" value="/reviews/new">
            <c:param name="carId" value="${selectedCar.id}"/>
        </c:url>
        <a href="${newReviewUrl}" class="btn-secondary add-review-btn">Ingresar para reseñar</a>
    </sec:authorize>
</aside>
