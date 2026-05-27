<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ attribute name="reviewThreads" required="true" type="java.util.List" %>
<%@ attribute name="carId" required="true" type="java.lang.Long" %>
<%@ attribute name="currentSort" required="false" type="java.lang.String" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalItems" required="false" type="java.lang.Long" %>
<%@ attribute name="currentUserId" required="false" type="java.lang.Long" %>
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
<spring:message var="replyAuthAction" code="review.authRequired.replyAction"/>
<spring:message var="likeLabel" code="review.like.label"/>
<spring:message var="reviewActionMenuLabel" code="review.actionMenu.open"/>
<spring:message var="reviewHideLabel" code="review.hide.action.aria"/>
<spring:message var="replyHideLabel" code="reply.hide.action.aria"/>
<spring:message var="replyDeleteLabel" code="reply.delete.action.aria"/>
<spring:message var="reviewAuthorFallback" code="review.author.fallback"/>

<section id="reviewsFeed" class="reviews-feed">
    <c:set var="reviewTotalCount" value="${empty totalItems ? fn:length(reviews) : totalItems}"/>
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
                <select id="reviewSortSelect" name="sort" class="review-sort-select">
                    <option value="" ${empty currentSort ? 'selected' : ''}><spring:message code="review.feed.sort.recent"/></option>
                    <option value="rating_desc" ${currentSort eq 'rating_desc' ? 'selected' : ''}><spring:message code="review.feed.sort.ratingDesc"/></option>
                    <option value="rating_asc" ${currentSort eq 'rating_asc' ? 'selected' : ''}><spring:message code="review.feed.sort.ratingAsc"/></option>
                </select>
                <button type="submit" class="btn-secondary review-sort-submit"><spring:message code="common.action.apply"/></button>
            </form>
        </div>
    </div>
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
                    <c:set var="reviewsContextRedirect" value="/reviews/car/${carId}"/>
                    <c:set var="reviewsContextHasQuery" value="${false}"/>
                    <c:if test="${not empty currentPage and currentPage > 1}">
                        <c:set var="reviewsContextRedirect" value="${reviewsContextRedirect}?page=${currentPage}"/>
                        <c:set var="reviewsContextHasQuery" value="${true}"/>
                    </c:if>
                    <c:if test="${not empty currentSort}">
                        <c:set var="reviewsContextRedirect" value="${reviewsContextRedirect}${reviewsContextHasQuery ? '&' : '?'}sort=${currentSort}"/>
                    </c:if>
                    <c:set var="reviewItemRedirect" value="${reviewsContextRedirect}#review-${review.id}"/>
                    <c:set var="reviewFeedRedirect" value="${reviewsContextRedirect}#reviewsFeed"/>
                    <c:url var="reviewEditPageUrl" value="/reviews/${review.id}/edit">
                        <c:param name="redirect" value="${reviewItemRedirect}"/>
                    </c:url>
                    <c:url var="reviewDeleteUrl" value="/reviews/${review.id}/delete"/>
                    <c:url var="reviewHideUrl" value="/reviews/${review.id}/hide"/>
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
                                            <button type="button"
                                                    class="review-hide-button"
                                                    aria-label="${fn:escapeXml(reviewHideLabel)}"
                                                    title="${fn:escapeXml(reviewHideLabel)}"
                                                    data-open-hide-review-modal
                                                    data-review-id="${fn:escapeXml(review.id)}"
                                                    data-review-hide-action="${fn:escapeXml(reviewHideUrl)}"
                                                    data-review-hide-redirect="${fn:escapeXml(reviewItemRedirect)}"
                                                    data-review-title="${fn:escapeXml(review.title)}">
                                                <pa:icon name="visibility-off" size="18"/>
                                            </button>
                                        </c:otherwise>
                                    </c:choose>
                                </sec:authorize>
                                <sec:authorize access="!hasRole('ADMIN') and isAuthenticated()">
                                    <c:if test="${not empty currentUserId and review.userId == currentUserId}">
                                        <pa:action-menu label="${reviewActionMenuLabel}" cssClass="review-item-menu">
                                            <a href="${reviewEditPageUrl}">
                                                <spring:message code="common.action.edit"/>
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
                                    </c:if>
                                </sec:authorize>
                            </div>
                        </div>
                        <c:if test="${not empty review.images}">
                            <c:set var="reviewImageUrlsCsv" value=""/>
                            <c:forEach var="urlImg" items="${review.images}" varStatus="urlStatus">
                                <c:url var="oneUrl" value="/reviews/${review.id}/images/${urlImg.imageId}"/>
                                <c:set var="reviewImageUrlsCsv" value="${reviewImageUrlsCsv}${urlStatus.first ? '' : '|'}${oneUrl}"/>
                            </c:forEach>
                            <div class="review-images-row" data-review-image-urls="${fn:escapeXml(reviewImageUrlsCsv)}">
                                <c:forEach var="reviewImg" items="${review.images}" varStatus="thumbStatus">
                                    <button type="button" class="review-image-thumb-button"
                                            data-review-image-index="${thumbStatus.index}"
                                            aria-label="<spring:message code='review.image.alt'/>">
                                        <img src="<c:url value='/reviews/${review.id}/images/${reviewImg.imageId}'/>"
                                             alt="<spring:message code='review.image.alt'/>"
                                             class="review-image-thumb"/>
                                    </button>
                                </c:forEach>
                            </div>
                        </c:if>
                        <p class="review-body"><c:out value="${review.body}"/></p>
                        <pa:review-tag-chips mode="display" tags="${review.tags}"/>
                        <div class="review-meta">
                            <pa:review-author-link review="${review}"/>
                            <span><c:out value="${relativeTimeFormatter.format(review.createdAt)}"/></span>
                            <pa:review-like-button
                                    reviewId="${review.id}"
                                    action="${reviewLikeUrl}"
                                    redirect="${reviewItemRedirect}"
                                    liked="${thread.liked}"
                                    likeCount="${thread.likeCount}"
                                    disabled="${not authenticated}"/>
                        </div>
                        <div class="review-replies" aria-label="${reviewRepliesLabel}">
                            <c:if test="${not empty thread.replies}">
                                <div class="review-reply-list">
                                    <c:forEach var="replyCard" items="${thread.replies}">
                                        <c:set var="reply" value="${replyCard.reply}"/>
                                        <c:url var="replyLikeUrl" value="/reviews/replies/${reply.id}/like"/>
                                        <c:url var="replyHideUrl" value="/reviews/replies/${reply.id}/hide"/>
                                        <c:url var="replyDeleteUrl" value="/reviews/replies/${reply.id}/delete"/>
                                        <c:url var="replyUpdateUrl" value="/reviews/replies/${reply.id}/update"/>
                                        <c:url var="replyAuthorProfileUrl" value="/profiles/${reply.userId}"/>
                                        <article class="review-reply" id="reply-${reply.id}">
                                            <div class="review-reply-header">
                                                <a class="review-author-link" href="${replyAuthorProfileUrl}">
                                                    <c:out value="${empty reply.authorUsername ? reviewAuthorFallback : reply.authorUsername}"/>
                                                </a>
                                                <div class="review-reply-header-actions">
                                                    <span><c:out value="${relativeTimeFormatter.format(reply.createdAt)}"/></span>
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
                                                    label="${likeLabel}"/>
                                        </article>
                                    </c:forEach>
                                </div>
                            </c:if>
                            <c:choose>
                                <c:when test="${authenticated}">
                                    <c:set var="replyHasError" value="${not empty replyErrorReviewId and replyErrorReviewId eq review.id}"/>
                                    <form method="post" action="${replyCreateUrl}" class="review-reply-form"
                                          data-auth-resume-intent="reply-${review.id}"
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
                                        <label for="replyBody-${review.id}"><spring:message code="review.feed.reply"/></label>
                                        <div class="review-reply-form-row">
                                            <textarea id="replyBody-${review.id}" name="body" rows="2" maxlength="1000" required
                                                      placeholder="${replyPlaceholder}"
                                                      class="${replyHasError ? 'is-invalid' : ''}"><c:if test="${replyHasError}"><c:out value="${replyErrorBody}"/></c:if></textarea>
                                            <button type="submit" class="btn-secondary"><spring:message code="review.feed.reply"/></button>
                                        </div>
                                        <span class="client-form-error" data-reply-error <c:if test="${not replyHasError}">hidden</c:if>><c:if test="${replyHasError}"><c:out value="${replyError}"/></c:if></span>
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
                        </div>
                    </article>
                </c:forEach>
            </div>
            <c:if test="${not empty totalPages and not empty currentPage and totalPages > 1}">
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

