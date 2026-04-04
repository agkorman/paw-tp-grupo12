<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text" required="true" %>
<%@ attribute name="size" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="btnSize" value="${not empty size ? size : 'md'}" />
<c:set var="btnCssClass" value="${not empty cssClass ? cssClass : ''}" />
<c:set var="btnDisabled" value="${disabled ne null ? disabled : false}" />

<c:set var="classes" value="btn btn-${btnSize} ${btnCssClass}" />

<c:choose>
    <c:when test="${btnDisabled}">
        <button type="button" class="${classes}" disabled><c:out value="${text}"/></button>
    </c:when>
    <c:otherwise>
        <button type="button" class="${classes}"><c:out value="${text}"/></button>
    </c:otherwise>
</c:choose>
