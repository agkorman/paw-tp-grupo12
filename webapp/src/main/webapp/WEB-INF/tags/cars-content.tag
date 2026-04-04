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

    <section class="catalog-section">
        <c:choose>
            <c:when test="${empty cars}">
                <div class="empty-state">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                        <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                    </svg>
                    <p>No se encontraron vehículos en la galería.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="cars-grid">
                    <c:forEach var="car" items="${cars}">
                        <c:url var="reviewUrl" value="/reviews">
                            <c:param name="carId" value="${car.id}"/>
                        </c:url>
                        <pa:car-card
                            model="${car.model}"
                            bodyType="${car.bodyType}"
                            carId="${car.id}"
                            hasImage="${car.hasImage}"
                            href="${reviewUrl}"
                            averageRating="${reviewStatsByCarId[car.id].averageRating}"
                            reviewCount="${reviewStatsByCarId[car.id].reviewCount}"/>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <c:if test="${not empty cars}">
        <div class="discover-wrap">
            <pa:button text="Descubrir más" variant="secondary" icon="chevron-down"/>
        </div>
    </c:if>
</div>
