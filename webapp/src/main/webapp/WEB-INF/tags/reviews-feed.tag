<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<section class="reviews-feed">
    <div class="feed-header">
        <h2>Reviews</h2>
        <div class="feed-header-actions">
            <span class="count-label">${fn:length(reviews)} entradas</span>
            <button type="button" class="filter-chip review-filter-btn">Filtrar</button>
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
                            <span>Usuario #<c:out value="${review.userId}"/></span>
                            <span><c:out value="${review.createdAt}"/></span>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>
