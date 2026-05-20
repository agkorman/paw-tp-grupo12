<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="authorCode" required="true" %>
<%@ attribute name="timeCode" required="true" %>
<%@ attribute name="typeCode" required="true" %>
<%@ attribute name="typeClass" required="true" %>
<%@ attribute name="titleCode" required="true" %>
<%@ attribute name="bodyCode" required="true" %>
<%@ attribute name="helpfulCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="commentCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="href" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="helpfulCountText" code="communities.post.metric.helpful" arguments="${helpfulCount}"/>
<spring:message var="commentCountText" code="communities.post.metric.comments" arguments="${commentCount}"/>
<spring:message var="postMetricsAria" code="communities.post.metrics.aria"/>

<c:choose>
    <c:when test="${not empty href}">
        <a class="community-post-card community-post-card-link" href="${fn:escapeXml(href)}">
            <div class="community-post-topline">
                <div class="community-post-avatar" aria-hidden="true"></div>
                <p class="community-post-meta">
                    <strong><spring:message code="${authorCode}"/></strong>
                    <span aria-hidden="true">•</span>
                    <span><spring:message code="${timeCode}"/></span>
                </p>
            </div>

            <span class="community-post-type ${fn:escapeXml(typeClass)}">
                <spring:message code="${typeCode}"/>
            </span>

            <h3 class="community-post-title"><spring:message code="${titleCode}"/></h3>
            <p class="community-post-body"><spring:message code="${bodyCode}"/></p>

            <div class="community-post-metrics" aria-label="${postMetricsAria}">
                <span class="community-post-metric"><c:out value="${helpfulCountText}"/></span>
                <span class="community-post-metric"><c:out value="${commentCountText}"/></span>
            </div>
        </a>
    </c:when>
    <c:otherwise>
        <article class="community-post-card">
            <div class="community-post-topline">
                <div class="community-post-avatar" aria-hidden="true"></div>
                <p class="community-post-meta">
                    <strong><spring:message code="${authorCode}"/></strong>
                    <span aria-hidden="true">•</span>
                    <span><spring:message code="${timeCode}"/></span>
                </p>
            </div>

            <span class="community-post-type ${fn:escapeXml(typeClass)}">
                <spring:message code="${typeCode}"/>
            </span>

            <h3 class="community-post-title"><spring:message code="${titleCode}"/></h3>
            <p class="community-post-body"><spring:message code="${bodyCode}"/></p>

            <div class="community-post-metrics" aria-label="${postMetricsAria}">
                <span class="community-post-metric"><c:out value="${helpfulCountText}"/></span>
                <span class="community-post-metric"><c:out value="${commentCountText}"/></span>
            </div>
        </article>
    </c:otherwise>
</c:choose>
