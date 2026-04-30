<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
<c:url var="reviewLikeUrl" value="/reviews/${review.id}/like"/>
<c:url var="goToReviewUrl" value="/reviews">
    <c:param name="carId" value="${car.id}"/>
</c:url>
<c:set var="goToReviewHref" value="${goToReviewUrl}#review-${review.id}"/>

<div class="review-detail-content" data-review-detail-content>
    <header class="review-detail-header">
        <div class="review-detail-author">
            <span class="review-detail-author-kicker">Reseña de</span>
            <c:choose>
                <c:when test="${not empty review.userId}">
                    <c:url var="authorProfileUrl" value="/profiles/${review.userId}"/>
                    <a class="review-detail-author-name" href="${authorProfileUrl}">
                        <c:choose>
                            <c:when test="${not empty review.reviewerUsername}"><c:out value="${review.reviewerUsername}"/></c:when>
                            <c:when test="${not empty review.reviewerEmail}"><c:out value="${review.reviewerEmail}"/></c:when>
                            <c:otherwise>Usuario</c:otherwise>
                        </c:choose>
                    </a>
                </c:when>
                <c:otherwise>
                    <span class="review-detail-author-name">
                        <c:choose>
                            <c:when test="${not empty review.reviewerUsername}"><c:out value="${review.reviewerUsername}"/></c:when>
                            <c:otherwise>anon</c:otherwise>
                        </c:choose>
                    </span>
                </c:otherwise>
            </c:choose>
            <c:if test="${not empty timeAgo}">
                <span class="review-detail-time"><c:out value="${timeAgo}"/></span>
            </c:if>
        </div>
        <a class="btn-primary review-detail-go-btn" href="${goToReviewHref}">
            Ver reseña
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
        </a>
    </header>

    <section class="review-detail-body" aria-label="Reseña">
        <div class="review-detail-title-row">
            <strong class="review-detail-title"><c:out value="${review.title}"/></strong>
            <span class="rating-pill"><c:out value="${review.rating}"/>/5.0</span>
        </div>
        <p class="review-detail-text"><c:out value="${review.body}"/></p>
        <pa:review-tag-chips mode="display" tags="${review.tags}"/>
        <div class="review-detail-meta">
            <pa:review-like-button
                    reviewId="${review.id}"
                    action="${reviewLikeUrl}"
                    liked="${liked}"
                    likeCount="${likeCount}"
                    disabled="${not authenticated}"/>
        </div>
    </section>

    <section class="review-detail-car" aria-label="Datos del auto">
        <h3 class="review-detail-car-title">
            <c:out value="${car.brandName}"/> <c:out value="${car.model}"/>
        </h3>
        <dl class="review-detail-car-specs">
            <div class="review-detail-spec">
                <dt>Marca</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty car.brandName}"><c:out value="${car.brandName}"/></c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="review-detail-spec">
                <dt>Modelo</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty car.model}"><c:out value="${car.model}"/></c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="review-detail-spec">
                <dt>Año</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty car.year}"><c:out value="${car.year}"/></c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="review-detail-spec">
                <dt>Tipo</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty car.bodyType}"><c:out value="${car.bodyType}"/></c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="review-detail-spec">
                <dt>Precio 0 km</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty car.priceUsd}">
                            USD <fmt:formatNumber value="${car.priceUsd}" groupingUsed="true" maxFractionDigits="0"/>
                        </c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="review-detail-spec">
                <dt>Potencia</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty car.horsepower}"><c:out value="${car.horsepower}"/> HP</c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="review-detail-spec">
                <dt>Transmisión</dt>
                <dd>
                    <c:choose>
                        <c:when test="${car.transmission eq 'manual'}">Manual</c:when>
                        <c:when test="${car.transmission eq 'automatic'}">Automática</c:when>
                        <c:when test="${not empty car.transmission}"><c:out value="${car.transmission}"/></c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
            <div class="review-detail-spec">
                <dt>Velocidad máxima</dt>
                <dd>
                    <c:choose>
                        <c:when test="${not empty car.maxSpeedKmh}"><c:out value="${car.maxSpeedKmh}"/> km/h</c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </dd>
            </div>
        </dl>
    </section>
</div>
