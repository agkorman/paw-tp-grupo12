<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="selectedCar" required="true" type="ar.edu.itba.paw.model.Car" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<article class="selected-car-panel">
    <h2>Imagen del auto</h2>
    <div class="selected-car-image">
        <c:choose>
            <c:when test="${selectedCar.hasImage}">
                <c:url var="selectedCarImageUrl" value="/car-image">
                    <c:param name="carId" value="${selectedCar.id}"/>
                </c:url>
                <img src="${selectedCarImageUrl}" alt="${fn:escapeXml(selectedCar.model)}" loading="eager">
            </c:when>
            <c:otherwise>
                <div class="img-placeholder">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                        <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                    </svg>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</article>
