<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="href" required="false" rtexprvalue="true" %>
<%@ attribute name="active" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="chipClasses" value="filter-chip"/>
<c:if test="${active}">
    <c:set var="chipClasses" value="filter-chip filter-chip--active"/>
</c:if>

<c:choose>
    <c:when test="${not empty href}">
        <a href="${href}" class="${chipClasses}">
            <c:out value="${label}"/>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="6 9 12 15 18 9"/>
            </svg>
        </a>
    </c:when>
    <c:otherwise>
        <button type="button" class="${chipClasses}">
            <c:out value="${label}"/>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="6 9 12 15 18 9"/>
            </svg>
        </button>
    </c:otherwise>
</c:choose>
