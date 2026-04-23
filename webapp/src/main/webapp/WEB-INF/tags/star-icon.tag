<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="size" required="false" type="java.lang.Integer" %>
<%@ attribute name="gradientId" required="true" type="java.lang.String" %>
<%@ attribute name="fillPercent" required="false" type="java.lang.Integer" %>
<%@ attribute name="filledColor" required="false" type="java.lang.String" %>
<%@ attribute name="emptyColor" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="resolvedSize" value="${empty size ? 24 : size}"/>
<c:set var="resolvedFillPercent" value="${empty fillPercent ? 100 : fillPercent}"/>
<c:set var="resolvedFilledColor" value="${empty filledColor ? '#ff5719' : filledColor}"/>
<c:set var="resolvedEmptyColor" value="${empty emptyColor ? '#3a3a3a' : emptyColor}"/>

<svg viewBox="0 0 24 24" width="${resolvedSize}" height="${resolvedSize}" aria-hidden="true">
    <defs>
        <linearGradient id="${gradientId}" x1="0" x2="1" y1="0" y2="0">
            <c:choose>
                <c:when test="${resolvedFillPercent le 0}">
                    <stop offset="0%" stop-color="${resolvedEmptyColor}"/>
                    <stop offset="100%" stop-color="${resolvedEmptyColor}"/>
                </c:when>
                <c:when test="${resolvedFillPercent ge 100}">
                    <stop offset="0%" stop-color="${resolvedFilledColor}"/>
                    <stop offset="100%" stop-color="${resolvedFilledColor}"/>
                </c:when>
                <c:otherwise>
                    <stop offset="${resolvedFillPercent}%" stop-color="${resolvedFilledColor}"/>
                    <stop offset="${resolvedFillPercent}%" stop-color="${resolvedEmptyColor}"/>
                </c:otherwise>
            </c:choose>
        </linearGradient>
    </defs>
    <path fill="url(#${gradientId})" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
</svg>
