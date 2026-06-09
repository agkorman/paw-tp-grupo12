<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="false" type="java.lang.String" %>
<%@ attribute name="titleCode" required="false" type="java.lang.String" %>
<%@ attribute name="styles" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>
        <c:choose>
            <c:when test="${not empty title}"><c:out value="${title}"/></c:when>
            <c:otherwise><spring:message code="${titleCode}"/></c:otherwise>
        </c:choose>
    </title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <pa:stylesheet href="/css/design-system.css"/>
    <pa:stylesheet href="/css/layout.css"/>
    <pa:stylesheet href="/css/buttons.css"/>
    <pa:stylesheet href="/css/toast.css"/>
    <pa:stylesheet href="/css/controls.css"/>
    <pa:stylesheet href="/css/action-menu.css"/>
    <pa:stylesheet href="/css/reactions.css"/>
    <pa:stylesheet href="/css/tabs.css"/>
    <pa:stylesheet href="/css/pagination.css"/>
    <pa:stylesheet href="/css/modal.css"/>
    <pa:stylesheet href="/css/users-search.css"/>
    <c:if test="${not empty styles}">
        <c:forEach var="styleHref" items="${fn:split(styles, '|')}">
            <c:if test="${not empty styleHref}">
                <pa:stylesheet href="${styleHref}"/>
            </c:if>
        </c:forEach>
    </c:if>
</head>
