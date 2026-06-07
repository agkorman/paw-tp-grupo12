<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activityCard" required="true" type="ar.edu.itba.paw.webapp.controller.ActivityController.ActivityCardView" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="activityMetricsAria" code="activity.card.metrics.aria"/>
<spring:message var="activityActionMenuLabel" code="activity.card.actionMenu.label"/>
<spring:message var="reviewHideLabel" code="review.hide.action.aria"/>
<spring:message var="postHideLabel" code="communities.post.hideAction"/>

<c:set var="activityCurrentPath" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
<c:if test="${empty activityCurrentPath}">
    <c:set var="activityCurrentPath" value="/"/>
</c:if>
<c:set var="activityLikeQueryStr" value="${requestScope['javax.servlet.forward.query_string']}"/>
<c:if test="${not empty activityLikeQueryStr}">
    <c:set var="activityCurrentPath" value="${activityCurrentPath}?${activityLikeQueryStr}"/>
</c:if>
<c:set var="activityCardRedirectPath" value="${activityCurrentPath}#${activityCard.cardAnchorId}"/>

<c:choose>
    <c:when test="${activityCard.communityPost}">
        <c:url var="resolvedCardHref" value="${activityCard.href}">
            <c:param name="redirect" value="${activityCurrentPath}"/>
        </c:url>
    </c:when>
    <c:when test="${fn:contains(activityCard.href, '#')}">
        <c:url var="resolvedCardHref" value="${fn:substringBefore(activityCard.href, '#')}"/>
        <c:set var="resolvedCardHref" value="${resolvedCardHref}#${fn:substringAfter(activityCard.href, '#')}"/>
    </c:when>
    <c:otherwise>
        <c:url var="resolvedCardHref" value="${activityCard.href}"/>
    </c:otherwise>
</c:choose>
<c:if test="${not empty activityCard.authorHref}">
    <c:url var="resolvedAuthorHref" value="${activityCard.authorHref}"/>
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

<c:url var="resolvedLikeAction" value="${activityCard.likeAction}"/>

<c:if test="${not empty activityCard.commentsHref}">
    <c:set var="commentsBase" value="${fn:substringBefore(activityCard.commentsHref, '#')}"/>
    <c:set var="commentsFragment" value="${fn:substringAfter(activityCard.commentsHref, '#')}"/>
    <c:choose>
        <c:when test="${activityCard.communityPost}">
            <c:url var="resolvedCommentsHref" value="${commentsBase}">
                <c:param name="redirect" value="${activityCurrentPath}"/>
            </c:url>
        </c:when>
        <c:otherwise>
            <c:url var="resolvedCommentsHref" value="${commentsBase}"/>
        </c:otherwise>
    </c:choose>
    <c:set var="resolvedCommentsHref" value="${resolvedCommentsHref}#${commentsFragment}"/>
</c:if>

