<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="true" %>
<%@ attribute name="favorited" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="disabled" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="defaultFavoriteLabel"  code="common.label.favorite"/>
<spring:message var="addFavoriteLabel"      code="cars.favorite.add"/>
<spring:message var="shortAddFavoriteLabel" code="cars.favorite.add.short"/>
<spring:message var="removeFavoriteLabel"   code="cars.favorite.remove"/>

<c:set var="favoriteLabel" value="${empty label ? defaultFavoriteLabel : label}"/>

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
            data-favorite-add-label="${fn:escapeXml(addFavoriteLabel)}"
            data-favorite-remove-label="${fn:escapeXml(removeFavoriteLabel)}"
            data-favorite-active-label="${fn:escapeXml(favoriteLabel)}"
            data-favorite-inactive-label="${fn:escapeXml(shortAddFavoriteLabel)}"
            aria-pressed="${favorited}"
            aria-label="${fn:escapeXml(favorited ? removeFavoriteLabel : addFavoriteLabel)}"
            <c:if test="${disabled}">disabled</c:if>>
        <pa:icon name="heart" size="18"/>
        <span><c:out value="${favoriteLabel}"/></span>
    </button>
</form>
