<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ attribute name="reviewThreads" required="true" type="java.util.List" %>
<%@ attribute name="carId" required="true" type="java.lang.Long" %>
<%@ attribute name="currentSort" required="false" type="java.lang.String" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalItems" required="false" type="java.lang.Long" %>
<%@ attribute name="currentUserId" required="false" type="java.lang.Long" %>
<%@ attribute name="hideFeedHeader" required="false" type="java.lang.Boolean" %>
<%@ attribute name="repliesPaginated" required="false" type="java.lang.Boolean" %>
<%@ attribute name="repliesCurrentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="repliesTotalPages" required="false" type="java.lang.Integer" %>
<%@ attribute name="contextRedirectBase" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
<spring:message var="reviewRepliesLabel" code="review.feed.replies.aria"/>
<spring:message var="replyPlaceholder" code="review.feed.reply.placeholder"/>
<spring:message var="replyRequiredMessage" code="review.reply.body.required"/>
<spring:message var="replyMaxMessage" code="review.reply.body.max" arguments="1000"/>
<spring:message var="likeLabel" code="review.like.label"/>
<spring:message var="reviewActionMenuLabel" code="review.actionMenu.open"/>
<spring:message var="reviewRepostLabel" code="review.action.repost"/>
<spring:message var="reviewHideLabel" code="review.hide.action.aria"/>
<spring:message var="replyHideLabel" code="reply.hide.action.aria"/>
<spring:message var="replyDeleteLabel" code="reply.delete.action.aria"/>
<spring:message var="reviewAuthorFallback" code="review.author.fallback"/>

