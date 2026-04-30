<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ attribute name="reviewThreads" required="true" type="java.util.List" %>
<%@ attribute name="carId" required="true" type="java.lang.Long" %>
<%@ attribute name="currentSort" required="false" type="java.lang.String" %>
<%@ attribute name="currentPage" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="false" type="java.lang.Integer" %>
<%@ attribute name="totalItems" required="false" type="java.lang.Long" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<c:url var="reviewsFeedUrl" value="/reviews/feed"/>
<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

<section id="reviewsFeed" class="reviews-feed">
    <c:set var="reviewTotalCount" value="${empty totalItems ? fn:length(reviews) : totalItems}"/>
    <div class="feed-header">
        <h2>Reseñas <span class="review-count-label">
            <c:choose>
                <c:when test="${reviewTotalCount >= 1000}">${reviewTotalCount / 1000}k</c:when>
                <c:otherwise>${reviewTotalCount}</c:otherwise>
            </c:choose>
        </span></h2>
        <div class="feed-header-actions">
            <form method="get" action="<c:url value='/reviews'/>" class="review-sort-form"
                  data-enhanced-filter="true"
                  data-fragment-url="${reviewsFeedUrl}"
                  data-target="#reviewsFeed"
                  data-auto-submit="true">
                <input type="hidden" name="carId" value="${carId}">
                <label class="review-sort-label" for="reviewSortSelect">Ordenar por:</label>
                <select id="reviewSortSelect" name="sort" class="review-sort-select">
                    <option value="" ${empty currentSort ? 'selected' : ''}>Más recientes</option>
                    <option value="rating_desc" ${currentSort eq 'rating_desc' ? 'selected' : ''}>Mayor puntuación</option>
                    <option value="rating_asc" ${currentSort eq 'rating_asc' ? 'selected' : ''}>Menor puntuación</option>
                </select>
                <noscript>
                    <button type="submit" class="btn-secondary review-sort-submit">Aplicar</button>
                </noscript>
            </form>
        </div>
    </div>
    <c:if test="${not empty replyError}">
        <div class="alert alert-error" role="alert"><c:out value="${replyError}"/></div>
    </c:if>
    <c:choose>
        <c:when test="${empty reviewThreads}">
            <div class="empty-state">
                <p>Todavía no hay reseñas para este auto.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="review-list">
                <c:forEach var="thread" items="${reviewThreads}">
                    <c:set var="review" value="${thread.review}"/>
                    <c:url var="reviewLikeUrl" value="/reviews/${review.id}/like"/>
                    <c:url var="replyCreateUrl" value="/reviews/${review.id}/replies"/>
                    <c:url var="adminReviewDeleteUrl" value="/admin/reviews/${review.id}/delete"/>
                    <article class="review-item" id="review-${review.id}"
                             data-open-review-detail
                             data-review-id="${review.id}">
                        <div class="review-item-top">
                            <strong><c:out value="${review.title}"/></strong>
                            <span class="rating-pill"><c:out value="${review.rating}"/>/5.0</span>
                            <sec:authorize access="hasRole('ADMIN')">
                                <pa:action-menu label="Abrir opciones de review" cssClass="review-admin-menu">
                                    <button
                                            type="button"
                                            class="action-menu-danger"
                                            data-open-delete-review-modal
                                            data-review-delete-action="${fn:escapeXml(adminReviewDeleteUrl)}"
                                            data-review-title="${fn:escapeXml(review.title)}">
                                        Eliminar
                                    </button>
                                </pa:action-menu>
                            </sec:authorize>
                        </div>
                        <p class="review-body"><c:out value="${review.body}"/></p>
                        <pa:review-tag-chips mode="display" tags="${review.tags}"/>
                        <div class="review-meta">
                            <pa:review-author-link review="${review}"/>
                            <span><c:out value="${thread.timeAgo}"/></span>
                            <pa:review-like-button
                                    reviewId="${review.id}"
                                    action="${reviewLikeUrl}"
                                    liked="${thread.liked}"
                                    likeCount="${thread.likeCount}"
                                    disabled="${not authenticated}"/>
                        </div>
                        <div class="review-replies" aria-label="Respuestas a la reseña">
                            <c:if test="${not empty thread.replies}">
                                <div class="review-reply-list">
                                    <c:forEach var="replyCard" items="${thread.replies}">
                                        <c:set var="reply" value="${replyCard.reply}"/>
                                        <c:url var="replyLikeUrl" value="/reviews/replies/${reply.id}/like"/>
                                        <c:url var="replyAuthorProfileUrl" value="/profiles/${reply.userId}"/>
                                        <c:url var="adminReplyDeleteUrl" value="/admin/reviews/replies/${reply.id}/delete"/>
                                        <article class="review-reply" id="reply-${reply.id}">
                                            <div class="review-reply-header">
                                                <a class="review-author-link" href="${replyAuthorProfileUrl}">
                                                    <c:out value="${empty reply.authorUsername ? 'Usuario' : reply.authorUsername}"/>
                                                </a>
                                                <span><c:out value="${replyCard.timeAgo}"/></span>
                                                <sec:authorize access="hasRole('ADMIN')">
                                                    <pa:action-menu label="Abrir opciones de respuesta" cssClass="reply-admin-menu">
                                                        <button
                                                                type="button"
                                                                class="action-menu-danger"
                                                                data-open-delete-reply-modal
                                                                data-reply-delete-action="${fn:escapeXml(adminReplyDeleteUrl)}"
                                                                data-reply-body="${fn:escapeXml(reply.body)}">
                                                            Eliminar
                                                        </button>
                                                    </pa:action-menu>
                                                </sec:authorize>
                                            </div>
                                            <p class="review-reply-body"><c:out value="${reply.body}"/></p>
                                            <pa:review-like-button
                                                    reviewId="${reply.id}"
                                                    action="${replyLikeUrl}"
                                                    liked="${replyCard.liked}"
                                                    likeCount="${replyCard.likeCount}"
                                                    disabled="${not authenticated}"
                                                    label="Like"/>
                                        </article>
                                    </c:forEach>
                                </div>
                            </c:if>
                            <c:choose>
                                <c:when test="${authenticated}">
                                    <form method="post" action="${replyCreateUrl}" class="review-reply-form"
                                          data-enhanced-review-reply="true"
                                          data-target="#reviewsFeed"
                                          data-auth-resume-intent="reply-${review.id}">
                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                        <label for="replyBody-${review.id}">Responder</label>
                                        <div class="review-reply-form-row">
                                            <textarea id="replyBody-${review.id}" name="body" rows="2" maxlength="1000" required
                                                      placeholder="Sumá una respuesta breve"></textarea>
                                            <button type="submit" class="btn-secondary">Responder</button>
                                        </div>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <c:url var="replyLoginUrl" value="/login">
                                        <c:param name="redirect" value="/reviews?carId=${carId}#review-${review.id}"/>
                                        <c:param name="intent" value="reply-${review.id}"/>
                                    </c:url>
                                    <p class="review-reply-login">
                                        <a href="${replyLoginUrl}"
                                           class="review-reply-login-button"
                                           data-auth-required="true"
                                           data-auth-required-action="responder una reseña"
                                           data-auth-required-intent="reply-${review.id}">
                                            Iniciá sesión
                                        </a>
                                        para responder o dar like.
                                    </p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </article>
                </c:forEach>
            </div>
            <c:if test="${not empty totalPages and not empty currentPage and currentPage < totalPages}">
                <c:url var="reviewsBaseUrl" value="/reviews"/>
                <c:url var="showMoreUrl" value="${reviewsBaseUrl}">
                    <c:param name="carId" value="${carId}"/>
                    <c:if test="${not empty currentSort}">
                        <c:param name="sort" value="${currentSort}"/>
                    </c:if>
                    <c:param name="page" value="${currentPage + 1}"/>
                </c:url>
                <div class="reviews-feed-more">
                    <a class="btn-secondary reviews-show-more"
                       href="${showMoreUrl}"
                       data-review-show-more="true"
                       data-fragment-url="${reviewsFeedUrl}"
                       data-target="#reviewsFeed">
                        Mostrar más reseñas
                    </a>
                </div>
            </c:if>
        </c:otherwise>
    </c:choose>
</section>
