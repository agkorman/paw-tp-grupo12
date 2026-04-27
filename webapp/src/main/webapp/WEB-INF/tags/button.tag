<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text"    required="true" %>
<%@ attribute name="variant" required="false" %>
<%@ attribute name="icon"    required="false" %>
<%@ attribute name="href"    required="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:choose>
    <c:when test="${variant eq 'secondary'}"><c:set var="v" value="secondary"/></c:when>
    <c:otherwise><c:set var="v" value="primary"/></c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty href}">
        <a href="${fn:escapeXml(href)}" class="btn-${v}">
            <c:out value="${text}"/>
            <c:if test="${icon eq 'chevron-down'}"><pa:icon name="chevron-down" size="14"/></c:if>
            <c:if test="${icon eq 'arrow-right'}"><pa:icon name="arrow-right" size="12"/></c:if>
        </a>
    </c:when>
    <c:otherwise>
        <button type="button" class="btn-${v}">
            <c:out value="${text}"/>
            <c:if test="${icon eq 'chevron-down'}"><pa:icon name="chevron-down" size="14"/></c:if>
            <c:if test="${icon eq 'arrow-right'}"><pa:icon name="arrow-right" size="12"/></c:if>
        </button>
    </c:otherwise>
</c:choose>