<spring:message var="lightboxPrevLabel" code="cars.image.previous"/>
<spring:message var="lightboxNextLabel" code="cars.image.next"/>
<spring:message var="lightboxCloseLabel" code="common.action.close"/>
<div id="reviewImageLightbox" class="review-image-lightbox" hidden aria-hidden="true" role="dialog" aria-modal="true">
    <div class="review-image-lightbox-backdrop" data-lightbox-close></div>
    <div class="review-image-lightbox-stage">
        <button type="button" class="review-image-lightbox-close" data-lightbox-close
                aria-label="${fn:escapeXml(lightboxCloseLabel)}">×</button>
        <button type="button" class="review-image-lightbox-nav review-image-lightbox-prev" data-lightbox-prev
                aria-label="${fn:escapeXml(lightboxPrevLabel)}">
            <pa:icon name="chevron-left" size="24"/>
        </button>
        <img class="review-image-lightbox-img" data-lightbox-img alt="">
        <button type="button" class="review-image-lightbox-nav review-image-lightbox-next" data-lightbox-next
                aria-label="${fn:escapeXml(lightboxNextLabel)}">
            <pa:icon name="chevron-right" size="24"/>
        </button>
        <span class="review-image-lightbox-count" data-lightbox-count>1 / 1</span>
    </div>
</div>
<pa:script src="/js/reviews/review-image-lightbox.js" defer="true"/>
