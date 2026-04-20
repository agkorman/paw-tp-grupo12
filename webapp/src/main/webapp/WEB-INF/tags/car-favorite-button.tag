<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="true" %>
<%@ attribute name="favorited" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="disabled" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="favoriteLabel">
    <c:choose>
        <c:when test="${not empty label}">${label}</c:when>
        <c:otherwise>Favorito</c:otherwise>
    </c:choose>
</c:set>

<button
        type="button"
        class="favorite-toggle ${favorited ? 'is-active' : ''}"
        data-favorite-toggle
        data-car-id="${fn:escapeXml(carId)}"
        data-favorited="${favorited}"
        aria-pressed="${favorited}"
        aria-label="${favorited ? 'Quitar de favoritos' : 'Agregar a favoritos'}"
        <c:if test="${disabled}">disabled</c:if>>
    <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M12 21s-6.8-4.2-9.5-8.4C0.6 9.5 2.4 5 6.2 5c2 0 3.5 1 4.4 2.3C11.5 6 13 5 15 5c3.8 0 5.6 4.5 3.7 7.6C16.8 16.8 12 21 12 21z"/>
    </svg>
    <span><c:out value="${favoriteLabel}"/></span>
</button>
