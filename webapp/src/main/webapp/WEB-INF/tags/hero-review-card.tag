<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="heroReview" required="true" type="ar.edu.itba.paw.model.Review" %>
<%@ attribute name="heroCarBrandName" required="false" type="java.lang.String" %>
<%@ attribute name="heroCarImageUrl" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<article class="hero-review-card">
    <div class="hero-review-header">
        <div class="hero-review-thumb" aria-hidden="true">
            <c:choose>
                <c:when test="${not empty heroCarImageUrl}">
                    <img src="${fn:escapeXml(heroCarImageUrl)}" alt="" class="hero-review-thumb-image">
                </c:when>
                <c:otherwise>
                    <span><c:out value="${fn:substring(heroCarBrandName, 0, 1)}"/></span>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="hero-review-rating">
            <span class="hero-review-stars" aria-hidden="true">
                <c:forEach var="i" begin="1" end="5">
                    <svg viewBox="0 0 24 24" width="18" height="18">
                        <c:choose>
                            <c:when test="${heroReview.rating >= i}">
                                <path fill="#ff5719" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </c:when>
                            <c:when test="${heroReview.rating >= i - 0.5}">
                                <defs>
                                    <linearGradient id="heroStar${heroReview.id}${i}" x1="0" x2="1" y1="0" y2="0">
                                        <stop offset="50%" stop-color="#ff5719"/>
                                        <stop offset="50%" stop-color="#3a3a3a"/>
                                    </linearGradient>
                                </defs>
                                <path fill="url(#heroStar${heroReview.id}${i})" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </c:when>
                            <c:otherwise>
                                <path fill="#3a3a3a" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                            </c:otherwise>
                        </c:choose>
                    </svg>
                </c:forEach>
            </span>
            <span class="hero-review-score"><c:out value="${heroReview.rating}"/></span>
        </div>
    </div>

    <p class="hero-review-quote">
        <c:choose>
            <c:when test="${not empty heroReview.body and fn:length(heroReview.body) gt 150}">
                “<c:out value="${fn:substring(heroReview.body, 0, 150)}"/>...”
            </c:when>
            <c:when test="${not empty heroReview.body}">
                “<c:out value="${heroReview.body}"/>”
            </c:when>
            <c:when test="${not empty heroReview.title}">
                “<c:out value="${heroReview.title}"/>”
            </c:when>
            <c:otherwise>
                “Una reseña reciente de la comunidad sobre este modelo destacado.”
            </c:otherwise>
        </c:choose>
    </p>

    <p class="hero-review-meta">
        <span>anon</span>
        <span><c:out value="${fn:substring(heroReview.createdAt, 0, 10)}"/></span>
    </p>
</article>