<section id="reviewsFeed" class="reviews-feed">
    <c:set var="reviewTotalCount" value="${empty totalItems ? fn:length(reviews) : totalItems}"/>
    <c:if test="${not (hideFeedHeader eq true)}">
        <div class="feed-header">
            <h2><spring:message code="review.feed.title"/> <span class="review-count-label">
                <c:choose>
                    <c:when test="${reviewTotalCount >= 1000}">${reviewTotalCount / 1000}k</c:when>
                    <c:otherwise>${reviewTotalCount}</c:otherwise>
                </c:choose>
            </span></h2>
            <div class="feed-header-actions">
                <form method="get" action="<c:url value='/reviews/car/${carId}'/>#reviewsFeed" class="review-sort-form">
                    <label class="review-sort-label" for="reviewSortSelect"><spring:message code="review.feed.sort"/></label>
                    <div class="cars-toolbar-field">
                        <span class="cars-toolbar-field-ui" aria-hidden="true">
                            <span class="cars-toolbar-field-copy">
                                <span class="cars-toolbar-value" data-toolbar-select-value="sort">
                                    <c:choose>
                                        <c:when test="${currentSort eq 'rating_desc'}"><spring:message code="review.feed.sort.ratingDesc"/></c:when>
                                        <c:when test="${currentSort eq 'rating_asc'}"><spring:message code="review.feed.sort.ratingAsc"/></c:when>
                                        <c:otherwise><spring:message code="review.feed.sort.recent"/></c:otherwise>
                                    </c:choose>
                                </span>
                            </span>
                            <span class="cars-toolbar-chevron">
                                <pa:icon name="chevron-down" size="12"/>
                            </span>
                        </span>
                        <select id="reviewSortSelect" name="sort" class="cars-toolbar-select cars-toolbar-select-overlay" data-auto-submit="true">
                            <option value="" ${empty currentSort ? 'selected' : ''}><spring:message code="review.feed.sort.recent"/></option>
                            <option value="rating_desc" ${currentSort eq 'rating_desc' ? 'selected' : ''}><spring:message code="review.feed.sort.ratingDesc"/></option>
                            <option value="rating_asc" ${currentSort eq 'rating_asc' ? 'selected' : ''}><spring:message code="review.feed.sort.ratingAsc"/></option>
                        </select>
                    </div>
                    <noscript>
                        <button type="submit" class="btn-secondary review-sort-submit">
                            <spring:message code="common.action.apply"/>
                        </button>
                    </noscript>
                </form>
            </div>
        </div>
    </c:if>
    <c:choose>
        <c:when test="${empty reviewThreads}">
            <div class="empty-state">
                <p><spring:message code="review.feed.empty"/></p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="review-list">
                <c:forEach var="thread" items="${reviewThreads}">
                    <c:set var="review" value="${thread.review}"/>
                    <c:url var="reviewLikeUrl" value="/reviews/${review.id}/like"/>
                    <c:url var="replyCreateUrl" value="/reviews/${review.id}/replies"/>
                    <c:choose>
                        <c:when test="${not empty contextRedirectBase}">
                            <c:set var="reviewsContextRedirect" value="${contextRedirectBase}"/>
                            <c:set var="reviewsContextHasQuery" value="${fn:contains(contextRedirectBase, '?')}"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="reviewsContextRedirect" value="/reviews/car/${carId}"/>
                            <c:set var="reviewsContextHasQuery" value="${false}"/>
                            <c:if test="${not empty currentPage and currentPage > 1}">
                                <c:set var="reviewsContextRedirect" value="${reviewsContextRedirect}?page=${currentPage}"/>
                                <c:set var="reviewsContextHasQuery" value="${true}"/>
                            </c:if>
                            <c:if test="${not empty currentSort}">
                                <c:set var="reviewsContextRedirect" value="${reviewsContextRedirect}${reviewsContextHasQuery ? '&' : '?'}sort=${currentSort}"/>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                    <c:set var="reviewItemRedirect" value="${reviewsContextRedirect}#review-${review.id}"/>
                    <c:set var="reviewFeedRedirect" value="${reviewsContextRedirect}#reviewsFeed"/>
                    <c:url var="reviewEditPageUrl" value="/reviews/${review.id}/edit">
                        <c:param name="redirect" value="${reviewItemRedirect}"/>
                    </c:url>
                    <c:url var="reviewDeleteUrl" value="/reviews/${review.id}/delete"/>
                    <c:url var="reviewHideUrl" value="/reviews/${review.id}/hide"/>
                    <c:url var="reviewRepostUrl" value="/reviews/${review.id}/repost"/>
                    <c:url var="reviewRepostLoginUrl" value="/login">
                        <c:param name="redirect" value="/reviews/${review.id}/repost"/>
                    </c:url>
                    <article class="review-item" id="review-${review.id}">
                        <div class="review-item-top">
                            <strong><c:out value="${review.title}"/></strong>
                            <div class="review-item-actions">
                                <span class="rating-pill"><c:out value="${review.rating}"/>/5.0</span>
                                <sec:authorize access="hasRole('ADMIN')">
                                    <c:choose>
                                        <c:when test="${not empty currentUserId and review.userId == currentUserId}">
                                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="review-item-menu">
                                                <a href="${reviewEditPageUrl}">
                                                    <spring:message code="common.action.edit"/>
                                                </a>
                                                <a href="${fn:escapeXml(reviewRepostUrl)}">
                                                    <c:out value="${reviewRepostLabel}"/>
                                                </a>
                                                <form method="post" action="${fn:escapeXml(reviewDeleteUrl)}"
                                                      data-confirm-modal="deleteReviewConfirmModal">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                    <input type="hidden" name="redirect" value="${fn:escapeXml(reviewFeedRedirect)}">
                                                    <button type="submit" class="action-menu-danger">
                                                        <spring:message code="common.action.delete"/>
                                                    </button>
                                                </form>
                                            </pa:action-menu>
                                        </c:when>
                                        <c:otherwise>
                                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="review-item-menu">
                                                <a href="${fn:escapeXml(reviewRepostUrl)}">
                                                    <c:out value="${reviewRepostLabel}"/>
                                                </a>
                                                <button type="button"
                                                        class="action-menu-danger"
                                                        data-open-hide-review-modal
                                                        data-review-id="${fn:escapeXml(review.id)}"
                                                        data-review-hide-action="${fn:escapeXml(reviewHideUrl)}"
                                                        data-review-hide-redirect="${fn:escapeXml(reviewFeedRedirect)}"
                                                        data-review-title="${fn:escapeXml(review.title)}">
                                                    <spring:message code="review.hide.action.aria"/>
                                                </button>
                                            </pa:action-menu>
                                        </c:otherwise>
                                    </c:choose>
                                </sec:authorize>
                                <sec:authorize access="!hasRole('ADMIN') and isAuthenticated()">
                                    <c:choose>
                                        <c:when test="${not empty currentUserId and review.userId == currentUserId}">
                                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="review-item-menu">
                                                <a href="${reviewEditPageUrl}">
                                                    <spring:message code="common.action.edit"/>
                                                </a>
                                                <a href="${fn:escapeXml(reviewRepostUrl)}">
                                                    <c:out value="${reviewRepostLabel}"/>
                                                </a>
                                                <form method="post" action="${fn:escapeXml(reviewDeleteUrl)}"
                                                      data-confirm-modal="deleteReviewConfirmModal">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                    <input type="hidden" name="redirect" value="${fn:escapeXml(reviewFeedRedirect)}">
                                                    <button type="submit" class="action-menu-danger">
                                                        <spring:message code="common.action.delete"/>
                                                    </button>
                                                </form>
                                            </pa:action-menu>
                                        </c:when>
                                        <c:otherwise>
                                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="review-item-menu">
                                                <a href="${fn:escapeXml(reviewRepostUrl)}">
                                                    <c:out value="${reviewRepostLabel}"/>
                                                </a>
                                            </pa:action-menu>
                                        </c:otherwise>
                                    </c:choose>
                                </sec:authorize>
                                <sec:authorize access="!isAuthenticated()">
                                    <pa:action-menu label="${reviewActionMenuLabel}" cssClass="review-item-menu">
                                        <a href="${fn:escapeXml(reviewRepostLoginUrl)}">
                                            <c:out value="${reviewRepostLabel}"/>
                                        </a>
                                    </pa:action-menu>
                                </sec:authorize>
                            </div>
                        </div>
                        <p class="review-body"><c:out value="${review.body}"/></p>
                        <c:if test="${not empty review.images}">
                            <c:set var="reviewImageUrlsCsv" value=""/>
                            <c:forEach var="urlImg" items="${review.images}" varStatus="urlStatus">
                                <c:set var="reviewImageUrlsCsv" value="${reviewImageUrlsCsv}${urlStatus.first ? '' : '|'}/reviews/${review.id}/images/${urlImg.imageId}"/>
                            </c:forEach>
                            <pa:image-gallery imageUrlsJoined="${reviewImageUrlsCsv}" altKey="review.image.alt"/>
                        </c:if>
                        <pa:review-tag-chips mode="display" tags="${review.tags}"/>
                        <c:if test="${not empty review.ownershipStatus or not empty review.mileageKm or review.wouldRecommend ne null}">
                            <dl class="review-details">
                                <c:if test="${not empty review.ownershipStatus}">
                                    <div class="review-details-item">
                                        <dt><spring:message code="review.form.ownership"/></dt>
                                        <dd><c:choose>
                                            <c:when test="${review.ownershipStatus eq 'current_owner' or review.ownershipStatus eq 'Propietario actual'}"><spring:message code="review.form.ownership.current"/></c:when>
                                            <c:when test="${review.ownershipStatus eq 'previous_owner' or review.ownershipStatus eq 'Ex propietario'}"><spring:message code="review.form.ownership.previous"/></c:when>
                                            <c:otherwise><c:out value="${review.ownershipStatus}"/></c:otherwise>
                                        </c:choose></dd>
                                    </div>
                                </c:if>
                                <c:if test="${not empty review.mileageKm}">
                                    <div class="review-details-item">
                                        <dt><spring:message code="review.card.mileage"/></dt>
                                        <dd><c:out value="${review.mileageKm}"/> km</dd>
                                    </div>
                                </c:if>
                                <c:if test="${review.wouldRecommend ne null}">
                                    <div class="review-details-item">
                                        <dt><spring:message code="review.form.recommend"/></dt>
                                        <dd><c:choose>
                                            <c:when test="${review.wouldRecommend}"><spring:message code="common.boolean.yes"/></c:when>
                                            <c:otherwise><spring:message code="common.boolean.no"/></c:otherwise>
                                        </c:choose></dd>
                                    </div>
                                </c:if>
                            </dl>
                        </c:if>
                        <div class="review-meta">
                            <pa:review-author-link review="${review}"/>
                            <span><pa:relative-time value="${review.createdAt}"/></span>
                            <pa:review-like-button
                                    reviewId="${review.id}"
                                    action="${reviewLikeUrl}"
                                    redirect="${reviewItemRedirect}"
                                    liked="${thread.liked}"
                                    likeCount="${thread.likeCount}"
                                    disabled="${not authenticated}"/>
                        </div>
                        <div class="review-replies" id="replies-${review.id}" aria-label="${reviewRepliesLabel}">
                            <c:if test="${repliesPaginated eq true and thread.totalReplyCount > 0}">
                                <p class="review-replies-count">
                                    <spring:message code="review.replies.count" arguments="${thread.totalReplyCount}"/>
                                </p>
                            </c:if>
                            <c:if test="${not empty thread.replies}">
                                <div class="review-reply-list">
                                    <c:forEach var="replyCard" items="${thread.replies}">
                                        <c:set var="reply" value="${replyCard.reply}"/>
                                        <c:url var="replyLikeUrl" value="/reviews/replies/${reply.id}/like"/>
                                        <c:url var="replyAuthorProfileUrl" value="/users/${reply.userId}"/>
                                        <c:url var="replyHideUrl" value="/reviews/replies/${reply.id}/hide"/>
                                        <c:url var="replyDeleteUrl" value="/reviews/replies/${reply.id}/delete"/>
                                        <c:url var="replyUpdateUrl" value="/reviews/replies/${reply.id}/update"/>

                                        <article class="review-reply" id="reply-${reply.id}">
                                            <div class="review-reply-header">
                                                <a class="review-author-link" href="${replyAuthorProfileUrl}">
                                                    <c:out value="${empty reply.authorUsername ? reviewAuthorFallback : reply.authorUsername}"/>
                                                </a>
                                                <span class="review-reply-time"><pa:relative-time value="${reply.createdAt}"/></span>
                                                <div class="review-reply-header-actions">
                                                    <sec:authorize access="hasRole('ADMIN')">
                                                        <c:if test="${empty currentUserId or reply.userId != currentUserId}">
                                                            <button type="button"
                                                                    class="review-hide-button"
                                                                    aria-label="${fn:escapeXml(replyHideLabel)}"
                                                                    title="${fn:escapeXml(replyHideLabel)}"
                                                                    data-open-hide-review-modal
                                                                    data-review-id="${fn:escapeXml(reply.id)}"
                                                                    data-review-hide-action="${fn:escapeXml(replyHideUrl)}"
                                                                    data-review-hide-redirect="${fn:escapeXml(reviewItemRedirect)}"
                                                                    data-review-title="${fn:escapeXml(reply.body)}">
                                                                <pa:icon name="visibility-off" size="18"/>
                                                            </button>
                                                        </c:if>
                                                    </sec:authorize>
                                                    <sec:authorize access="isAuthenticated()">
                                                        <c:if test="${not empty currentUserId and reply.userId == currentUserId}">
                                                            <pa:action-menu label="${reviewActionMenuLabel}" cssClass="review-reply-menu">
                                                                <button type="button" data-edit-reply-trigger>
                                                                    <spring:message code="common.action.edit"/>
                                                                </button>
                                                                <form method="post" action="${fn:escapeXml(replyDeleteUrl)}"
                                                                      data-confirm-modal="deleteReviewConfirmModal">
                                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                                    <input type="hidden" name="redirect" value="${fn:escapeXml(reviewItemRedirect)}">
                                                                    <button type="submit" class="action-menu-danger" aria-label="${fn:escapeXml(replyDeleteLabel)}">
                                                                        <spring:message code="common.action.delete"/>
                                                                    </button>
                                                                </form>
                                                            </pa:action-menu>
                                                        </c:if>
                                                    </sec:authorize>
                                                </div>
                                            </div>
                                            <p class="review-reply-body" data-reply-body><c:out value="${reply.body}"/></p>
                                            <sec:authorize access="isAuthenticated()">
                                                <c:if test="${not empty currentUserId and reply.userId == currentUserId}">
                                                    <form method="post"
                                                          action="${fn:escapeXml(replyUpdateUrl)}"
                                                          class="review-reply-edit-form"
                                                          data-reply-edit-form
                                                          data-reply-required-message="${fn:escapeXml(replyRequiredMessage)}"
                                                          data-reply-max-message="${fn:escapeXml(replyMaxMessage)}"
                                                          hidden
                                                          novalidate="novalidate">
                                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                        <input type="hidden" name="redirect" value="${fn:escapeXml(reviewsContextRedirect)}#reply-${reply.id}">
                                                        <textarea name="body" rows="2" maxlength="1000" required><c:out value="${reply.body}"/></textarea>
                                                        <span class="client-form-error" data-reply-error hidden></span>
                                                        <div class="review-reply-edit-actions">
                                                            <button type="button" class="btn-secondary" data-cancel-reply-edit>
                                                                <spring:message code="common.action.cancel"/>
                                                            </button>
                                                            <button type="submit" class="btn-primary">
                                                                <spring:message code="common.action.save"/>
                                                            </button>
                                                        </div>
                                                    </form>
                                                </c:if>
                                            </sec:authorize>
                                            <pa:review-like-button
                                                    reviewId="${reply.id}"
                                                    action="${replyLikeUrl}"
                                                    liked="${replyCard.liked}"
                                                    likeCount="${replyCard.likeCount}"
                                                    disabled="${not authenticated}"
                                                    redirect="${reviewsContextRedirect}#reply-${reply.id}"
                                                    label="${likeLabel}"/>
                                        </article>
                                    </c:forEach>
                                </div>
                            </c:if>
                            <c:choose>
                                <c:when test="${authenticated}">
                                    <c:set var="replyHasError" value="${not empty replyErrorReviewId and replyErrorReviewId eq review.id}"/>
                                    <form method="post" action="${replyCreateUrl}" class="review-reply-form"
                                          data-reply-intent="reply-${review.id}"
                                          data-reply-has-error="${replyHasError}"
                                          data-reply-required-message="${fn:escapeXml(replyRequiredMessage)}"
                                          data-reply-max-message="${fn:escapeXml(replyMaxMessage)}"
                                          novalidate="novalidate">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <c:if test="${not empty currentPage and currentPage > 1}">
                                            <input type="hidden" name="page" value="${currentPage}">
                                        </c:if>
                                        <c:if test="${not empty currentSort}">
                                            <input type="hidden" name="sort" value="${fn:escapeXml(currentSort)}">
                                        </c:if>
                                        <button type="button" class="review-reply-toggle" data-reply-toggle
                                                aria-expanded="false" aria-controls="replyPanel-${review.id}"><spring:message code="review.feed.reply"/></button>
                                        <div class="review-reply-panel" id="replyPanel-${review.id}" data-reply-panel>
                                            <label for="replyBody-${review.id}"><spring:message code="review.feed.reply"/></label>
                                            <div class="review-reply-form-row">
                                                <textarea id="replyBody-${review.id}" name="body" rows="2" maxlength="1000" required
                                                          placeholder="${replyPlaceholder}"
                                                          class="${replyHasError ? 'is-invalid' : ''}"><c:if test="${replyHasError}"><c:out value="${replyErrorBody}"/></c:if></textarea>
                                                <button type="submit" class="btn-secondary"><spring:message code="review.feed.reply"/></button>
                                            </div>
                                            <span class="client-form-error" data-reply-error <c:if test="${not replyHasError}">hidden</c:if>><c:if test="${replyHasError}"><c:out value="${replyError}"/></c:if></span>
                                        </div>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <c:url var="replyLoginUrl" value="/login">
                                        <c:param name="redirect" value="/reviews/car/${carId}#review-${review.id}"/>
                                        <c:param name="intent" value="reply-${review.id}"/>
                                    </c:url>
                                    <p class="review-reply-login">
                                        <a href="${replyLoginUrl}"
                                           class="review-reply-login-button">
                                            <spring:message code="review.feed.loginPrefix"/>
                                        </a>
                                        <spring:message code="review.feed.loginSuffix"/>
                                    </p>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${repliesPaginated eq true and not empty repliesTotalPages and repliesTotalPages > 1}">
                                <c:url var="reviewDetailBaseUrl" value="/reviews/${review.id}"/>
                                <spring:message var="repliesPaginationAria" code="review.replies.pagination.aria"/>
                                <pa:pagination currentPage="${repliesCurrentPage}"
                                               totalPages="${repliesTotalPages}"
                                               baseUrl="${reviewDetailBaseUrl}"
                                               pageParam="repliesPage"
                                               fragment="replies-${review.id}"
                                               ariaLabel="${repliesPaginationAria}"/>
                            </c:if>
                            <c:if test="${not (repliesPaginated eq true) and thread.hasMoreReplies}">
                                <c:url var="reviewDetailViewUrl" value="/reviews/${review.id}"/>
                                <p class="review-replies-view-all">
                                    <a href="${reviewDetailViewUrl}#replies-${review.id}" class="review-replies-view-all-link">
                                        <spring:message code="${thread.totalReplyCount eq 1 ? 'review.replies.viewAll.one' : 'review.replies.viewAll.many'}"
                                                        arguments="${thread.totalReplyCount}"/>
                                    </a>
                                </p>
                            </c:if>
                        </div>
                    </article>
                </c:forEach>
            </div>
            <c:if test="${not (hideFeedHeader eq true) and not empty totalPages and not empty currentPage and totalPages > 1}">
                <c:set var="reviewsBaseUrl" value="/reviews/car/${carId}"/>
                <jsp:useBean id="reviewsPaginationParams" class="java.util.LinkedHashMap"/>
                <c:if test="${not empty currentSort}">
                    <c:set target="${reviewsPaginationParams}" property="sort" value="${currentSort}"/>
                </c:if>
                <spring:message var="reviewsPaginationAria" code="review.feed.pagination.aria"/>
                <pa:pagination currentPage="${currentPage}"
                               totalPages="${totalPages}"
                               baseUrl="${reviewsBaseUrl}"
                               extraParams="${reviewsPaginationParams}"
                               fragment="reviewsFeed"
                               ariaLabel="${reviewsPaginationAria}"/>
            </c:if>
        </c:otherwise>
    </c:choose>
</section>
<pa:image-lightbox/>
<pa:script src="/js/shared/image-lightbox.js" defer="true"/>
