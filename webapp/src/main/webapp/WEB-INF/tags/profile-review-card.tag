<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewCard" required="true" type="ar.edu.itba.paw.webapp.controller.UserController.ProfileReviewCard" %>
<%@ attribute name="editable" required="true" type="java.lang.Boolean" %>
<%@ attribute name="actionRedirect" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="profileReviewUrl" value="/reviews/car/${reviewCard.review.carId}"/>
<c:set var="profileReviewHref" value="${profileReviewUrl}#review-${reviewCard.review.id}"/>
<c:url var="reviewEditPageUrl" value="/reviews/${reviewCard.review.id}/edit">
    <c:if test="${not empty actionRedirect}">
        <c:param name="redirect" value="${actionRedirect}"/>
    </c:if>
</c:url>
<c:url var="reviewDeleteUrl" value="/reviews/${reviewCard.review.id}/delete"/>
<c:url var="reviewLikeUrl" value="/reviews/${reviewCard.review.id}/like"/>
<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
<spring:message var="reviewActionMenuLabel" code="review.actionMenu.open"/>
<spring:message var="reviewRepostLabel" code="review.action.repost"/>
<c:url var="reviewRepostUrl" value="/reviews/${reviewCard.review.id}/repost"/>
<c:url var="reviewRepostLoginUrl" value="/login">
    <c:param name="redirect" value="/reviews/${reviewCard.review.id}/repost"/>
</c:url>
<spring:message var="replyCountText" code="profile.card.metric.replies" arguments="${reviewCard.replyCount}"/>
<spring:message var="likeCountText" code="profile.card.metric.likes" arguments="${reviewCard.likeCount}"/>
<spring:message var="reviewMetricsAria" code="profile.card.metrics.aria"/>

<article class="profile-review-card" data-profile-card-link="${fn:escapeXml(profileReviewHref)}" role="link" tabindex="0">
    <a class="profile-review-image" href="${profileReviewHref}">
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
            <span class="profile-review-date"><c:out value="${relativeTimeFormatter.format(reviewCard.review.createdAt)}"/></span>
            <div class="profile-review-actions">
                <pa:review-like-button
                        reviewId="${reviewCard.review.id}"
                        action="${reviewLikeUrl}"
                        redirect="${actionRedirect}"
                        liked="${reviewCard.liked}"
                        likeCount="${reviewCard.likeCount}"
                        disabled="${not authenticated}"/>
                <span class="profile-card-metric"><c:out value="${replyCountText}"/></span>
                <div class="profile-review-menu-slot">
                    <c:choose>
                        <c:when test="${editable}">
                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="profile-review-menu">
                                <a href="${reviewEditPageUrl}">
                                    <spring:message code="common.action.edit"/>
                                </a>
                                <c:if test="${authenticated}">
                                    <a href="${fn:escapeXml(reviewRepostUrl)}">
                                        <c:out value="${reviewRepostLabel}"/>
                                    </a>
                                </c:if>
                                <form method="post" action="${fn:escapeXml(reviewDeleteUrl)}"
                                      data-confirm-modal="deleteReviewConfirmModal">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <c:if test="${not empty actionRedirect}">
                                        <input type="hidden" name="redirect" value="${fn:escapeXml(actionRedirect)}">
                                    </c:if>
                                    <button type="submit" class="action-menu-danger">
                                        <spring:message code="common.action.delete"/>
                                    </button>
                                </form>
                            </pa:action-menu>
                        </c:when>
                        <c:when test="${authenticated}">
                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="profile-review-menu">
                                <a href="${fn:escapeXml(reviewRepostUrl)}">
                                    <c:out value="${reviewRepostLabel}"/>
                                </a>
                            </pa:action-menu>
                        </c:when>
                        <c:otherwise>
                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="profile-review-menu">
                                <a href="${fn:escapeXml(reviewRepostLoginUrl)}">
                                    <c:out value="${reviewRepostLabel}"/>
                                </a>
                            </pa:action-menu>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <div class="profile-review-context-row">
            <p class="profile-card-context"><c:out value="${reviewCard.carName}"/></p>
            <span class="card-rating-badge profile-review-inline-rating">
                <pa:icon name="star-filled" size="12"/>
                <span class="card-rating-value"><c:out value="${reviewCard.review.rating}"/></span>
            </span>
        </div>
        <h3><c:out value="${reviewCard.review.title}"/></h3>
        <p class="profile-card-body"><c:out value="${reviewCard.review.body}"/></p>
        <pa:review-tag-chips mode="display" tags="${reviewCard.review.tags}"/>
    </div>
</article>
