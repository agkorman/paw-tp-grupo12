<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="replyCard" required="true" type="ar.edu.itba.paw.webapp.controller.ProfileController.ProfileLikedReplyCard" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="replyReviewUrl" value="/reviews">
    <c:param name="carId" value="${replyCard.carId}"/>
</c:url>
<c:set var="replyReviewHref" value="${replyReviewUrl}#reply-${replyCard.reply.id}"/>
<c:url var="replyLikeUrl" value="/reviews/replies/${replyCard.reply.id}/like"/>
<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

<article class="profile-liked-reply-card" data-profile-card-link="${fn:escapeXml(replyReviewHref)}" role="link" tabindex="0">
    <div class="profile-liked-reply-meta">
        <span>Respuesta likeada</span>
        <span><c:out value="${replyCard.timeAgo}"/></span>
    </div>

    <p class="profile-liked-reply-body"><c:out value="${replyCard.reply.body}"/></p>

    <div class="profile-liked-reply-context">
        <a href="${replyReviewHref}">
            <c:out value="${replyCard.carName}"/>
        </a>
        <span>en</span>
        <strong><c:out value="${replyCard.parentReviewTitle}"/></strong>
    </div>

    <div class="profile-liked-reply-actions">
        <pa:review-like-button
                reviewId="${replyCard.reply.id}"
                action="${replyLikeUrl}"
                liked="${replyCard.liked}"
                likeCount="${replyCard.likeCount}"
                disabled="${not authenticated}"
                label="Like"/>
    </div>
</article>
