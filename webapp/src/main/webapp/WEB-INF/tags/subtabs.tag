<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="tabCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="labels" required="true" type="java.lang.String" %>
<%@ attribute name="hrefs" required="true" type="java.lang.String" %>
<%@ attribute name="counts" required="true" type="java.lang.String" %>
<%@ attribute name="values" required="true" type="java.lang.String" %>
<%@ attribute name="activeValue" required="true" type="java.lang.String" %>
<%@ attribute name="ariaLabel" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="subtabLabels" value="${fn:split(labels, '|')}"/>
<c:set var="subtabHrefs" value="${fn:split(hrefs, '|')}"/>
<c:set var="subtabCounts" value="${fn:split(counts, '|')}"/>
<c:set var="subtabValues" value="${fn:split(values, '|')}"/>
<c:set var="resolvedTabCount" value="${tabCount < 1 ? 1 : (tabCount > 4 ? 4 : tabCount)}"/>

<nav class="subtabs-list subtabs-list-${resolvedTabCount}" aria-label="${empty ariaLabel ? 'Secciones' : fn:escapeXml(ariaLabel)}">
    <c:forEach var="label" items="${subtabLabels}" varStatus="status">
        <c:if test="${status.index < resolvedTabCount}">
            <a class="subtab"
               href="${subtabHrefs[status.index]}"
               <c:if test="${subtabValues[status.index] eq activeValue}">aria-current="page"</c:if>>
                <span><c:out value="${label}"/></span>
                <strong><c:out value="${subtabCounts[status.index]}"/></strong>
            </a>
        </c:if>
    </c:forEach>
</nav>
