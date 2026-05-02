<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="showLabel" required="false" %>
<%@ attribute name="hideLabel" required="false" %>
<%@ attribute name="cssClass" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="defaultShowLabel" code="common.action.showMore"/>
<spring:message var="defaultHideLabel" code="common.action.showLess"/>
<c:set var="resolvedShowLabel" value="${empty showLabel ? defaultShowLabel : showLabel}"/>
<c:set var="resolvedHideLabel" value="${empty hideLabel ? defaultHideLabel : hideLabel}"/>

<button
        type="button"
        class="collapsible-toggle ${cssClass}"
        data-collapsible-toggle
        data-show-label="${fn:escapeXml(resolvedShowLabel)}"
        data-hide-label="${fn:escapeXml(resolvedHideLabel)}"
        aria-expanded="false"
        hidden>
    <c:out value="${resolvedShowLabel}"/>
</button>
