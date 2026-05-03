<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="true" %>
<%@ attribute name="favorited" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="disabled" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:set var="favoriteLabel">
    <c:choose>
        <c:when test="${not empty label}"><c:out value="${label}"/></c:when>
        <c:otherwise>Favorito</c:otherwise>
    </c:choose>
</c:set>

<c:url var="favoriteAction" value="/cars/${carId}/favorite"/>

<form class="favorite-form" method="post" action="${favoriteAction}" data-favorite-form>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <input type="hidden" name="favorite" value="${favorited ? 'false' : 'true'}" data-favorite-next-value>
    <button
            type="submit"
            class="favorite-toggle ${favorited ? 'is-active' : ''}"
            data-favorite-toggle
            data-car-id="${fn:escapeXml(carId)}"
            data-favorited="${favorited}"
            aria-pressed="${favorited}"
            aria-label="${favorited ? 'Quitar de favoritos' : 'Agregar a favoritos'}"
            <c:if test="${disabled}">disabled</c:if>>
        <pa:icon name="heart" size="18"/>
        <span><c:out value="${favoriteLabel}"/></span>
    </button>
</form>
