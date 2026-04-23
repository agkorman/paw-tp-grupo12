<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="rating" required="true" type="java.lang.Number" %>
<%@ attribute name="size" required="false" type="java.lang.Integer" %>
<%@ attribute name="idPrefix" required="true" type="java.lang.String" %>
<%@ attribute name="filledColor" required="false" type="java.lang.String" %>
<%@ attribute name="emptyColor" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:set var="resolvedSize" value="${empty size ? 24 : size}"/>
<c:set var="resolvedFilledColor" value="${empty filledColor ? '#ff5719' : filledColor}"/>
<c:set var="resolvedEmptyColor" value="${empty emptyColor ? '#3a3a3a' : emptyColor}"/>

<c:forEach var="i" begin="1" end="5">
    <c:set var="fillPercent" value="${rating >= i ? 100 : rating >= i - 0.5 ? 50 : 0}"/>
    <pa:star-icon size="${resolvedSize}"
                  gradientId="${idPrefix}${i}"
                  fillPercent="${fillPercent}"
                  filledColor="${resolvedFilledColor}"
                  emptyColor="${resolvedEmptyColor}"/>
</c:forEach>
