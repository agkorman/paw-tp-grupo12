<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="href" required="true" %>
<%@ attribute name="accentClass" required="true" %>
<%@ attribute name="badgeCode" required="false" %>
<%@ attribute name="titleCode" required="true" %>
<%@ attribute name="descriptionCode" required="true" %>
<%@ attribute name="metaCode" required="true" %>
<%@ attribute name="actionCode" required="true" %>
<%@ attribute name="joined" required="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<a class="community-card" href="${fn:escapeXml(href)}">
    <div class="community-card-accent ${fn:escapeXml(accentClass)}">
        <c:if test="${not empty badgeCode}">
            <p class="community-card-badge">
                <pa:icon name="star-filled" size="12"/>
                <span><spring:message code="${badgeCode}"/></span>
            </p>
        </c:if>
        <div class="community-card-illustration" aria-hidden="true">
            <pa:icon name="car" size="54" cssClass="community-card-illustration-icon"/>
        </div>
    </div>
    <div class="community-card-body">
        <h2 class="community-card-title"><spring:message code="${titleCode}"/></h2>
        <p class="community-card-description"><spring:message code="${descriptionCode}"/></p>
        <div class="community-card-footer">
            <p class="community-card-meta"><spring:message code="${metaCode}"/></p>
            <span class="community-card-action ${joined ? 'is-joined' : ''}">
                <spring:message code="${actionCode}"/>
            </span>
        </div>
    </div>
</a>
