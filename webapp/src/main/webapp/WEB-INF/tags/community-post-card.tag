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
<%@ attribute name="repostReview" required="false" type="ar.edu.itba.paw.webapp.controller.CommunityController.RepostReviewView" %>
<%@ attribute name="editable" required="false" type="java.lang.Boolean" %>
<%@ attribute name="hideable" required="false" type="java.lang.Boolean" %>
<%@ attribute name="communitySlug" required="false" %>
<%@ attribute name="postSlug" required="false" %>
<%@ attribute name="actionRedirect" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="replyCountText" code="communities.post.metric.replies" arguments="${commentCount}"/>
<spring:message var="postMetricsAria" code="communities.post.metrics.aria"/>
<spring:message var="postActionMenuLabel" code="activity.card.actionMenu.label"/>
<spring:message var="postHideLabel" code="communities.post.hideAction"/>
<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

<c:set var="showActionMenu" value="${editable or hideable}"/>
<c:if test="${editable}">
    <c:url var="postEditUrl" value="/communities/${communitySlug}/posts/${postSlug}/edit">
        <c:if test="${not empty actionRedirect}">
            <c:param name="redirect" value="${actionRedirect}"/>
        </c:if>
    </c:url>
    <c:url var="postDeleteUrl" value="/communities/${communitySlug}/posts/${postSlug}/delete"/>
</c:if>
<c:if test="${hideable}">
    <c:url var="postHideUrl" value="/communities/${communitySlug}/posts/${postSlug}/hide"/>
</c:if>

<c:choose>
    <c:when test="${showActionMenu}">
        <article class="community-post-card community-post-card-linkable"
                 data-profile-card-link="${fn:escapeXml(href)}"
                 role="link"
                 tabindex="0"${not empty postId ? ' id="post-'.concat(postId).concat('"') : ''}>
            <div class="community-post-topline">
                <p class="community-post-meta">
                    <strong><c:out value="${author}"/></strong>
                    <span aria-hidden="true">•</span>
                    <span><c:out value="${timeText}"/></span>
                </p>
                <div class="community-post-topline-actions">
                    <div class="community-post-card-actions" aria-label="${fn:escapeXml(postMetricsAria)}">
                        <pa:review-like-button
                                reviewId="${postId}"
                                action="${helpfulAction}"
                                redirect="${helpfulRedirect}"
                                liked="${helpfulByCurrentUser}"
                                likeCount="${helpfulCount}"
                                disabled="${not authenticated}"
                                intent="community-post-helpful-${postId}"/>
                        <span class="profile-card-metric"><c:out value="${replyCountText}"/></span>
                    </div>
                    <c:if test="${showActionMenu}">
                        <pa:action-menu label="${postActionMenuLabel}" cssClass="community-post-menu">
                            <c:if test="${editable}">
                                <a href="${fn:escapeXml(postEditUrl)}">
                                    <spring:message code="common.action.edit"/>
                                </a>
                            </c:if>
                            <c:if test="${hideable}">
                                <button type="button"
                                        class="action-menu-danger"
                                        data-open-community-hide-modal
                                        data-community-hide-modal-target="hideCommunityPostModal"
                                        data-community-hide-action="${fn:escapeXml(postHideUrl)}"
                                        data-community-hide-redirect="${fn:escapeXml(actionRedirect)}">
                                    <c:out value="${postHideLabel}"/>
                                </button>
                            </c:if>
                            <c:if test="${editable}">
                                <form method="post" action="${fn:escapeXml(postDeleteUrl)}"
                                      data-confirm-modal="deletePostConfirmModal">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <c:if test="${not empty actionRedirect}">
                                        <input type="hidden" name="redirect" value="${fn:escapeXml(actionRedirect)}">
                                    </c:if>
                                    <button type="submit" class="action-menu-danger">
                                        <spring:message code="communities.post.deleteAction"/>
                                    </button>
                                </form>
                            </c:if>
                        </pa:action-menu>
                    </c:if>
                </div>
            </div>

            <c:if test="${not empty communityName}">
                <p class="profile-card-context"><c:out value="${communityName}"/></p>
            </c:if>
            <h3 class="community-post-title">
                <a href="${fn:escapeXml(href)}"><c:out value="${title}"/></a>
            </h3>
            <p class="community-post-body"><c:out value="${body}"/></p>
            <pa:image-gallery imageUrls="${imageUrls}" altKey="communities.post.image.alt"/>
        </article>
    </c:when>
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
                    <span class="profile-card-metric"><c:out value="${replyCountText}"/></span>
                </div>
            </div>

            <c:if test="${not empty communityName}">
                <p class="profile-card-context"><c:out value="${communityName}"/></p>
            </c:if>
            <h3 class="community-post-title"><c:out value="${title}"/></h3>
            <p class="community-post-body"><c:out value="${body}"/></p>
            <pa:image-gallery imageUrls="${imageUrls}" altKey="communities.post.image.alt"/>
            <c:if test="${not empty repostReview}">
                <pa:reposted-review-card repostReview="${repostReview}" linked="${false}"/>
            </c:if>
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
                    <span class="profile-card-metric"><c:out value="${replyCountText}"/></span>
                </div>
            </div>

            <h3 class="community-post-title"><c:out value="${title}"/></h3>
            <p class="community-post-body"><c:out value="${body}"/></p>
            <pa:image-gallery imageUrls="${imageUrls}" altKey="communities.post.image.alt"/>
            <c:if test="${not empty repostReview}">
                <pa:reposted-review-card repostReview="${repostReview}"/>
            </c:if>
        </article>
    </c:otherwise>
</c:choose>
