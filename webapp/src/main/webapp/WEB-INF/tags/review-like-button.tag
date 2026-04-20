<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewId" required="true" %>
<%@ attribute name="liked" required="false" %>
<%@ attribute name="likeCount" required="false" %>
<%@ attribute name="disabled" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<button
        type="button"
        class="review-like-toggle ${liked ? 'is-active' : ''}"
        data-review-like-toggle
        data-review-id="${fn:escapeXml(reviewId)}"
        data-liked="${liked}"
        aria-pressed="${liked}"
        aria-label="${liked ? 'Quitar like de la review' : 'Likear review'}"
        <c:if test="${disabled}">disabled</c:if>>
    <svg width="17" height="17" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M12 21s-6.8-4.2-9.5-8.4C0.6 9.5 2.4 5 6.2 5c2 0 3.5 1 4.4 2.3C11.5 6 13 5 15 5c3.8 0 5.6 4.5 3.7 7.6C16.8 16.8 12 21 12 21z"/>
    </svg>
    <span class="review-like-label">Like</span>
    <span class="review-like-count" data-review-like-count><c:out value="${empty likeCount ? 0 : likeCount}"/></span>
</button>
