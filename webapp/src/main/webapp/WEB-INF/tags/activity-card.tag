<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activityCard" required="true" type="ar.edu.itba.paw.webapp.controller.ActivityController.ActivityCardView" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="activityMetricsAria" code="activity.card.metrics.aria"/>
<spring:message var="primaryMetricText" code="${activityCard.primaryMetricKey}" arguments="${activityCard.primaryMetricValue}"/>
<c:if test="${not empty activityCard.secondaryMetricKey}">
    <spring:message var="secondaryMetricText" code="${activityCard.secondaryMetricKey}" arguments="${activityCard.secondaryMetricValue}"/>
</c:if>

<article class="activity-card">
    <div class="activity-card-topline">
        <div class="activity-card-avatar" aria-hidden="true"></div>
        <div class="activity-card-topline-copy">
            <p class="activity-card-meta">
                <c:choose>
                    <c:when test="${not empty activityCard.authorHref}">
                        <a class="activity-card-author-link" href="${fn:escapeXml(activityCard.authorHref)}">
                            <strong><c:out value="${activityCard.authorName}"/></strong>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <strong><c:out value="${activityCard.authorName}"/></strong>
                    </c:otherwise>
                </c:choose>
                <span aria-hidden="true">•</span>
                <span><c:out value="${activityCard.timeText}"/></span>
            </p>

            <p class="activity-card-context">
                <span class="activity-card-context-label"><spring:message code="${activityCard.contextLabelKey}"/></span>
                <span class="activity-card-context-value"><c:out value="${activityCard.contextValue}"/></span>
            </p>
        </div>
    </div>

    <h2 class="activity-card-title">
        <a href="${fn:escapeXml(activityCard.href)}"><c:out value="${activityCard.title}"/></a>
    </h2>
    <p class="activity-card-body"><c:out value="${activityCard.body}"/></p>
    <c:if test="${not empty activityCard.imageUrls}">
        <div class="activity-card-gallery-shell">
            <pa:image-gallery imageUrls="${activityCard.imageUrls}"
                              altKey="${activityCard.imageAltKey}"
                              cssClass="activity-card-gallery"/>
            <span class="activity-card-gallery-count">
                1 / <c:out value="${fn:length(activityCard.imageUrls)}"/>
            </span>
        </div>
    </c:if>

    <div class="activity-card-metrics" aria-label="${fn:escapeXml(activityMetricsAria)}">
        <span class="activity-card-metric"><c:out value="${primaryMetricText}"/></span>
        <c:if test="${not empty activityCard.secondaryMetricKey}">
            <span class="activity-card-metric"><c:out value="${secondaryMetricText}"/></span>
        </c:if>
    </div>
</article>
