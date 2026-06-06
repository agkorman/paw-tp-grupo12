<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${pageTitle}" styles="/css/community-post-common.css|/css/community-post-detail.css|/css/communities-responsive.css|/css/profile-modal.css|/css/image-lightbox.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="communityDetailUrl" value="/communities/${postDetail.community.slug}"/>
    <c:set var="communityPostsSectionUrl" value="${communityDetailUrl}#communityFeedTitle"/>
    <c:if test="${not empty postReturnRedirect}">
        <c:set var="communityPostsSectionUrl" value="${postReturnRedirect}"/>
    </c:if>
    <c:url var="communityPostDetailUrl" value="/communities/${postDetail.community.slug}/posts/${postDetail.post.slug}"/>
    <c:url var="communityPostHelpfulUrl" value="/communities/${postDetail.community.slug}/posts/${postDetail.post.slug}/helpful"/>
    <c:url var="communityPostCommentCreateUrl" value="/communities/${postDetail.community.slug}/posts/${postDetail.post.slug}/comments"/>
    <c:url var="communityJoinUrl" value="/communities/${postDetail.community.slug}/join"/>
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
                <a class="community-back-link" href="${fn:escapeXml(communityDetailUrl)}" aria-label="${fn:escapeXml(communityPostBackLabel)}"
                   onclick="if (document.referrer &amp;&amp; document.referrer.indexOf(location.origin) === 0) { history.back(); return false; }">
                    <pa:icon name="chevron-left" size="18"/>
                </a>
                <div class="community-post-origin">
                    <div>
                        <p class="community-post-origin-line">
                            <strong><c:out value="${postView.communityHandle}"/></strong>
                            <span aria-hidden="true">•</span>
                            <span><c:out value="${postView.timeText}"/></span>
                        </p>
                        <c:url var="postAuthorProfileHref" value="${postView.authorProfileHref}"/>
                        <a class="community-post-origin-author" href="${fn:escapeXml(postAuthorProfileHref)}">
                            <c:out value="${postView.author}"/>
                        </a>
                    </div>
                </div>
                <c:if test="${postView.deletable or postView.hideable}">
                    <spring:message var="postModMenuLabel" code="communities.post.modMenu.label"/>
                    <pa:action-menu label="${postModMenuLabel}" cssClass="community-post-mod-menu">
                        <c:if test="${postView.editable}">
                            <c:url var="communityPostEditUrl" value="/communities/${postView.communitySlug}/posts/${postView.postSlug}/edit">
                                <c:param name="redirect" value="${communityPostDetailUrl}"/>
                            </c:url>
                            <a href="${fn:escapeXml(communityPostEditUrl)}">
                                <spring:message code="common.action.edit"/>
                            </a>
                        </c:if>
                        <c:if test="${postView.deletable}">
                            <c:url var="communityPostDeleteUrl" value="/communities/${postView.communitySlug}/posts/${postView.postSlug}/delete"/>
                            <form method="post" action="${fn:escapeXml(communityPostDeleteUrl)}"
                                  data-confirm-modal="deletePostConfirmModal">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <input type="hidden" name="redirect" value="${fn:escapeXml(communityPostsSectionUrl)}">
                                <button type="submit" class="action-menu-danger">
                                    <spring:message code="communities.post.deleteAction"/>
                                </button>
                            </form>
                        </c:if>
                        <c:if test="${postView.hideable}">
                            <c:url var="communityPostHideUrl" value="/communities/${postView.communitySlug}/posts/${postView.postSlug}/hide"/>
                            <button type="button"
                                    class="action-menu-danger"
                                    data-open-community-hide-modal
                                    data-community-hide-modal-target="hideCommunityPostModal"
                                    data-community-hide-action="${fn:escapeXml(communityPostHideUrl)}"
                                    data-community-hide-redirect="${fn:escapeXml(communityPostsSectionUrl)}">
                                <spring:message code="communities.post.hideAction"/>
                            </button>
                        </c:if>
                    </pa:action-menu>
                </c:if>
            </div>

            <article class="community-post-detail-card">
                <h1><c:out value="${postView.title}"/></h1>
                <p class="community-post-detail-body"><c:out value="${postView.body}"/></p>
                <pa:image-gallery imageUrls="${postView.imageUrls}" altKey="communities.post.image.alt"/>

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

            <div id="comments">
            <sec:authorize access="isAuthenticated()">
                <c:choose>
                    <c:when test="${postView.viewerMember}">
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
                    </c:when>
                    <c:otherwise>
                        <form action="${fn:escapeXml(communityJoinUrl)}" method="post" class="community-comment-login">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <span><spring:message code="communities.postDetail.comment.joinRequired"/></span>
                            <button type="submit" class="community-comment-login-button">
                                <spring:message code="communities.postDetail.comment.joinAction"/>
                            </button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </sec:authorize>
            <sec:authorize access="!isAuthenticated()">
                <p class="community-comment-login">
                    <a href="${fn:escapeXml(communityPostCommentLoginUrl)}" class="community-comment-login-button"><c:out value="${communityPostCommentLoginPrefix}"/></a>
                    <c:out value="${communityPostCommentLoginSuffix}"/>
                </p>
            </sec:authorize>
            </div>

            <section class="community-comments-list">
                <spring:message var="commentModMenuLabel" code="communities.comment.modMenu.label"/>
                <c:forEach var="comment" items="${postView.comments}">
                    <div class="community-comment-row" id="comment-${comment.commentId}">
                        <article class="community-comment">
                            <div class="community-comment-header">
                                <div class="community-comment-meta">
                                    <c:url var="commentAuthorProfileHref" value="${comment.authorProfileHref}"/>
                                    <a class="community-author-link" href="${fn:escapeXml(commentAuthorProfileHref)}">
                                        <strong><c:out value="${comment.author}"/></strong>
                                    </a>
                                    <c:if test="${comment.op}">
                                        <span class="community-comment-badge"><spring:message code="communities.postDetail.comment.op"/></span>
                                    </c:if>
                                    <span aria-hidden="true">•</span>
                                    <span><c:out value="${comment.timeText}"/></span>
                                </div>
                            </div>
                            <p class="community-comment-body" data-community-comment-body><c:out value="${comment.body}"/></p>
                            <sec:authorize access="isAuthenticated()">
                                <c:if test="${comment.editable}">
                                    <c:url var="communityCommentUpdateUrl" value="/communities/${postView.communitySlug}/posts/${postView.postSlug}/comments/${comment.commentId}/update"/>
                                    <form method="post"
                                          action="${fn:escapeXml(communityCommentUpdateUrl)}"
                                          class="community-comment-edit-form"
                                          data-community-comment-edit-form
                                          hidden
                                          novalidate="novalidate">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <textarea name="body" rows="2" maxlength="1000" required><c:out value="${comment.body}"/></textarea>
                                        <div class="community-comment-edit-actions">
                                            <button type="button" class="btn-secondary" data-cancel-community-comment-edit>
                                                <spring:message code="common.action.cancel"/>
                                            </button>
                                            <button type="submit" class="btn-primary">
                                                <spring:message code="common.action.save"/>
                                            </button>
                                        </div>
                                    </form>
                                </c:if>
                            </sec:authorize>
                            <c:url var="communityCommentHelpfulUrl" value="/communities/${postView.communitySlug}/posts/${postView.postSlug}/comments/${comment.commentId}/helpful"/>
                            <div class="community-comment-actions">
                                <pa:review-like-button
                                        reviewId="${comment.commentId}"
                                        liked="${comment.helpfulByCurrentUser}"
                                        likeCount="${comment.helpfulCount}"
                                        action="${communityCommentHelpfulUrl}"
                                        disabled="${empty pageContext.request.userPrincipal}"
                                        intent="community-comment-helpful-${comment.commentId}"/>
                            </div>
                        </article>
                        <c:if test="${comment.deletable or comment.hideable}">
                            <div class="community-comment-row-meta">
                                <pa:action-menu label="${commentModMenuLabel}" cssClass="community-comment-mod-menu">
                                    <c:if test="${comment.editable}">
                                        <button type="button" data-edit-community-comment-trigger>
                                            <spring:message code="common.action.edit"/>
                                        </button>
                                    </c:if>
                                    <c:if test="${comment.deletable}">
                                        <c:url var="communityCommentDeleteUrl" value="/communities/${postView.communitySlug}/posts/${postView.postSlug}/comments/${comment.commentId}/delete"/>
                                        <form method="post" action="${fn:escapeXml(communityCommentDeleteUrl)}"
                                              data-confirm-modal="deleteCommentConfirmModal">
                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                            <button type="submit" class="action-menu-danger">
                                                <spring:message code="communities.comment.deleteAction"/>
                                            </button>
                                        </form>
                                    </c:if>
                                    <c:if test="${comment.hideable and not comment.deletable}">
                                        <c:url var="communityCommentHideUrl" value="/communities/${postView.communitySlug}/posts/${postView.postSlug}/comments/${comment.commentId}/hide"/>
                                        <button type="button"
                                                class="action-menu-danger"
                                                data-open-community-hide-modal
                                                data-community-hide-modal-target="hideCommunityCommentModal"
                                                data-community-hide-action="${fn:escapeXml(communityCommentHideUrl)}"
                                                data-community-hide-redirect="${fn:escapeXml(communityPostDetailUrl)}">
                                            <spring:message code="communities.comment.hideAction"/>
                                        </button>
                                    </c:if>
                                </pa:action-menu>
                            </div>
                        </c:if>
                    </div>
                </c:forEach>
            </section>
        </section>
    </main>

    <sec:authorize access="isAuthenticated()">
        <pa:confirmation-modal id="deletePostConfirmModal"
                               titleCode="communities.post.delete.title"
                               bodyCode="communities.post.delete.body"
                               confirmCode="communities.post.deleteAction"
                               confirmCssClass="btn-primary"/>
        <pa:confirmation-modal id="deleteCommentConfirmModal"
                               titleCode="communities.comment.delete.title"
                               bodyCode="communities.comment.delete.body"
                               confirmCode="communities.comment.deleteAction"
                               confirmCssClass="btn-primary"/>
        <c:if test="${postView.moderationAvailable}">
            <pa:community-hide-modal id="hideCommunityPostModal"
                                     titleCode="communities.post.hide.title"
                                     bodyCode="communities.post.hide.body"
                                     confirmCode="communities.post.hideAction"
                                     placeholderCode="communities.post.hide.reason.placeholder"/>
            <pa:community-hide-modal id="hideCommunityCommentModal"
                                     titleCode="communities.comment.hide.title"
                                     bodyCode="communities.comment.hide.body"
                                     confirmCode="communities.comment.hideAction"
                                     placeholderCode="communities.comment.hide.reason.placeholder"/>
        </c:if>
        <pa:script src="/js/shared/action-menu.js"/>
        <pa:script src="/js/shared/confirmation-modal.js"/>
        <pa:script src="/js/communities/community-comment-edit.js"/>
        <pa:script src="/js/communities/community-moderation.js"/>
    </sec:authorize>
    <pa:image-lightbox/>
    <pa:script src="/js/shared/image-lightbox.js" defer="true"/>

    <pa:footer/>
</body>
</html>
