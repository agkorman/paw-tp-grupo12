<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<section class="latest-review-section" aria-label="Ultima review">
    <h2>Ultima review</h2>
    <c:choose>
        <c:when test="${empty reviews}">
            <div class="last-review-empty">
                Todavia no hay reviews para este auto.
            </div>
        </c:when>
        <c:otherwise>
            <c:set var="latestReview" value="${reviews[0]}"/>
            <article class="last-review-item">
                <div class="last-review-top">
                    <strong><c:out value="${latestReview.title}"/></strong>
                    <span class="rating-pill"><c:out value="${latestReview.rating}"/>/5.0</span>
                </div>
                <p class="last-review-body"><c:out value="${latestReview.body}"/></p>
                <div class="review-meta last-review-meta">
                    <span>Usuario #<c:out value="${latestReview.userId}"/></span>
                    <span><c:out value="${latestReview.createdAt}"/></span>
                </div>
            </article>
        </c:otherwise>
    </c:choose>
</section>
