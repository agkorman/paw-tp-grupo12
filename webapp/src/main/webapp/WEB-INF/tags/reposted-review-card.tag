<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="repostReview" required="true" type="ar.edu.itba.paw.webapp.controller.CommunityController.RepostReviewView" %>
<%@ attribute name="linked" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:if test="${empty linked}">
    <c:set var="linked" value="${true}"/>
</c:if>
<c:url var="repostedReviewHref" value="${repostReview.reviewHref}"/>
<c:url var="repostedCarImageUrl" value="/car-image">
    <c:param name="carId" value="${repostReview.carId}"/>
</c:url>
<spring:message var="repostedReviewLabel" code="communities.repostedReview.label"/>
<spring:message var="repostedReviewViewFull" code="communities.repostedReview.viewFull"/>

<div class="reposted-review-card">
    <div class="reposted-review-header">
        <span class="reposted-review-badge">
            <pa:icon name="repeat" size="14"/>
            <c:out value="${repostedReviewLabel}"/>
        </span>
    </div>
    <div class="reposted-review-content">
        <c:choose>
            <c:when test="${linked}">
                <a class="reposted-review-image" href="${fn:escapeXml(repostedReviewHref)}">
                    <img src="${repostedCarImageUrl}" alt="${fn:escapeXml(repostReview.carName)}">
                </a>
            </c:when>
            <c:otherwise>
                <div class="reposted-review-image">
                    <img src="${repostedCarImageUrl}" alt="${fn:escapeXml(repostReview.carName)}">
                </div>
            </c:otherwise>
        </c:choose>
        <div class="reposted-review-body">
            <div class="reposted-review-context">
                <c:if test="${not empty repostReview.carName}">
                    <span class="reposted-review-car"><c:out value="${repostReview.carName}"/></span>
                </c:if>
                <c:if test="${repostReview.rating != null}">
                    <span class="card-rating-badge reposted-review-inline-rating">
                        <pa:icon name="star-filled" size="12"/>
                        <span class="card-rating-value"><c:out value="${repostReview.rating}"/></span>
                    </span>
                </c:if>
            </div>
            <h4 class="reposted-review-title"><c:out value="${repostReview.title}"/></h4>
            <p class="reposted-review-text"><c:out value="${repostReview.body}"/></p>
            <c:if test="${not empty repostReview.authorName}">
                <span class="reposted-review-author"><c:out value="${repostReview.authorName}"/></span>
            </c:if>
            <pa:review-tag-chips mode="display-flat" tags="${repostReview.tags}"/>
        </div>
    </div>
    <c:choose>
        <c:when test="${linked}">
            <a class="reposted-review-link" href="${fn:escapeXml(repostedReviewHref)}">
                <c:out value="${repostedReviewViewFull}"/>
                <pa:icon name="chevron-right" size="14"/>
            </a>
        </c:when>
        <c:otherwise>
            <span class="reposted-review-link">
                <c:out value="${repostedReviewViewFull}"/>
                <pa:icon name="chevron-right" size="14"/>
            </span>
        </c:otherwise>
    </c:choose>
</div>
