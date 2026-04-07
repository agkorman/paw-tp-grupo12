<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ attribute name="carId" required="true" type="java.lang.Long" %>
<%@ attribute name="currentSort" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url var="reviewsFeedUrl" value="/reviews/feed"/>

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
    <c:choose>
        <c:when test="${empty reviews}">
            <div class="empty-state">
                <p>Todavía no hay reseñas para este auto.</p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="review-list">
                <c:forEach var="review" items="${reviews}">
                    <article class="review-item">
                        <div class="review-item-top">
                            <strong><c:out value="${review.title}"/></strong>
                            <span class="rating-pill"><c:out value="${review.rating}"/>/5.0</span>
                        </div>
                        <p class="review-body"><c:out value="${review.body}"/></p>
                        <div class="review-meta">
                            <span>
                                <c:choose>
                                    <c:when test="${not empty review.userId}">
                                        Usuario #<c:out value="${review.userId}"/>
                                    </c:when>
                                    <c:when test="${not empty review.reviewerEmail}">
                                        <c:out value="${review.reviewerEmail}"/>
                                    </c:when>
                                    <c:otherwise>
                                        Anónimo
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            <span><c:out value="${fn:substring(review.createdAt, 0, 10)}"/></span>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
