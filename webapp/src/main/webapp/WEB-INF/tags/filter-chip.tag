<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="label" required="true" %>
<%@ attribute name="href" required="false" rtexprvalue="true" %>
<%@ attribute name="active" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:set var="chipClasses" value="filter-chip"/>
<c:if test="${active}">
    <c:set var="chipClasses" value="filter-chip filter-chip--active"/>
</c:if>

<c:choose>
    <c:when test="${not empty href}">
        <a href="${fn:escapeXml(href)}" class="${chipClasses}">
            <c:out value="${label}"/>
            <pa:icon name="chevron-down" size="12"/>
        </a>
    </c:when>
    <c:otherwise>
        <button type="button" class="${chipClasses}">
            <c:out value="${label}"/>
            <pa:icon name="chevron-down" size="12"/>
        </button>
    </c:otherwise>
</c:choose>
