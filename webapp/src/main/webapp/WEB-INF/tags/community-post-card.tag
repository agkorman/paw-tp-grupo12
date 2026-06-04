<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="author" required="true" %>
<%@ attribute name="timeText" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="body" required="true" %>
<%@ attribute name="imageUrls" required="false" type="java.util.List" %>
<%@ attribute name="helpfulCount" required="true" type="java.lang.Long" %>
<%@ attribute name="commentCount" required="true" type="java.lang.Long" %>
<%@ attribute name="href" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="helpfulCountText" code="communities.post.metric.helpful" arguments="${helpfulCount}"/>
<spring:message var="commentCountText" code="communities.post.metric.comments" arguments="${commentCount}"/>
<spring:message var="postMetricsAria" code="communities.post.metrics.aria"/>

<c:choose>
    <c:when test="${not empty href}">
        <a class="community-post-card community-post-card-link" href="${fn:escapeXml(href)}">
            <div class="community-post-topline">
                <p class="community-post-meta">
                    <strong><c:out value="${author}"/></strong>
                    <span aria-hidden="true">•</span>
                    <span><c:out value="${timeText}"/></span>
                </p>
            </div>

            <h3 class="community-post-title"><c:out value="${title}"/></h3>
            <p class="community-post-body"><c:out value="${body}"/></p>
            <pa:image-gallery imageUrls="${imageUrls}" altKey="communities.post.image.alt"/>

            <div class="community-post-metrics" aria-label="${fn:escapeXml(postMetricsAria)}">
                <span class="community-post-metric"><c:out value="${helpfulCountText}"/></span>
                <span class="community-post-metric"><c:out value="${commentCountText}"/></span>
            </div>
        </a>
    </c:when>
    <c:otherwise>
        <article class="community-post-card">
            <div class="community-post-topline">
                <p class="community-post-meta">
                    <strong><c:out value="${author}"/></strong>
                    <span aria-hidden="true">•</span>
                    <span><c:out value="${timeText}"/></span>
                </p>
            </div>

            <h3 class="community-post-title"><c:out value="${title}"/></h3>
            <p class="community-post-body"><c:out value="${body}"/></p>
            <pa:image-gallery imageUrls="${imageUrls}" altKey="communities.post.image.alt"/>

            <div class="community-post-metrics" aria-label="${fn:escapeXml(postMetricsAria)}">
                <span class="community-post-metric"><c:out value="${helpfulCountText}"/></span>
                <span class="community-post-metric"><c:out value="${commentCountText}"/></span>
            </div>
        </article>
    </c:otherwise>
</c:choose>
