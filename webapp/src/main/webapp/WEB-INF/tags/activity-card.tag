<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activityCard" required="true" type="ar.edu.itba.paw.webapp.controller.ActivityController.ActivityCardView" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="activityMetricsAria" code="activity.card.metrics.aria"/>

<c:choose>
    <c:when test="${fn:contains(activityCard.href, '#')}">
        <c:url var="resolvedCardHref" value="${fn:substringBefore(activityCard.href, '#')}"/>
        <c:set var="resolvedCardHref" value="${resolvedCardHref}#${fn:substringAfter(activityCard.href, '#')}"/>
    </c:when>
    <c:otherwise>
        <c:url var="resolvedCardHref" value="${activityCard.href}"/>
    </c:otherwise>
</c:choose>
<c:if test="${not empty activityCard.authorHref}">
    <c:url var="resolvedAuthorHref" value="${activityCard.authorHref}"/>
</c:if>

<c:choose>
    <c:when test="${not empty activityCard.authorName}">
        <c:set var="resolvedAuthorName" value="${activityCard.authorName}"/>
    </c:when>
    <c:otherwise>
        <spring:message var="resolvedAuthorName" code="activity.card.author.unknown"/>
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${not empty activityCard.primaryMetricKey}">
        <spring:message var="primaryMetricText" code="${activityCard.primaryMetricKey}" arguments="${activityCard.primaryMetricValue}"/>
    </c:when>
    <c:otherwise>
        <c:set var="primaryMetricText" value="${activityCard.primaryMetricValue}"/>
    </c:otherwise>
</c:choose>

<c:if test="${not empty activityCard.secondaryMetricKey or not empty activityCard.secondaryMetricValue}">
    <c:choose>
        <c:when test="${not empty activityCard.secondaryMetricKey}">
            <spring:message var="secondaryMetricText" code="${activityCard.secondaryMetricKey}" arguments="${activityCard.secondaryMetricValue}"/>
        </c:when>
        <c:otherwise>
            <c:set var="secondaryMetricText" value="${activityCard.secondaryMetricValue}"/>
        </c:otherwise>
    </c:choose>
</c:if>

<article class="activity-card">
    <div class="activity-card-topline">
        <div class="activity-card-avatar" aria-hidden="true">
            <pa:icon name="user-avatar" size="24"/>
        </div>
        <div class="activity-card-topline-copy">
            <p class="activity-card-meta">
                <c:choose>
                    <c:when test="${not empty activityCard.authorHref}">
                        <a class="activity-card-author-link" href="${fn:escapeXml(resolvedAuthorHref)}">
                            <strong><c:out value="${resolvedAuthorName}"/></strong>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <strong><c:out value="${resolvedAuthorName}"/></strong>
                    </c:otherwise>
                </c:choose>
                <span aria-hidden="true">•</span>
                <span><c:out value="${activityCard.timeText}"/></span>
            </p>

            <p class="activity-card-context">
                <span class="activity-card-context-label"><spring:message code="${activityCard.contextLabelKey}"/></span>
                <c:if test="${not empty activityCard.contextValue}">
                    <span class="activity-card-context-value"><c:out value="${activityCard.contextValue}"/></span>
                </c:if>
            </p>
        </div>
    </div>

    <h2 class="activity-card-title">
        <a href="${fn:escapeXml(resolvedCardHref)}"><c:out value="${activityCard.title}"/></a>
    </h2>
    <p class="activity-card-body"><c:out value="${activityCard.body}"/></p>
    <c:if test="${not empty activityCard.imageUrls}">
        <c:set var="resolvedImageUrlsJoined" value=""/>
        <c:forEach var="rawImageUrl" items="${activityCard.imageUrls}" varStatus="status">
            <c:url var="oneResolvedUrl" value="${rawImageUrl}"/>
            <c:set var="resolvedImageUrlsJoined" value="${resolvedImageUrlsJoined}${status.first ? '' : '|'}${oneResolvedUrl}"/>
        </c:forEach>
        <div class="activity-card-gallery-shell">
            <pa:image-gallery imageUrlsJoined="${resolvedImageUrlsJoined}"
                              altKey="${activityCard.imageAltKey}"
                              cssClass="activity-card-gallery"/>
            <span class="activity-card-gallery-count">
                1 / <c:out value="${fn:length(activityCard.imageUrls)}"/>
            </span>
        </div>
    </c:if>

    <div class="activity-card-metrics" aria-label="${fn:escapeXml(activityMetricsAria)}">
        <span class="activity-card-metric"><c:out value="${primaryMetricText}"/></span>
        <c:if test="${not empty secondaryMetricText}">
            <span class="activity-card-metric"><c:out value="${secondaryMetricText}"/></span>
        </c:if>
    </div>
</article>