<article class="activity-card" id="${fn:escapeXml(activityCard.cardAnchorId)}">
    <div class="activity-card-topline">
        <div class="activity-card-topline-content">
            <p class="activity-card-meta">
                <c:choose>
                    <c:when test="${not empty activityCard.authorHref}">
                        <a class="activity-card-author-link" href="${fn:escapeXml(resolvedAuthorHref)}">
                            <strong><c:out value="${resolvedAuthorName}"/></strong>
                        </a>
                    </c:when>
                    <c:otherwise>
                        <strong><c:out value="${resolvedAuthorName}"/></strong>
                    </c:otherwise>
                </c:choose>
                <span aria-hidden="true">•</span>
                <span><c:out value="${activityCard.timeText}"/></span>
            </p>

            <p class="activity-card-context">
                <span class="activity-card-context-label"><spring:message code="${activityCard.contextLabelKey}"/></span>
                <c:if test="${not empty activityCard.contextValue}">
                    <c:choose>
                        <c:when test="${not empty activityCard.contextHref}">
                            <c:url var="resolvedContextHref" value="${activityCard.contextHref}"/>
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
        <c:choose>
            <c:when test="${activityCard.editable or activityCard.deletable}">
                <pa:action-menu label="${activityActionMenuLabel}" cssClass="activity-card-menu">
                    <c:if test="${activityCard.editable}">
                        <c:url var="activityCardEditUrl" value="${activityCard.editHref}">
                            <c:param name="redirect" value="${activityCardRedirectPath}"/>
                        </c:url>
                        <a href="${fn:escapeXml(activityCardEditUrl)}">
                            <spring:message code="common.action.edit"/>
                        </a>
                    </c:if>
                    <c:if test="${activityCard.deletable}">
                        <c:url var="activityCardDeleteUrl" value="${activityCard.deleteAction}"/>
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
            </c:when>
            <c:when test="${activityCard.hideable}">
                <c:url var="activityCardHideUrl" value="${activityCard.hideAction}"/>
                <c:choose>
                    <c:when test="${activityCard.review}">
                        <button type="button"
                                class="action-menu-hide-button"
                                aria-label="${fn:escapeXml(reviewHideLabel)}"
                                title="${fn:escapeXml(reviewHideLabel)}"
                                data-open-hide-review-modal
                                data-review-hide-action="${fn:escapeXml(activityCardHideUrl)}"
                                data-review-hide-redirect="${fn:escapeXml(activityCurrentPath)}"
                                data-review-title="${fn:escapeXml(activityCard.title)}">
                            <pa:icon name="visibility-off" size="18"/>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button type="button"
                                class="action-menu-hide-button"
                                aria-label="${fn:escapeXml(postHideLabel)}"
                                title="${fn:escapeXml(postHideLabel)}"
                                data-open-community-hide-modal
                                data-community-hide-modal-target="${fn:escapeXml(activityCard.hideModalTarget)}"
                                data-community-hide-action="${fn:escapeXml(activityCardHideUrl)}"
                                data-community-hide-redirect="${fn:escapeXml(activityCurrentPath)}">
                            <pa:icon name="visibility-off" size="18"/>
                        </button>
                    </c:otherwise>
                </c:choose>
            </c:when>
        </c:choose>
    </div>

    <h2 class="activity-card-title">
        <a href="${fn:escapeXml(resolvedCardHref)}"><c:out value="${activityCard.title}"/></a>
    </h2>
    <p class="activity-card-body ${not empty activityCard.imageUrls ? 'activity-card-body--with-image' : 'activity-card-body--text-only'}"><c:out value="${activityCard.body}"/></p>
    <c:if test="${not empty activityCard.imageUrls}">
        <c:set var="resolvedImageUrlsJoined" value=""/>
        <c:forEach var="rawImageUrl" items="${activityCard.imageUrls}" varStatus="status">
            <c:url var="oneResolvedUrl" value="${rawImageUrl}"/>
            <c:set var="resolvedImageUrlsJoined" value="${resolvedImageUrlsJoined}${status.first ? '' : '|'}${oneResolvedUrl}"/>
        </c:forEach>
        <div class="activity-card-gallery-shell">
            <pa:image-gallery imageUrlsJoined="${resolvedImageUrlsJoined}"
                              altKey="${activityCard.imageAltKey}"
                              cssClass="activity-card-gallery"/>
            <span class="activity-card-gallery-count">
                1 / <c:out value="${fn:length(activityCard.imageUrls)}"/>
            </span>
        </div>
    </c:if>

    <div class="activity-card-metrics" aria-label="${fn:escapeXml(activityMetricsAria)}">
        <pa:review-like-button
                reviewId="${activityCard.likeEntityId}"
                action="${resolvedLikeAction}"
                redirect="${activityCardRedirectPath}"
                liked="${activityCard.liked}"
                likeCount="${activityCard.likeCount}"
                disabled="${not activityCard.authenticated}"
                intent="${activityCard.cardAnchorId}"/>
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
