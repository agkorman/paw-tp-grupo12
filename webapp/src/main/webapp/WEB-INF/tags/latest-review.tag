<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="latestReview" required="false" type="ar.edu.itba.paw.model.Review" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<section class="latest-review-section" aria-label="Última reseña">
    <h2>Última reseña</h2>
    <c:choose>
        <c:when test="${empty latestReview}">
            <div class="last-review-empty">
                Todavía no hay reseñas para este auto.
            </div>
        </c:when>
        <c:otherwise>
            <article class="last-review-item">
                <div class="last-review-top">
                    <strong><c:out value="${latestReview.title}"/></strong>
                    <span class="rating-pill"><c:out value="${latestReview.rating}"/>/5.0</span>
                </div>
                <p class="last-review-body"><c:out value="${latestReview.body}"/></p>
                <div class="review-meta last-review-meta">
                    <span>anon</span>
                    <span><c:out value="${fn:substring(latestReview.createdAt, 0, 10)}"/></span>
                </div>
            </article>
        </c:otherwise>
    </c:choose>
</section>
