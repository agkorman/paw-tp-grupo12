<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ attribute name="carId" required="true" type="java.lang.Long" %>
<%@ attribute name="currentSort" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:url var="reviewsFeedUrl" value="/reviews/feed"/>

<section id="reviewsFeed" class="reviews-feed">
    <div class="feed-header">
        <h2>Reviews</h2>
        <div class="feed-header-actions">
            <span class="count-label">${fn:length(reviews)} entradas</span>
            <form method="get" action="<c:url value='/reviews'/>" class="review-sort-form"
                  data-enhanced-filter="true"
                  data-fragment-url="${reviewsFeedUrl}"
                  data-target="#reviewsFeed"
                  data-auto-submit="true">
                <input type="hidden" name="carId" value="${carId}">
                <label class="review-sort-label" for="reviewSortSelect">Ordenar por:</label>
                <select id="reviewSortSelect" name="sort" class="review-sort-select">
                    <option value="" ${empty currentSort ? 'selected' : ''}>Mas recientes</option>
                    <option value="rating_desc" ${currentSort eq 'rating_desc' ? 'selected' : ''}>Mayor puntuacion</option>
                    <option value="rating_asc" ${currentSort eq 'rating_asc' ? 'selected' : ''}>Menor puntuacion</option>
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
                <p>Todavia no hay reviews para este auto.</p>
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
                                    <c:when test="${not empty review.reviewerEmail}">
                                        anon
                                    </c:when>
                                    <c:when test="${not empty review.userId}">
                                        Usuario #<c:out value="${review.userId}"/>
                                    </c:when>
                                    <c:otherwise>
                                        Usuario sin identificar
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            <span><c:out value="${review.createdAt}"/></span>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
