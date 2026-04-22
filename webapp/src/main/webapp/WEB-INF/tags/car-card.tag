<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="model"      required="true" %>
<%@ attribute name="bodyType"   required="false" %>
<%@ attribute name="carId"      required="true" %>
<%@ attribute name="hasImage"   required="true" %>
<%@ attribute name="href"       required="true" %>
<%@ attribute name="averageRating" required="false" %>
<%@ attribute name="reviewCount" required="false" %>
<%@ attribute name="imageUrl" required="false" %>
<%@ attribute name="submitter" required="false" %>
<%@ attribute name="footerText" required="false" %>
<%@ attribute name="actionText" required="false" %>
<%@ attribute name="showFavorite" required="false" %>
<%@ attribute name="favorited" required="false" %>
<%@ attribute name="horsepower" required="false" %>
<%@ attribute name="maxSpeedKmh" required="false" %>
<%@ attribute name="fuelConsumption" required="false" %>
<%@ attribute name="airbagCount" required="false" %>
<%@ attribute name="transmission" required="false" %>
<%@ attribute name="fuelType" required="false" %>
<%@ attribute name="showHp" required="false" %>
<%@ attribute name="showSpeed" required="false" %>
<%@ attribute name="showConsumption" required="false" %>
<%@ attribute name="showAirbags" required="false" %>
<%@ attribute name="showTransmission" required="false" %>
<%@ attribute name="showFuelType" required="false" %>
<%@ attribute name="openModal" required="false" %>
<%@ attribute name="requestId" required="false" %>
<%@ attribute name="requestBrand" required="false" %>
<%@ attribute name="requestModel" required="false" %>
<%@ attribute name="requestBodyType" required="false" %>
<%@ attribute name="requestDescription" required="false" %>
<%@ attribute name="requestSubmitter" required="false" %>
<%@ attribute name="requestImageUrls" required="false" %>
<%@ attribute name="requestFuelType" required="false" %>
<%@ attribute name="requestHorsepower" required="false" %>
<%@ attribute name="requestAirbagCount" required="false" %>
<%@ attribute name="requestTransmission" required="false" %>
<%@ attribute name="requestFuelConsumption" required="false" %>
<%@ attribute name="requestMaxSpeedKmh" required="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:set var="modalImageUrl" value=""/>
<c:if test="${not empty imageUrl}">
    <c:url var="modalImageUrl" value="${imageUrl}"/>
</c:if>
<c:set var="modalImageUrls" value=""/>
<c:if test="${not empty requestImageUrls}">
    <c:forEach var="requestImageUrl" items="${fn:split(requestImageUrls, '|')}">
        <c:if test="${not empty requestImageUrl}">
            <c:url var="resolvedRequestImageUrl" value="${requestImageUrl}"/>
            <c:set var="modalImageUrls" value="${modalImageUrls}${empty modalImageUrls ? '' : '|'}${resolvedRequestImageUrl}"/>
        </c:if>
    </c:forEach>
</c:if>

