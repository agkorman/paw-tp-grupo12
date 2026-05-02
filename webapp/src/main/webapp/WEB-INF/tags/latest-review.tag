<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="latestReview" required="false" type="ar.edu.itba.paw.model.Review" %>
<%@ attribute name="liked" required="false" %>
<%@ attribute name="likeCount" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
<spring:message var="latestReviewLabel" code="review.latest.title"/>

<section class="latest-review-section" aria-label="${latestReviewLabel}">
    <h2><spring:message code="review.latest.title"/></h2>
    <c:choose>
        <c:when test="${empty latestReview}">
            <div class="last-review-empty">
                <spring:message code="review.latest.empty"/>
            </div>
        </c:when>
        <c:otherwise>
            <article class="last-review-item">
                <div class="last-review-top">
                    <strong><c:out value="${latestReview.title}"/></strong>
                    <span class="rating-pill"><c:out value="${latestReview.rating}"/>/5.0</span>
                </div>
                <p class="last-review-body"><c:out value="${latestReview.body}"/></p>
                <pa:review-tag-chips mode="display" tags="${latestReview.tags}"/>
                <div class="review-meta last-review-meta">
                    <pa:review-author-link review="${latestReview}"/>
                    <span><c:out value="${relativeTimeFormatter.format(latestReview.createdAt)}"/></span>
                    <c:url var="latestReviewLikeUrl" value="/reviews/${latestReview.id}/like"/>
                    <pa:review-like-button
                            reviewId="${latestReview.id}"
                            action="${latestReviewLikeUrl}"
                            liked="${liked}"
                            likeCount="${likeCount}"
                            disabled="${not authenticated}"/>
                </div>
            </article>
        </c:otherwise>
    </c:choose>
</section>
