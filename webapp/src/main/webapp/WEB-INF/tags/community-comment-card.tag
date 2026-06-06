<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="author" required="true" %>
<%@ attribute name="authorProfileHref" required="true" %>
<%@ attribute name="timeText" required="true" %>
<%@ attribute name="body" required="true" %>
<%@ attribute name="commentId" required="true" %>
<%@ attribute name="helpfulCount" required="true" type="java.lang.Long" %>
<%@ attribute name="helpfulByCurrentUser" required="false" type="java.lang.Boolean" %>
<%@ attribute name="helpfulAction" required="false" type="java.lang.String" %>
<%@ attribute name="helpfulDisabled" required="false" %>
<%@ attribute name="helpfulIntent" required="false" type="java.lang.String" %>
<%@ attribute name="isOp" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<article class="community-comment">
    <div class="community-comment-header">
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
                liked="${helpfulByCurrentUser}"
                likeCount="${helpfulCount}"
                action="${helpfulAction}"
                disabled="${helpfulDisabled}"
                intent="${helpfulIntent}"/>
    </div>
</article>