<div class="car-card-shell">
    <a href="${fn:escapeXml(href)}"
       class="car-card-link"
       aria-label="Ver ${fn:escapeXml(model)}"
       data-open-create-car-modal="${openModal ? 'true' : 'false'}"
       data-request-id="${fn:escapeXml(requestId)}"
       data-request-brand="${fn:escapeXml(requestBrand)}"
       data-request-model="${fn:escapeXml(requestModel)}"
       data-request-body-type="${fn:escapeXml(requestBodyType)}"
       data-request-description="${fn:escapeXml(requestDescription)}"
       data-request-submitter="${fn:escapeXml(requestSubmitter)}"
       data-request-fuel-type="${fn:escapeXml(requestFuelType)}"
       data-request-horsepower="${fn:escapeXml(requestHorsepower)}"
       data-request-airbag-count="${fn:escapeXml(requestAirbagCount)}"
       data-request-transmission="${fn:escapeXml(requestTransmission)}"
       data-request-fuel-consumption="${fn:escapeXml(requestFuelConsumption)}"
       data-request-max-speed-kmh="${fn:escapeXml(requestMaxSpeedKmh)}"
       data-request-image-url="${fn:escapeXml(modalImageUrl)}"
       data-request-image-urls="${fn:escapeXml(modalImageUrls)}">
        <div class="car-card">
            <div class="card-image-wrap">
                <c:choose>
                    <c:when test="${hasImage}">
                        <c:choose>
                            <c:when test="${not empty imageUrl}">
                                <c:url var="resolvedImageUrl" value="${imageUrl}"/>
                            </c:when>
                            <c:otherwise>
                                <c:url var="resolvedImageUrl" value="/car-image">
                                    <c:param name="carId" value="${carId}"/>
                                </c:url>
                            </c:otherwise>
                        </c:choose>
                        <img src="${resolvedImageUrl}" alt="${fn:escapeXml(model)}" loading="lazy">
                    </c:when>
                    <c:otherwise>
                        <div class="img-placeholder">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                                <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                            </svg>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">
                <span class="card-category">
                    <c:choose>
                        <c:when test="${not empty bodyType}"><c:out value="${bodyType}"/></c:when>
                        <c:otherwise>Vehículo</c:otherwise>
                    </c:choose>
                </span>
                <div class="card-title-row">
                    <span class="card-title"><c:out value="${model}"/></span>
                </div>
                <c:if test="${showHp eq 'true' or showSpeed eq 'true' or showConsumption eq 'true' or showAirbags eq 'true' or showTransmission eq 'true' or showFuelType eq 'true'}">
                <div class="card-spec-tags">
                    <c:if test="${showHp eq 'true'}">
                        <span class="card-spec-tag">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
                            </svg>
                            <c:choose>
                                <c:when test="${not empty horsepower}"><c:out value="${horsepower}"/> HP</c:when>
                                <c:otherwise>-- HP</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showSpeed eq 'true'}">
                        <span class="card-spec-tag">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M12 12m-10 0a10 10 0 1 0 20 0a10 10 0 1 0-20 0"/>
                                <path d="M12 12l4.5-4.5"/>
                                <path d="M12 7v1"/>
                                <path d="M17 12h-1"/>
                            </svg>
                            <c:choose>
                                <c:when test="${not empty maxSpeedKmh}"><c:out value="${maxSpeedKmh}"/> km/h</c:when>
                                <c:otherwise>-- km/h</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showConsumption eq 'true'}">
                        <span class="card-spec-tag">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M12 22a7 7 0 0 0 7-7c0-2-1-3.9-3-5.5s-3.5-4-4-6.5c-.5 2.5-2 4.9-4 6.5C6 11.1 5 13 5 15a7 7 0 0 0 7 7z"/>
                            </svg>
                            <c:choose>
                                <c:when test="${not empty fuelConsumption}"><c:out value="${fuelConsumption}"/>L/100km</c:when>
                                <c:otherwise>--L/100km</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showAirbags eq 'true'}">
                        <span class="card-spec-tag">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                            </svg>
                            <c:choose>
                                <c:when test="${not empty airbagCount}"><c:out value="${airbagCount}"/> Airbags</c:when>
                                <c:otherwise>-- Airbags</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showTransmission eq 'true'}">
                        <span class="card-spec-tag">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14"/>
                                <path d="M4.93 4.93a10 10 0 0 0 0 14.14"/>
                            </svg>
                            <c:choose>
                                <c:when test="${transmission eq 'automatic'}">Auto</c:when>
                                <c:when test="${transmission eq 'manual'}">Manual</c:when>
                                <c:otherwise>--</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showFuelType eq 'true'}">
                        <span class="card-spec-tag">
                            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/>
                            </svg>
                            <c:choose>
                                <c:when test="${fuelType eq 'hybrid'}">Híbrido</c:when>
                                <c:when test="${fuelType eq 'electric'}">Eléctrico</c:when>
                                <c:when test="${fuelType eq 'combustion'}">Combustión</c:when>
                                <c:otherwise>--</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                </div>
                </c:if>
                <div class="card-rating-row">
                    <c:choose>
                        <c:when test="${not empty submitter}">
                            <span class="card-rating-empty">Enviado por <c:out value="${submitter}"/></span>
                        </c:when>
                        <c:when test="${reviewCount gt 0}">
                            <span class="card-rating-badge">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                                    <path d="M12 2.75l2.91 5.9 6.51.95-4.71 4.59 1.11 6.48L12 17.62l-5.82 3.05 1.11-6.48-4.71-4.59 6.51-.95L12 2.75z"/>
                                </svg>
                                <span class="card-rating-value"><c:out value="${averageRating}"/></span>
                            </span>
                            <span class="card-rating-count">
                                <c:out value="${reviewCount}"/>
                                <c:choose>
                                    <c:when test="${reviewCount eq 1}">reseña</c:when>
                                    <c:otherwise>reseñas</c:otherwise>
                                </c:choose>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="card-rating-empty">Sin reseñas todavía</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="card-footer">
                    <span class="card-meta">
                        <c:choose>
                            <c:when test="${not empty footerText}"><c:out value="${footerText}"/></c:when>
                            <c:when test="${reviewCount gt 0}">Puntaje de la comunidad sobre 5</c:when>
                            <c:otherwise>Comparte la primera impresión</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="card-specs-link">
                        <c:choose>
                            <c:when test="${not empty actionText}"><c:out value="${actionText}"/></c:when>
                            <c:otherwise>Ver reseñas</c:otherwise>
                        </c:choose>
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <path d="M5 12h14M12 5l7 7-7 7"/>
                        </svg>
                        </span>
                </div>
            </div>
        </div>
    </a>
    <c:if test="${showFavorite ne false}">
        <div class="car-card-favorite">
            <pa:car-favorite-button carId="${carId}" favorited="${favorited}"/>
        </div>
    </c:if>
</div>
