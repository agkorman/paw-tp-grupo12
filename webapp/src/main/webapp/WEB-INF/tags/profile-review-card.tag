<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewCard" required="true" type="ar.edu.itba.paw.webapp.controller.ProfileController.ProfileReviewCard" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="profileReviewUrl" value="/reviews">
    <c:param name="carId" value="${reviewCard.review.carId}"/>
</c:url>

<article class="profile-review-card">
    <a class="profile-review-image" href="${profileReviewUrl}">
        <c:choose>
            <c:when test="${reviewCard.hasCarImage}">
                <c:url var="profileReviewImageUrl" value="/car-image">
                    <c:param name="carId" value="${reviewCard.review.carId}"/>
                </c:url>
                <img src="${profileReviewImageUrl}" alt="${fn:escapeXml(reviewCard.carName)}">
            </c:when>
            <c:otherwise>
                <span class="profile-review-image-placeholder" aria-hidden="true"></span>
            </c:otherwise>
        </c:choose>
    </a>

    <div class="profile-review-content">
        <div class="profile-review-meta-row">
            <span class="profile-review-date"><c:out value="${fn:substring(reviewCard.review.createdAt, 0, 10)}"/></span>
            <div class="profile-review-actions">
                <span class="profile-review-score"><c:out value="${reviewCard.review.rating}"/></span>
                <pa:review-like-button
                        reviewId="${reviewCard.review.id}"
                        liked="${reviewCard.liked}"
                        likeCount="${reviewCard.likeCount}"/>
            </div>
        </div>
        <p class="profile-review-car"><c:out value="${reviewCard.carName}"/></p>
        <h3><c:out value="${reviewCard.review.title}"/></h3>
        <p class="profile-review-body"><c:out value="${reviewCard.review.body}"/></p>
    </div>
</article>
