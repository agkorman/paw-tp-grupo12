<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="model"      required="true" %>
<%@ attribute name="bodyType"   required="false" %>
<%@ attribute name="carId"      required="true" %>
<%@ attribute name="hasImage"   required="true" %>
<%@ attribute name="href"       required="true" %>
<%@ attribute name="averageRating" required="false" %>
<%@ attribute name="reviewCount" required="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<a href="${fn:escapeXml(href)}" class="car-card-link" aria-label="Ver reseñas de ${fn:escapeXml(model)}">
    <div class="car-card">
        <div class="card-image-wrap">
            <c:choose>
                <c:when test="${hasImage}">
                    <c:url var="carImageUrl" value="/car-image">
                        <c:param name="carId" value="${carId}"/>
                    </c:url>
                    <img src="${carImageUrl}" alt="${fn:escapeXml(model)}" loading="lazy">
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
        <div class="card-body">
            <span class="card-category">
                <c:choose>
                    <c:when test="${not empty bodyType}"><c:out value="${bodyType}"/></c:when>
                    <c:otherwise>Vehículo</c:otherwise>
                </c:choose>
            </span>
            <div class="card-title-row">
                <span class="card-title"><c:out value="${model}"/></span>
            </div>
            <div class="card-rating-row">
                <c:choose>
                    <c:when test="${reviewCount gt 0}">
                        <span class="card-rating-badge">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                                <path d="M12 2.75l2.91 5.9 6.51.95-4.71 4.59 1.11 6.48L12 17.62l-5.82 3.05 1.11-6.48-4.71-4.59 6.51-.95L12 2.75z"/>
                            </svg>
                            <span class="card-rating-value"><c:out value="${averageRating}"/></span>
                        </span>
                        <span class="card-rating-count">
                            <c:out value="${reviewCount}"/>
                            <c:choose>
                                <c:when test="${reviewCount eq 1}">reseña</c:when>
                                <c:otherwise>reseñas</c:otherwise>
                            </c:choose>
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="card-rating-empty">Sin reseñas todavía</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-footer">
                <span class="card-meta">
                    <c:choose>
                        <c:when test="${reviewCount gt 0}">Puntaje de la comunidad sobre 5</c:when>
                        <c:otherwise>Comparte la primera impresión</c:otherwise>
                    </c:choose>
                </span>
                <span class="card-specs-link">
                    Ver reseñas
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <path d="M5 12h14M12 5l7 7-7 7"/>
                    </svg>
                </span>
            </div>
        </div>
    </div>
</a>
