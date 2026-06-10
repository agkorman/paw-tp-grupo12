<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activityCard" required="true" type="ar.edu.itba.paw.webapp.controller.ActivityController.ActivityCardView" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="activityMetricsAria" code="activity.card.metrics.aria"/>
<spring:message var="activityActionMenuLabel" code="activity.card.actionMenu.label"/>
<spring:message var="activityRepostLabel" code="review.action.repost"/>

<c:set var="activityCurrentPath" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
<c:if test="${empty activityCurrentPath}">
    <c:set var="activityCurrentPath" value="/"/>
</c:if>
<c:set var="activityLikeQueryStr" value="${requestScope['javax.servlet.forward.query_string']}"/>
<c:if test="${not empty activityLikeQueryStr}">
    <c:set var="activityCurrentPath" value="${activityCurrentPath}?${activityLikeQueryStr}"/>
</c:if>
<c:set var="activityCardRedirectPath" value="${activityCurrentPath}#${activityCard.cardAnchorId}"/>

<c:set var="activityCardImageUrls" value=""/>
<c:choose>
    <c:when test="${activityCard.review}">
        <c:url var="activityReviewBase" value="/reviews/car/${activityCard.carId}">
            <c:if test="${activityCard.reviewPage > 1}"><c:param name="page" value="${activityCard.reviewPage}"/></c:if>
        </c:url>
        <c:set var="resolvedCardHref" value="${activityReviewBase}#review-${activityCard.likeEntityId}"/>
        <c:set var="resolvedCommentsHref" value="${activityReviewBase}#replies-${activityCard.likeEntityId}"/>
        <c:url var="resolvedLikeAction" value="/reviews/${activityCard.likeEntityId}/like"/>
        <c:url var="activityCardEditUrl" value="/reviews/${activityCard.likeEntityId}/edit">
            <c:param name="redirect" value="${activityCardRedirectPath}"/>
        </c:url>
        <c:url var="activityCardDeleteUrl" value="/reviews/${activityCard.likeEntityId}/delete"/>
        <c:url var="activityCardHideUrl" value="/reviews/${activityCard.likeEntityId}/hide"/>
        <c:if test="${activityCard.repostable}">
            <c:url var="activityCardRepostUrl" value="/reviews/${activityCard.likeEntityId}/repost"/>
        </c:if>
        <c:if test="${not empty activityCard.carId}">
            <c:url var="resolvedContextHref" value="/reviews/car/${activityCard.carId}"/>
        </c:if>
        <c:forEach var="activityImageId" items="${activityCard.imageIds}">
            <c:set var="activityCardImageUrls" value="${activityCardImageUrls}${empty activityCardImageUrls ? '' : '|'}/reviews/${activityCard.likeEntityId}/images/${activityImageId}"/>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <c:url var="resolvedCardHref" value="/communities/${activityCard.communitySlug}/posts/${activityCard.postSlug}">
            <c:param name="redirect" value="${activityCurrentPath}"/>
        </c:url>
        <c:url var="activityCommentsBase" value="/communities/${activityCard.communitySlug}/posts/${activityCard.postSlug}">
            <c:param name="redirect" value="${activityCurrentPath}"/>
        </c:url>
        <c:set var="resolvedCommentsHref" value="${activityCommentsBase}#comments"/>
        <c:url var="resolvedLikeAction" value="/communities/${activityCard.communitySlug}/posts/${activityCard.postSlug}/helpful"/>
        <c:url var="activityCardEditUrl" value="/communities/${activityCard.communitySlug}/posts/${activityCard.postSlug}/edit">
            <c:param name="redirect" value="${activityCardRedirectPath}"/>
        </c:url>
        <c:url var="activityCardDeleteUrl" value="/communities/${activityCard.communitySlug}/posts/${activityCard.postSlug}/delete"/>
        <c:url var="activityCardHideUrl" value="/communities/${activityCard.communitySlug}/posts/${activityCard.postSlug}/hide"/>
        <c:url var="resolvedContextHref" value="/communities/${activityCard.communitySlug}"/>
        <c:forEach var="activityImageId" items="${activityCard.imageIds}">
            <c:set var="activityCardImageUrls" value="${activityCardImageUrls}${empty activityCardImageUrls ? '' : '|'}/communities/${activityCard.communitySlug}/posts/${activityCard.postSlug}/images/${activityImageId}"/>
        </c:forEach>
    </c:otherwise>
</c:choose>
<c:if test="${not empty activityCard.authorUserId}">
    <c:url var="resolvedAuthorHref" value="/users/${activityCard.authorUserId}"/>
</c:if>

<c:choose>
    <c:when test="${not empty activityCard.authorName}">
        <c:set var="resolvedAuthorName" value="${activityCard.authorName}"/>
    </c:when>
    <c:otherwise>
        <spring:message var="resolvedAuthorName" code="activity.card.author.unknown"/>
    </c:otherwise>
</c:choose>

<c:if test="${not empty activityCard.secondaryMetricKey or not empty activityCard.secondaryMetricValue}">
    <c:choose>
        <c:when test="${not empty activityCard.secondaryMetricKey}">
            <spring:message var="secondaryMetricText" code="${activityCard.secondaryMetricKey}" arguments="${activityCard.secondaryMetricValue}"/>
        </c:when>
        <c:otherwise>
            <c:set var="secondaryMetricText" value="${activityCard.secondaryMetricValue}"/>
        </c:otherwise>
    </c:choose>
