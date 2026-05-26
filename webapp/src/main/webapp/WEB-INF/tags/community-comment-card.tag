<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="author" required="true" %>
<%@ attribute name="authorProfileHref" required="true" %>
<%@ attribute name="timeText" required="true" %>
<%@ attribute name="body" required="true" %>
<%@ attribute name="commentId" required="true" %>
<%@ attribute name="helpfulCount" required="true" type="java.lang.Long" %>
<%@ attribute name="isOp" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<article class="community-comment">
    <div class="community-comment-header">
        <span class="community-comment-avatar" aria-hidden="true"></span>
        <div class="community-comment-meta">
            <a class="community-author-link" href="${fn:escapeXml(authorProfileHref)}">
                <strong><c:out value="${author}"/></strong>
            </a>
            <c:if test="${isOp}">
                <span class="community-comment-badge"><spring:message code="communities.postDetail.comment.op"/></span>
            </c:if>
            <span aria-hidden="true">•</span>
            <span><c:out value="${timeText}"/></span>
        </div>
    </div>
    <p class="community-comment-body"><c:out value="${body}"/></p>
    <div class="community-comment-actions">
        <pa:review-like-button
                reviewId="${commentId}"
                liked="false"
                likeCount="${helpfulCount}"
                readonly="true"/>
    </div>
</article>
