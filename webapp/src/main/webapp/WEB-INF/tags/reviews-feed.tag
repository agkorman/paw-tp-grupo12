<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ attribute name="reviewThreads" required="true" type="java.util.List" %>
<%@ attribute name="carId" required="true" type="java.lang.Long" %>
<%@ attribute name="currentSort" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>


<c:url var="reviewsFeedUrl" value="/reviews/feed"/>
<c:url var="loginUrl" value="/login"/>
<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

<section id="reviewsFeed" class="reviews-feed">
    <div class="feed-header">
        <h2>Reseñas <span class="review-count-label">
            <c:choose>
                <c:when test="${fn:length(reviews) >= 1000}">${fn:length(reviews) / 1000}k</c:when>
                <c:otherwise>${fn:length(reviews)}</c:otherwise>
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
                    <article class="review-item" id="review-${review.id}">
                        <div class="review-item-top">
                            <strong><c:out value="${review.title}"/></strong>
                            <span class="rating-pill"><c:out value="${review.rating}"/>/5.0</span>
                        </div>
                        <p class="review-body"><c:out value="${review.body}"/></p>
                        <div class="review-meta">
                            <pa:review-author-link review="${review}"/>
                            <span><c:out value="${fn:substring(review.createdAt, 0, 10)}"/></span>
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
                                        <article class="review-reply" id="reply-${reply.id}">
                                            <div class="review-reply-header">
                                                <a class="review-author-link" href="${replyAuthorProfileUrl}">
                                                    <c:out value="${empty reply.authorUsername ? 'Usuario' : reply.authorUsername}"/>
                                                </a>
                                                <span><c:out value="${fn:substring(reply.createdAt, 0, 10)}"/></span>
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
                                    <form method="post" action="${replyCreateUrl}" class="review-reply-form">
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
                                    <p class="review-reply-login">
                                        <a href="${loginUrl}">Iniciá sesión</a> para responder o dar like.
                                    </p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
