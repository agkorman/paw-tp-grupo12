<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="text"    required="true" %>
<%@ attribute name="variant" required="false" %>
<%@ attribute name="icon"    required="false" %>
<%@ attribute name="href"    required="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:choose>
    <c:when test="${variant eq 'secondary'}"><c:set var="v" value="secondary"/></c:when>
    <c:otherwise><c:set var="v" value="primary"/></c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty href}">
        <a href="${fn:escapeXml(href)}" class="btn-${v}">
            <c:out value="${text}"/>
            <c:if test="${icon eq 'chevron-down'}">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="6 9 12 15 18 9"/>
                </svg>
            </c:if>
            <c:if test="${icon eq 'arrow-right'}">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
            </c:if>
        </a>
    </c:when>
    <c:otherwise>
        <button type="button" class="btn-${v}">
            <c:out value="${text}"/>
            <c:if test="${icon eq 'chevron-down'}">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="6 9 12 15 18 9"/>
                </svg>
            </c:if>
            <c:if test="${icon eq 'arrow-right'}">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M5 12h14M12 5l7 7-7 7"/>
                </svg>
            </c:if>
        </button>
    </c:otherwise>
</c:choose>
