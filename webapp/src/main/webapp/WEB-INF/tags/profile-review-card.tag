<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewCard" required="true" type="ar.edu.itba.paw.webapp.controller.ProfileController.ProfileReviewCard" %>
<%@ attribute name="editable" required="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="profileReviewUrl" value="/reviews">
    <c:param name="carId" value="${reviewCard.review.carId}"/>
</c:url>
<c:set var="profileReviewHref" value="${profileReviewUrl}#review-${reviewCard.review.id}"/>
<c:url var="reviewEditUrl" value="/reviews/${reviewCard.review.id}"/>
<c:url var="reviewDeleteUrl" value="/reviews/${reviewCard.review.id}/delete"/>
<c:url var="reviewLikeUrl" value="/reviews/${reviewCard.review.id}/like"/>
<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

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
            <span class="profile-review-date"><c:out value="${fn:substring(reviewCard.review.createdAt, 0, 10)}"/></span>
            <div class="profile-review-actions">
                <span class="profile-review-score"><c:out value="${reviewCard.review.rating}"/></span>
                <pa:review-like-button
                        reviewId="${reviewCard.review.id}"
                        action="${reviewLikeUrl}"
                        liked="${reviewCard.liked}"
                        likeCount="${reviewCard.likeCount}"
                        disabled="${not authenticated}"/>
                <c:if test="${editable}">
                    <pa:action-menu label="Abrir opciones de review" cssClass="profile-review-menu">
                        <button
                                type="button"
                                data-open-review-modal="edit"
                                data-review-action="${fn:escapeXml(reviewEditUrl)}"
                                data-review-id="${fn:escapeXml(reviewCard.review.id)}"
                                data-review-car-id="${fn:escapeXml(reviewCard.review.carId)}"
                                data-review-rating="${fn:escapeXml(reviewCard.review.rating)}"
                                data-review-ownership-status="${fn:escapeXml(reviewCard.review.ownershipStatus)}"
                                data-review-title="${fn:escapeXml(reviewCard.review.title)}"
                                data-review-body="${fn:escapeXml(reviewCard.review.body)}"
                                data-review-model-year="${fn:escapeXml(reviewCard.review.modelYear)}"
                                data-review-mileage-km="${fn:escapeXml(reviewCard.review.mileageKm)}"
                                data-review-would-recommend="${fn:escapeXml(reviewCard.review.wouldRecommend)}">
                            Editar
                        </button>
                        <button
                                type="button"
                                class="action-menu-danger"
                                data-open-delete-review-modal
                                data-review-delete-action="${fn:escapeXml(reviewDeleteUrl)}"
                                data-review-title="${fn:escapeXml(reviewCard.review.title)}">
                            Eliminar
                        </button>
                    </pa:action-menu>
                </c:if>
            </div>
        </div>
        <p class="profile-review-car"><c:out value="${reviewCard.carName}"/></p>
        <h3><c:out value="${reviewCard.review.title}"/></h3>
        <p class="profile-review-body"><c:out value="${reviewCard.review.body}"/></p>
        <pa:review-tag-chips mode="display" tags="${reviewCard.review.tags}"/>
    </div>
</article>
