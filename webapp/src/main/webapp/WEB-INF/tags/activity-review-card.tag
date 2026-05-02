<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewCard" required="true" type="ar.edu.itba.paw.webapp.controller.ActivityController.ActivityReviewCard" %>
<%@ attribute name="idPrefix" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="activityReviewUrl" value="/reviews">
    <c:param name="carId" value="${reviewCard.review.carId}"/>
    <c:if test="${reviewCard.reviewPage gt 1}">
        <c:param name="page" value="${reviewCard.reviewPage}"/>
    </c:if>
</c:url>
<c:set var="activityReviewHref" value="${activityReviewUrl}#review-${reviewCard.review.id}"/>
<c:set var="activityReviewStyle" value=""/>
<c:if test="${reviewCard.hasCarImage}">
    <c:url var="activityCarImageUrl" value="/car-image">
        <c:param name="carId" value="${reviewCard.review.carId}"/>
    </c:url>
    <c:set var="activityReviewStyle" value="--activity-car-image: url('${activityCarImageUrl}');"/>
</c:if>
<spring:message var="activityReviewAria" code="activity.review.view.aria" arguments="${reviewCard.carName}"/>
<c:set var="activityPreviewModalId" value="${idPrefix}-${reviewCard.review.id}"/>

<a class="activity-review-card ${reviewCard.hasCarImage ? 'has-image' : ''}"
   href="${activityReviewHref}"
   data-activity-review-card
   data-activity-preview-target="${fn:escapeXml(activityPreviewModalId)}"
   aria-controls="${fn:escapeXml(activityPreviewModalId)}"
   style="${activityReviewStyle}"
   aria-label="${fn:escapeXml(activityReviewAria)}">
    <span class="activity-review-author">
        <span class="activity-review-author-copy">
            <strong><c:out value="${reviewCard.authorName}"/></strong>
            <span><c:out value="${reviewCard.timeAgo}"/></span>
        </span>
    </span>

    <span class="activity-review-copy">
        <span class="activity-review-car-row">
            <span class="activity-review-car"><c:out value="${reviewCard.carName}"/></span>
            <span class="card-rating-row activity-review-rating-row">
                <span class="card-rating-badge">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                        <path d="M12 2.75l2.91 5.9 6.51.95-4.71 4.59 1.11 6.48L12 17.62l-5.82 3.05 1.11-6.48-4.71-4.59 6.51-.95L12 2.75z"/>
                    </svg>
                    <span class="card-rating-value"><c:out value="${reviewCard.review.rating}"/></span>
                </span>
            </span>
        </span>
        <strong class="activity-review-title"><c:out value="${reviewCard.review.title}"/></strong>
        <span class="activity-review-body"><c:out value="${reviewCard.review.body}"/></span>
    </span>
</a>
