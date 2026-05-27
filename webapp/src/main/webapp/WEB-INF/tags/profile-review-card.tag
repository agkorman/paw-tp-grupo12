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
                <span class="card-rating-row profile-review-rating-row">
                    <span class="card-rating-badge">
                        <pa:icon name="star-filled" size="12"/>
                        <span class="card-rating-value"><c:out value="${reviewCard.review.rating}"/></span>
                    </span>
                </span>
                <pa:review-like-button
                        reviewId="${reviewCard.review.id}"
                        action="${reviewLikeUrl}"
                        redirect="${actionRedirect}"
                        liked="${reviewCard.liked}"
                        likeCount="${reviewCard.likeCount}"
                        disabled="${not authenticated}"/>
                <div class="profile-review-menu-slot">
                    <c:if test="${editable}">
                        <pa:action-menu label="${reviewActionMenuLabel}" cssClass="profile-review-menu">
                            <a href="${reviewEditPageUrl}">
                                <spring:message code="common.action.edit"/>
                            </a>
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
                    </c:if>
                </div>
            </div>
        </div>
        <p class="profile-review-car"><c:out value="${reviewCard.carName}"/></p>
        <h3><c:out value="${reviewCard.review.title}"/></h3>
        <p class="profile-review-body"><c:out value="${reviewCard.review.body}"/></p>
        <pa:review-tag-chips mode="display" tags="${reviewCard.review.tags}"/>
    </div>
</article>
