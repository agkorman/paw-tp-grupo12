<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
    <c:url var="communityPostHelpfulLoginUrl" value="/login">
        <c:param name="redirect" value="${communityPostDetailUrl}"/>
        <c:param name="intent" value="community-post-helpful-${postDetail.post.id}"/>
    </c:url>
    <c:url var="communityPostCommentLoginUrl" value="/login">
        <c:param name="redirect" value="${communityPostDetailUrl}"/>
        <c:param name="intent" value="community-post-comment-${postDetail.post.id}"/>
    </c:url>
    <spring:message var="communityPostBackLabel" code="communities.postDetail.back"/>
    <spring:message var="communityPostCommentPlaceholder" code="communities.postDetail.comment.placeholder"/>
    <spring:message var="communityPostCommentLabel" code="communities.postDetail.comment.label"/>
    <spring:message var="communityPostCommentSubmitLabel" code="communities.postDetail.comment.submit"/>
    <spring:message var="communityPostCommentRequiredMessage" code="validation.communityPostComment.body.required"/>
    <spring:message var="communityPostCommentMaxMessage" code="validation.communityPostComment.body.max" arguments="1000"/>
    <spring:message var="communityPostCommentLoginPrefix" code="communities.postDetail.comment.loginPrefix"/>
    <spring:message var="communityPostCommentLoginSuffix" code="communities.postDetail.comment.loginSuffix"/>
    <spring:message var="communityPostHelpfulAddLabel" code="communities.postDetail.helpful.add"/>
    <spring:message var="communityPostHelpfulRemoveLabel" code="communities.postDetail.helpful.remove"/>
    <spring:message var="communityPostHelpfulLoginLabel" code="communities.postDetail.helpful.login"/>
    <c:set var="communityPostHelpfulButtonClass" value="community-post-detail-pill community-post-detail-pill-button"/>
    <c:set var="communityPostHelpfulButtonLabel" value="${communityPostHelpfulAddLabel}"/>
    <c:if test="${postView.helpfulByCurrentUser}">
        <c:set var="communityPostHelpfulButtonClass" value="community-post-detail-pill community-post-detail-pill-button is-positive"/>
        <c:set var="communityPostHelpfulButtonLabel" value="${communityPostHelpfulRemoveLabel}"/>
    </c:if>
    <main class="community-post-page">
        <section class="community-post-shell">
            <div class="community-post-header">
                <a class="community-back-link" href="${fn:escapeXml(communityDetailUrl)}" aria-label="${fn:escapeXml(communityPostBackLabel)}">
                    <pa:icon name="chevron-left" size="18"/>
                </a>
                <div class="community-post-origin">
                    <span class="community-post-origin-avatar" aria-hidden="true"><c:out value="${fn:substring(postView.communityName, 0, 1)}"/></span>
                    <div>
                        <p class="community-post-origin-line">
                            <strong><c:out value="${postView.communityHandle}"/></strong>
                            <span aria-hidden="true">•</span>
                            <span><c:out value="${postView.timeText}"/></span>
                        </p>
                        <p class="community-post-origin-author"><c:out value="${postView.author}"/></p>
                    </div>
                </div>
            </div>

            <article class="community-post-detail-card">
                <h1><c:out value="${postView.title}"/></h1>
                <p class="community-post-detail-body"><c:out value="${postView.body}"/></p>

                <div class="community-post-detail-actions">
                    <spring:message var="postHelpfulText" code="communities.post.metric.helpful" arguments="${postView.helpfulCount}"/>
                    <spring:message var="postCommentCountText" code="communities.post.metric.comments" arguments="${postView.commentCount}"/>
                    <sec:authorize access="isAuthenticated()">
                        <form method="post" action="${fn:escapeXml(communityPostHelpfulUrl)}" class="community-post-detail-reaction-form">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit"
                                    class="${communityPostHelpfulButtonClass}"
                                    aria-pressed="${postView.helpfulByCurrentUser}"
                                    aria-label="${fn:escapeXml(communityPostHelpfulButtonLabel)}">
                                <c:out value="${postHelpfulText}"/>
                            </button>
                        </form>
                    </sec:authorize>
                    <sec:authorize access="!isAuthenticated()">
                        <a href="${fn:escapeXml(communityPostHelpfulLoginUrl)}"
                           class="community-post-detail-pill community-post-detail-pill-button"
                           aria-label="${fn:escapeXml(communityPostHelpfulLoginLabel)}">
                            <c:out value="${postHelpfulText}"/>
                        </a>
                    </sec:authorize>
                    <span class="community-post-detail-pill"><c:out value="${postCommentCountText}"/></span>
                </div>
            </article>

            <sec:authorize access="isAuthenticated()">
                <c:set var="commentHasError" value="${not empty commentError}"/>
                <c:set var="communityPostCommentBodyClass" value=""/>
                <c:if test="${commentHasError}">
                    <c:set var="communityPostCommentBodyClass" value="is-invalid"/>
                </c:if>
                <form method="post" action="${fn:escapeXml(communityPostCommentCreateUrl)}" class="community-comment-composer" novalidate="novalidate">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <label for="communityPostComment"><c:out value="${communityPostCommentLabel}"/></label>
                    <div class="community-comment-composer-row">
                        <textarea id="communityPostComment"
                                  name="body"
                                  rows="2"
                                  maxlength="1000"
                                  required="required"
                                  placeholder="${fn:escapeXml(communityPostCommentPlaceholder)}"
                                  class="${communityPostCommentBodyClass}"><c:out value="${commentErrorBody}"/></textarea>
                        <button type="submit" class="btn-secondary"><c:out value="${communityPostCommentSubmitLabel}"/></button>
                    </div>
                    <c:choose>
                        <c:when test="${commentHasError}">
                            <span class="community-comment-inline-error client-form-error">
                                <c:choose>
                                    <c:when test="${commentError eq 'validation.communityPostComment.body.max'}"><c:out value="${communityPostCommentMaxMessage}"/></c:when>
                                    <c:otherwise><c:out value="${communityPostCommentRequiredMessage}"/></c:otherwise>
                                </c:choose>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="community-comment-inline-error client-form-error" hidden></span>
                        </c:otherwise>
                    </c:choose>
                </form>
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
