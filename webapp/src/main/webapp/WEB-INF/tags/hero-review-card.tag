<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="heroReview" required="true" type="ar.edu.itba.paw.model.Review" %>
<%@ attribute name="heroCarBrandName" required="false" type="java.lang.String" %>
<%@ attribute name="heroCarImageUrl" required="false" type="java.lang.String" %>
<%@ attribute name="href" required="false" type="java.lang.String" %>
<%@ attribute name="timeAgo" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<a class="hero-review-card" href="${fn:escapeXml(href)}" aria-label="Ver review destacada">
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
                <pa:rating-stars rating="${heroReview.rating}" size="18" idPrefix="heroStar${heroReview.id}-"/>
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
        <span>
            <c:choose>
                <c:when test="${not empty heroReview.reviewerUsername}">
                    <c:out value="${heroReview.reviewerUsername}"/>
                </c:when>
                <c:when test="${not empty heroReview.reviewerEmail}">
                    <c:out value="${heroReview.reviewerEmail}"/>
                </c:when>
                <c:otherwise>anon</c:otherwise>
            </c:choose>
        </span>
        <span><c:out value="${timeAgo}"/></span>
    </p>
</a>
