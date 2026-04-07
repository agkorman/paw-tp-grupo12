<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="cars" required="true" type="java.util.List" %>
<%@ attribute name="reviewStatsByCarId" required="true" type="java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<div id="carsCatalogContent" class="catalog-content">
    <div class="catalog-meta">
        <span class="count-label">${fn:length(cars)} vehículos encontrados</span>
    </div>

    <c:set var="isEmptyCatalog" value="${empty cars}"/>

    <section class="catalog-section">
        <div class="cars-grid <c:if test='${isEmptyCatalog}'>cars-grid--empty</c:if>">
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
                    averageRating="${reviewStatsByCarId[car.id].averageRating}"
                    reviewCount="${reviewStatsByCarId[car.id].reviewCount}"/>
            </c:forEach>

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
        </div>
    </section>

    <c:if test="${not empty cars}">
        <div class="discover-wrap">
            <pa:button text="Descubrir más" variant="secondary" icon="chevron-down"/>
        </div>
    </c:if>
</div>
