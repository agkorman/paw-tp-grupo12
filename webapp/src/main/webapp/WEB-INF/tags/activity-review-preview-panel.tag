<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewCard" required="true" type="ar.edu.itba.paw.webapp.controller.ActivityController.ActivityReviewCard" %>
<%@ attribute name="idPrefix" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="activityReviewUrl" value="/reviews">
    <c:param name="carId" value="${reviewCard.review.carId}"/>
    <c:if test="${reviewCard.reviewPage gt 1}">
        <c:param name="page" value="${reviewCard.reviewPage}"/>
    </c:if>
</c:url>
<c:set var="activityReviewHref" value="${activityReviewUrl}#review-${reviewCard.review.id}"/>
<c:set var="panelId" value="${idPrefix}-${reviewCard.review.id}"/>
<c:set var="panelTitleId" value="${panelId}-title"/>
<c:set var="selectedCar" value="${reviewCard.car}"/>
<c:set var="carTitle">
    <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/>
</c:set>

<c:if test="${not empty selectedCar}">
    <section id="${fn:escapeXml(panelId)}"
             class="activity-review-preview-panel"
             data-activity-preview-panel
             aria-labelledby="${fn:escapeXml(panelTitleId)}"
             hidden>
        <button type="button"
                class="review-modal-close review-preview-close activity-preview-close"
                data-close-activity-preview
                aria-label="Cerrar preview">
            <pa:icon name="close" size="20"/>
        </button>

        <div class="review-preview-layout activity-preview-layout">
            <aside class="review-preview-car" aria-label="Información del auto">
                <div class="review-preview-image">
                    <c:choose>
                        <c:when test="${selectedCar.hasImage}">
                            <c:url var="previewCarImageUrl" value="/car-image">
                                <c:param name="carId" value="${selectedCar.id}"/>
                            </c:url>
                            <img src="${previewCarImageUrl}" alt="${fn:escapeXml(carTitle)}">
                        </c:when>
                        <c:otherwise>
                            <div class="review-preview-image-placeholder" aria-hidden="true">
                                <pa:icon name="car-placeholder" size="58"/>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="review-preview-car-copy">
                    <span class="review-preview-kicker">
                        <c:choose>
                            <c:when test="${not empty selectedCar.bodyType}"><c:out value="${selectedCar.bodyType}"/></c:when>
                            <c:otherwise>Auto reseñado</c:otherwise>
                        </c:choose>
                    </span>
                    <h2><c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/></h2>
                    <c:if test="${not empty selectedCar.year}">
                        <span class="review-preview-year"><c:out value="${selectedCar.year}"/></span>
                    </c:if>
                </div>

                <dl class="review-preview-specs">
                    <c:if test="${not empty selectedCar.priceUsd}">
                        <div class="review-preview-spec">
                            <dt><pa:icon name="dollar" size="20"/><span>Precio 0 km</span></dt>
                            <dd>USD <fmt:formatNumber value="${selectedCar.priceUsd}" groupingUsed="true" maxFractionDigits="0"/></dd>
                        </div>
                    </c:if>
                    <c:if test="${not empty selectedCar.fuelType}">
                        <div class="review-preview-spec">
                            <dt><pa:icon name="${selectedCar.fuelType eq 'electric' ? 'eco' : 'gas-pump'}" size="20"/><span>Motorización</span></dt>
                            <dd>
                                <c:choose>
                                    <c:when test="${selectedCar.fuelType eq 'combustion'}">Combustión</c:when>
                                    <c:when test="${selectedCar.fuelType eq 'hybrid'}">Híbrido</c:when>
                                    <c:when test="${selectedCar.fuelType eq 'electric'}">Eléctrico</c:when>
                                    <c:otherwise><c:out value="${selectedCar.fuelType}"/></c:otherwise>
                                </c:choose>
                            </dd>
                        </div>
                    </c:if>
                    <c:if test="${not empty selectedCar.transmission}">
                        <div class="review-preview-spec">
                            <dt><pa:icon name="car" size="20"/><span>Transmisión</span></dt>
                            <dd>
                                <c:choose>
                                    <c:when test="${selectedCar.transmission eq 'manual'}">Manual</c:when>
                                    <c:when test="${selectedCar.transmission eq 'automatic'}">Automática</c:when>
                                    <c:otherwise><c:out value="${selectedCar.transmission}"/></c:otherwise>
                                </c:choose>
                            </dd>
                        </div>
                    </c:if>
                    <c:if test="${not empty selectedCar.horsepower}">
                        <div class="review-preview-spec">
                            <dt><pa:icon name="bolt" size="20"/><span>Potencia</span></dt>
                            <dd><c:out value="${selectedCar.horsepower}"/> HP</dd>
                        </div>
                    </c:if>
                    <c:if test="${not empty selectedCar.fuelConsumption}">
                        <div class="review-preview-spec">
                            <dt><pa:icon name="droplet" size="20"/><span>Consumo</span></dt>
                            <dd><c:out value="${selectedCar.fuelConsumption}"/> L/100 km</dd>
                        </div>
                    </c:if>
                    <c:if test="${not empty selectedCar.maxSpeedKmh}">
                        <div class="review-preview-spec">
                            <dt><pa:icon name="speedometer" size="20"/><span>Velocidad máxima</span></dt>
                            <dd><c:out value="${selectedCar.maxSpeedKmh}"/> km/h</dd>
                        </div>
                    </c:if>
                    <c:if test="${not empty selectedCar.airbagCount}">
                        <div class="review-preview-spec">
                            <dt><pa:icon name="shield" size="20"/><span>Airbags</span></dt>
                            <dd><c:out value="${selectedCar.airbagCount}"/></dd>
                        </div>
                    </c:if>
                </dl>
            </aside>

            <article class="review-preview-content" aria-label="Preview de la reseña">
                <header class="review-preview-header">
                    <div class="review-preview-author">
                        <span class="review-preview-avatar" aria-hidden="true">
                            <pa:icon name="user-avatar" size="22"/>
                        </span>
                        <div>
                            <pa:review-author-link review="${reviewCard.review}"/>
                            <span><c:out value="${reviewCard.timeAgo}"/></span>
                        </div>
                    </div>
                    <div class="review-preview-rating" aria-label="${reviewCard.review.rating} de 5 estrellas">
                        <span class="review-preview-stars">
                            <pa:rating-stars rating="${reviewCard.review.rating}" size="18" idPrefix="${panelId}Star"/>
                        </span>
                        <span class="review-preview-score">
                            <strong><c:out value="${reviewCard.review.rating}"/></strong>
                            <span>/ 5</span>
                        </span>
                    </div>
                </header>

                <div class="review-preview-body">
                    <h3 id="${fn:escapeXml(panelTitleId)}"><c:out value="${reviewCard.review.title}"/></h3>
                    <p><c:out value="${reviewCard.review.body}"/></p>
                </div>

                <pa:review-tag-chips mode="display" tags="${reviewCard.review.tags}"/>

                <div class="review-preview-actions">
                    <a class="btn-primary" href="${activityReviewHref}">
                        Ver Reseña
                        <pa:icon name="arrow-right" size="14"/>
                    </a>
                </div>
            </article>
        </div>
    </section>
</c:if>
