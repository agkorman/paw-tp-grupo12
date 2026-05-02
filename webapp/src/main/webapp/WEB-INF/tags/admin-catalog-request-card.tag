<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" type="java.lang.Long" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="submitter" required="true" %>
<%@ attribute name="comments" required="false" %>
<%@ attribute name="type" required="true" description="brand or body-type" %>
<%@ attribute name="kicker" required="false" %>
<%@ attribute name="showSubmitterPrefix" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="submitterPrefixEnabled" value="${empty showSubmitterPrefix or showSubmitterPrefix}"/>
<spring:message var="defaultKicker" code="admin.catalogRequest.kicker"/>
<spring:message var="sentByLabel" code="common.label.sentBy"/>

<button type="button"
        class="admin-catalog-request-card"
        data-open-admin-catalog-request
        data-catalog-type="${fn:escapeXml(type)}"
        data-request-id="${id}"
        data-request-name="${fn:escapeXml(name)}"
        data-request-submitter="${fn:escapeXml(submitter)}"
        data-request-comments="${fn:escapeXml(comments)}">
    <span class="admin-catalog-request-card-kicker">
        <c:out value="${empty kicker ? defaultKicker : kicker}"/>
    </span>
    <span class="admin-catalog-request-card-name"><c:out value="${name}"/></span>
    <span class="admin-catalog-request-card-submitter">
        <c:if test="${submitterPrefixEnabled}"><c:out value="${sentByLabel}"/> </c:if><c:out value="${submitter}"/>
    </span>
</button>
