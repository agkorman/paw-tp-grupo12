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
<c:if test="${reviewCard.hasCarImage}">
    <c:url var="activityCarImageUrl" value="/car-image">
        <c:param name="carId" value="${reviewCard.review.carId}"/>
    </c:url>
</c:if>
<spring:message var="activityReviewAria" code="activity.review.view.aria" arguments="${reviewCard.carName}"/>
<c:set var="activityPreviewModalId" value="${idPrefix}-${reviewCard.review.id}"/>

<a class="activity-review-card ${reviewCard.hasCarImage ? 'has-image' : ''}"
   href="${activityReviewHref}"
   data-activity-review-card
   data-activity-preview-target="${fn:escapeXml(activityPreviewModalId)}"
   aria-controls="${fn:escapeXml(activityPreviewModalId)}"
   aria-label="${fn:escapeXml(activityReviewAria)}">
    <c:if test="${reviewCard.hasCarImage}">
        <span class="activity-review-card-media" aria-hidden="true">
            <img src="${activityCarImageUrl}" alt="">
        </span>
    </c:if>
    <span class="activity-review-author">
        <span class="activity-review-author-copy">
            <strong><c:out value="${reviewCard.authorName}"/></strong>
            <span><c:out value="${relativeTimeFormatter.format(reviewCard.review.createdAt)}"/></span>
        </span>
    </span>

    <span class="activity-review-copy">
        <span class="activity-review-car-row">
            <span class="activity-review-car"><c:out value="${reviewCard.carName}"/></span>
                <span class="card-rating-row activity-review-rating-row">
                    <span class="card-rating-badge">
                    <pa:icon name="star-filled" size="12"/>
                    <span class="card-rating-value"><c:out value="${reviewCard.review.rating}"/></span>
                </span>
            </span>
        </span>
        <strong class="activity-review-title"><c:out value="${reviewCard.review.title}"/></strong>
        <span class="activity-review-body"><c:out value="${reviewCard.review.body}"/></span>
    </span>
</a>
