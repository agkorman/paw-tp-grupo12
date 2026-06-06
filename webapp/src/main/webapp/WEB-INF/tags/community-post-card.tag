<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="author" required="true" %>
<%@ attribute name="timeText" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="body" required="true" %>
<%@ attribute name="imageUrls" required="false" type="java.util.List" %>
<%@ attribute name="helpfulCount" required="true" type="java.lang.Long" %>
<%@ attribute name="commentCount" required="true" type="java.lang.Long" %>
<%@ attribute name="href" required="false" %>
<%@ attribute name="communityName" required="false" %>
<%@ attribute name="postId" required="false" type="java.lang.Long" %>
<%@ attribute name="helpfulAction" required="false" %>
<%@ attribute name="helpfulByCurrentUser" required="false" type="java.lang.Boolean" %>
<%@ attribute name="helpfulRedirect" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="commentCountText" code="communities.post.metric.comments" arguments="${commentCount}"/>
<spring:message var="postMetricsAria" code="communities.post.metrics.aria"/>
<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

<c:choose>
    <c:when test="${not empty href}">
        <a class="community-post-card community-post-card-link" href="${fn:escapeXml(href)}"${not empty postId ? ' id="post-'.concat(postId).concat('"') : ''}>
            <div class="community-post-topline">
                <p class="community-post-meta">
                    <strong><c:out value="${author}"/></strong>
                    <span aria-hidden="true">•</span>
                    <span><c:out value="${timeText}"/></span>
                </p>
                <div class="community-post-card-actions" aria-label="${fn:escapeXml(postMetricsAria)}">
                    <pa:review-like-button
                            reviewId="${postId}"
                            action="${helpfulAction}"
                            redirect="${helpfulRedirect}"
                            liked="${helpfulByCurrentUser}"
                            likeCount="${helpfulCount}"
                            disabled="${not authenticated}"
                            intent="community-post-helpful-${postId}"/>
                    <span class="profile-card-metric"><c:out value="${commentCountText}"/></span>
                </div>
            </div>

            <c:if test="${not empty communityName}">
                <p class="profile-card-context"><c:out value="${communityName}"/></p>
            </c:if>
            <h3 class="community-post-title"><c:out value="${title}"/></h3>
            <p class="community-post-body"><c:out value="${body}"/></p>
            <pa:image-gallery imageUrls="${imageUrls}" altKey="communities.post.image.alt"/>
        </a>
    </c:when>
    <c:otherwise>
        <article class="community-post-card"${not empty postId ? ' id="post-'.concat(postId).concat('"') : ''}>
            <div class="community-post-topline">
                <p class="community-post-meta">
                    <strong><c:out value="${author}"/></strong>
                    <span aria-hidden="true">•</span>
                    <span><c:out value="${timeText}"/></span>
                </p>
                <div class="community-post-card-actions" aria-label="${fn:escapeXml(postMetricsAria)}">
                    <pa:review-like-button
                            reviewId="${postId}"
                            action="${helpfulAction}"
                            redirect="${helpfulRedirect}"
                            liked="${helpfulByCurrentUser}"
                            likeCount="${helpfulCount}"
                            disabled="${not authenticated}"
                            intent="community-post-helpful-${postId}"/>
                    <span class="profile-card-metric"><c:out value="${commentCountText}"/></span>
                </div>
            </div>

            <h3 class="community-post-title"><c:out value="${title}"/></h3>
            <p class="community-post-body"><c:out value="${body}"/></p>
            <pa:image-gallery imageUrls="${imageUrls}" altKey="communities.post.image.alt"/>
        </article>
    </c:otherwise>
</c:choose>
