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

<c:url var="resolvedLikeAction" value="${activityCard.likeAction}"/>
<c:set var="activityLikeRedirectPath" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
<c:if test="${empty activityLikeRedirectPath}">
    <c:set var="activityLikeRedirectPath" value="/"/>
</c:if>
<c:set var="activityLikeQueryStr" value="${requestScope['javax.servlet.forward.query_string']}"/>
<c:if test="${not empty activityLikeQueryStr}">
    <c:set var="activityLikeRedirectPath" value="${activityLikeRedirectPath}?${activityLikeQueryStr}"/>
</c:if>
<c:set var="activityLikeRedirectPath" value="${activityLikeRedirectPath}#${activityCard.cardAnchorId}"/>

<article class="activity-card" id="${fn:escapeXml(activityCard.cardAnchorId)}">
    <div class="activity-card-topline">
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
                    <c:choose>
                        <c:when test="${not empty activityCard.contextHref}">
                            <c:url var="resolvedContextHref" value="${activityCard.contextHref}"/>
                            <a class="activity-card-context-link" href="${fn:escapeXml(resolvedContextHref)}">
                                <c:out value="${activityCard.contextValue}"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span class="activity-card-context-value"><c:out value="${activityCard.contextValue}"/></span>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </p>
    </div>

    <h2 class="activity-card-title">
        <a href="${fn:escapeXml(resolvedCardHref)}"><c:out value="${activityCard.title}"/></a>
    </h2>
    <p class="activity-card-body ${not empty activityCard.imageUrls ? 'activity-card-body--with-image' : 'activity-card-body--text-only'}"><c:out value="${activityCard.body}"/></p>
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
        <pa:review-like-button
                reviewId="${activityCard.likeEntityId}"
                action="${resolvedLikeAction}"
                redirect="${activityLikeRedirectPath}"
                liked="${activityCard.liked}"
                likeCount="${activityCard.likeCount}"
                disabled="${not activityCard.authenticated}"
                intent="${activityCard.cardAnchorId}"/>
        <c:if test="${not empty secondaryMetricText}">
            <span class="activity-card-metric"><c:out value="${secondaryMetricText}"/></span>
        </c:if>
    </div>
</article>