</c:if>

<article class="activity-card" id="${fn:escapeXml(activityCard.cardAnchorId)}">
    <div class="activity-card-topline">
        <div class="activity-card-topline-content">
            <p class="activity-card-meta">
                <c:choose>
                    <c:when test="${not empty resolvedAuthorHref}">
                        <a class="activity-card-author-link" href="${fn:escapeXml(resolvedAuthorHref)}">
                            <strong><c:out value="${resolvedAuthorName}"/></strong>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <strong><c:out value="${resolvedAuthorName}"/></strong>
                    </c:otherwise>
                </c:choose>
                <span aria-hidden="true">•</span>
                <span><pa:relative-time value="${activityCard.createdAt}"/></span>
            </p>

            <p class="activity-card-context">
                <span class="activity-card-context-label ${activityCard.review ? 'activity-card-context-label--review' : 'activity-card-context-label--community'}"><spring:message code="${activityCard.contextLabelKey}"/></span>
                <c:if test="${not empty activityCard.contextValue}">
                    <c:choose>
                        <c:when test="${not empty resolvedContextHref}">
                            <a class="activity-card-context-link" href="${fn:escapeXml(resolvedContextHref)}">
                                <c:out value="${activityCard.contextValue}"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <span class="activity-card-context-value"><c:out value="${activityCard.contextValue}"/></span>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </p>
        </div>
        <c:if test="${activityCard.actionMenuVisible}">
            <pa:action-menu label="${activityActionMenuLabel}" cssClass="activity-card-menu">
                <c:if test="${activityCard.repostable}">
                    <a href="${fn:escapeXml(activityCardRepostUrl)}">
                        <c:out value="${activityRepostLabel}"/>
                    </a>
                </c:if>
                <c:if test="${activityCard.editable}">
                    <a href="${fn:escapeXml(activityCardEditUrl)}">
                        <spring:message code="common.action.edit"/>
                    </a>
                </c:if>
                <c:if test="${activityCard.hideable}">
                    <c:choose>
                        <c:when test="${activityCard.review}">
                            <button type="button"
                                    class="action-menu-danger"
                                    data-open-hide-review-modal
                                    data-review-hide-action="${fn:escapeXml(activityCardHideUrl)}"
                                    data-review-hide-redirect="${fn:escapeXml(activityCurrentPath)}"
                                    data-review-title="${fn:escapeXml(activityCard.title)}">
                                <spring:message code="review.hide.action.aria"/>
                            </button>
                        </c:when>
                        <c:otherwise>
                            <button type="button"
                                    class="action-menu-danger"
                                    data-open-community-hide-modal
                                    data-community-hide-modal-target="${fn:escapeXml(activityCard.hideModalTarget)}"
                                    data-community-hide-action="${fn:escapeXml(activityCardHideUrl)}"
                                    data-community-hide-redirect="${fn:escapeXml(activityCurrentPath)}">
                                <spring:message code="communities.post.hideAction"/>
                            </button>
                        </c:otherwise>
                    </c:choose>
                </c:if>
                <c:if test="${activityCard.deletable}">
                    <form method="post" action="${fn:escapeXml(activityCardDeleteUrl)}"
                          data-confirm-modal="${activityCard.review ? 'deleteReviewConfirmModal' : 'deletePostConfirmModal'}">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                        <input type="hidden" name="redirect" value="${fn:escapeXml(activityCurrentPath)}">
                        <button type="submit" class="action-menu-danger">
                            <c:choose>
                                <c:when test="${activityCard.review}">
                                    <spring:message code="common.action.delete"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="communities.post.deleteAction"/>
                                </c:otherwise>
                            </c:choose>
                        </button>
                    </form>
                </c:if>
            </pa:action-menu>
        </c:if>
    </div>

    <h2 class="activity-card-title">
        <a href="${fn:escapeXml(resolvedCardHref)}"><c:out value="${activityCard.title}"/></a>
    </h2>
    <p class="activity-card-body ${not empty activityCard.imageIds ? 'activity-card-body--with-image' : 'activity-card-body--text-only'}"><c:out value="${activityCard.body}"/></p>
    <c:if test="${not empty activityCard.imageIds}">
        <pa:image-gallery imageUrlsJoined="${activityCardImageUrls}" altKey="${activityCard.imageAltKey}" maxVisible="${3}" cssClass="activity-card-gallery"/>
    </c:if>
    <c:if test="${not empty activityCard.repostReview}">
        <pa:reposted-review-card repostReview="${activityCard.repostReview}"/>
    </c:if>

    <div class="activity-card-metrics" aria-label="${fn:escapeXml(activityMetricsAria)}">
        <pa:review-like-button
                reviewId="${activityCard.likeEntityId}"
                action="${resolvedLikeAction}"
                redirect="${activityCardRedirectPath}"
                liked="${activityCard.liked}"
                likeCount="${activityCard.likeCount}"
                disabled="${not activityCard.authenticated}"/>
        <c:if test="${not empty secondaryMetricText}">
            <c:choose>
                <c:when test="${not empty resolvedCommentsHref}">
                    <a class="activity-card-metric activity-card-metric-link" href="${fn:escapeXml(resolvedCommentsHref)}"><c:out value="${secondaryMetricText}"/></a>
                </c:when>
                <c:otherwise>
                    <span class="activity-card-metric"><c:out value="${secondaryMetricText}"/></span>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>
</article>
