<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="authorCode" required="true" %>
<%@ attribute name="timeCode" required="true" %>
<%@ attribute name="bodyCode" required="true" %>
<%@ attribute name="helpfulCount" required="true" type="java.lang.Integer" %>
<%@ attribute name="isOp" required="false" type="java.lang.Boolean" %>
<%@ attribute name="nested" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="commentHelpfulText" code="communities.post.metric.helpful" arguments="${helpfulCount}"/>

<article class="community-comment${nested ? ' is-nested' : ''}">
    <div class="community-comment-header">
        <span class="community-comment-avatar" aria-hidden="true"></span>
        <div class="community-comment-meta">
            <strong><spring:message code="${authorCode}"/></strong>
            <c:if test="${isOp}">
                <span class="community-comment-badge"><spring:message code="communities.postDetail.comment.op"/></span>
            </c:if>
            <span aria-hidden="true">•</span>
            <span><spring:message code="${timeCode}"/></span>
        </div>
    </div>
    <p class="community-comment-body"><spring:message code="${bodyCode}"/></p>
    <div class="community-comment-actions">
        <span class="community-comment-action"><c:out value="${commentHelpfulText}"/></span>
        <button type="button" class="community-comment-action"><spring:message code="communities.postDetail.comment.reply"/></button>
        <button type="button" class="community-comment-action"><spring:message code="communities.postDetail.comment.share"/></button>
    </div>
</article>
