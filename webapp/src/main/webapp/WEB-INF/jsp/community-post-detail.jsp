<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${pageTitle}" styles="/css/community-post-common.css|/css/community-post-detail.css|/css/communities-responsive.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="communityDetailUrl" value="/communities/${postDetail.community.slug}"/>
    <c:url var="communityPostDetailUrl" value="/communities/${postDetail.community.slug}/posts/${postDetail.post.slug}"/>
    <c:url var="communityPostHelpfulUrl" value="/communities/${postDetail.community.slug}/posts/${postDetail.post.slug}/helpful"/>
    <c:url var="communityPostCommentCreateUrl" value="/communities/${postDetail.community.slug}/posts/${postDetail.post.slug}/comments"/>
    <c:url var="communityPostCommentLoginUrl" value="/login">
        <c:param name="redirect" value="${communityPostDetailUrl}"/>
        <c:param name="intent" value="community-post-comment-${postDetail.post.id}"/>
    </c:url>
    <spring:message var="communityPostBackLabel" code="communities.postDetail.back"/>
    <spring:message var="communityPostCommentPlaceholder" code="communities.postDetail.comment.placeholder"/>
    <spring:message var="communityPostCommentLoginPrefix" code="communities.postDetail.comment.loginPrefix"/>
    <spring:message var="communityPostCommentLoginSuffix" code="communities.postDetail.comment.loginSuffix"/>
    <main class="community-post-page">
        <section class="community-post-shell">
            <div class="community-post-header">
                <a class="community-back-link" href="${fn:escapeXml(communityDetailUrl)}" aria-label="${fn:escapeXml(communityPostBackLabel)}">
                    <pa:icon name="chevron-left" size="18"/>
                </a>
                <div class="community-post-origin">
                    <div>
                        <p class="community-post-origin-line">
                            <strong><c:out value="${postView.communityHandle}"/></strong>
                            <span aria-hidden="true">•</span>
                            <span><c:out value="${postView.timeText}"/></span>
                        </p>
                        <a class="community-post-origin-author" href="${fn:escapeXml(postView.authorProfileHref)}">
                            <c:out value="${postView.author}"/>
                        </a>
                    </div>
                </div>
            </div>

            <article class="community-post-detail-card">
                <h1><c:out value="${postView.title}"/></h1>
                <p class="community-post-detail-body"><c:out value="${postView.body}"/></p>

                <div class="community-post-detail-actions">
                    <spring:message var="postCommentCountText" code="communities.post.metric.comments" arguments="${postView.commentCount}"/>
                    <pa:review-like-button
                            reviewId="${postDetail.post.id}"
                            liked="${postView.helpfulByCurrentUser}"
                            likeCount="${postView.helpfulCount}"
                            action="${communityPostHelpfulUrl}"
                            disabled="${empty pageContext.request.userPrincipal}"
                            intent="community-post-helpful-${postDetail.post.id}"/>
                    <span class="community-post-detail-pill"><c:out value="${postCommentCountText}"/></span>
                </div>
            </article>

            <sec:authorize access="isAuthenticated()">
                <form:form method="post"
                           action="${fn:escapeXml(communityPostCommentCreateUrl)}"
                           modelAttribute="communityPostCommentForm"
                           cssClass="community-comment-composer"
                           novalidate="novalidate">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <label for="communityPostComment"><spring:message code="communities.postDetail.comment.label"/></label>
                    <div class="community-comment-composer-row">
                        <form:textarea id="communityPostComment"
                                       path="body"
                                       rows="2"
                                       maxlength="1000"
                                       required="required"
                                       placeholder="${fn:escapeXml(communityPostCommentPlaceholder)}"/>
                        <button type="submit" class="btn-secondary"><spring:message code="communities.postDetail.comment.submit"/></button>
                    </div>
                    <form:errors path="body" cssClass="community-comment-inline-error client-form-error" element="span"/>
                </form:form>
            </sec:authorize>
            <sec:authorize access="!isAuthenticated()">
                <p class="community-comment-login">
                    <a href="${fn:escapeXml(communityPostCommentLoginUrl)}" class="community-comment-login-button"><c:out value="${communityPostCommentLoginPrefix}"/></a>
                    <c:out value="${communityPostCommentLoginSuffix}"/>
                </p>
            </sec:authorize>

            <section class="community-comments-list">
                <c:forEach var="comment" items="${postView.comments}">
                    <pa:community-comment-card
                            authorProfileHref="${comment.authorProfileHref}"
                            author="${comment.author}"
                            timeText="${comment.timeText}"
                            body="${comment.body}"
                            helpfulCount="${comment.helpfulCount}"
                            isOp="${comment.op}"/>
                </c:forEach>
            </section>
        </section>
    </main>
    <pa:footer/>
</body>
</html>
