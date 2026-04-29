<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="model"      required="true" %>
<%@ attribute name="year"       required="false" %>
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
<%@ attribute name="horsepower" required="false" %>
<%@ attribute name="maxSpeedKmh" required="false" %>
<%@ attribute name="fuelConsumption" required="false" %>
<%@ attribute name="airbagCount" required="false" %>
<%@ attribute name="transmission" required="false" %>
<%@ attribute name="fuelType" required="false" %>
<%@ attribute name="priceUsd" required="false" %>
<%@ attribute name="showHp" required="false" %>
<%@ attribute name="showSpeed" required="false" %>
<%@ attribute name="showConsumption" required="false" %>
<%@ attribute name="showAirbags" required="false" %>
<%@ attribute name="showFuelType" required="false" %>
<%@ attribute name="showPrice" required="false" %>
<%@ attribute name="showYear" required="false" %>
<%@ attribute name="openModal" required="false" %>
<%@ attribute name="requestId" required="false" %>
<%@ attribute name="requestBrand" required="false" %>
<%@ attribute name="requestModel" required="false" %>
<%@ attribute name="requestYear" required="false" %>
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
<%@ attribute name="requestPriceUsd" required="false" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pa"  tagdir="/WEB-INF/tags" %>

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
       <c:if test="${openModal}">
           data-open-create-car-modal="true"
           data-request-id="${fn:escapeXml(requestId)}"
           data-request-brand="${fn:escapeXml(requestBrand)}"
           data-request-model="${fn:escapeXml(requestModel)}"
           data-request-year="${fn:escapeXml(requestYear)}"
           data-request-body-type="${fn:escapeXml(requestBodyType)}"
           data-request-description="${fn:escapeXml(requestDescription)}"
           data-request-submitter="${fn:escapeXml(requestSubmitter)}"
           data-request-fuel-type="${fn:escapeXml(requestFuelType)}"
           data-request-horsepower="${fn:escapeXml(requestHorsepower)}"
           data-request-airbag-count="${fn:escapeXml(requestAirbagCount)}"
           data-request-transmission="${fn:escapeXml(requestTransmission)}"
           data-request-fuel-consumption="${fn:escapeXml(requestFuelConsumption)}"
           data-request-max-speed-kmh="${fn:escapeXml(requestMaxSpeedKmh)}"
           data-request-price-usd="${fn:escapeXml(requestPriceUsd)}"
           data-request-image-url="${fn:escapeXml(modalImageUrl)}"
           data-request-image-urls="${fn:escapeXml(modalImageUrls)}"
       </c:if>>
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
                            <pa:icon name="car-placeholder" size="48"/>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">
                <span class="card-category">
                    <c:choose>
                        <c:when test="${not empty year and not empty bodyType}"><c:out value="${year}"/> · <c:out value="${bodyType}"/></c:when>
                        <c:when test="${not empty year}"><c:out value="${year}"/></c:when>
                        <c:when test="${not empty bodyType}"><c:out value="${bodyType}"/></c:when>
                        <c:otherwise>Vehículo</c:otherwise>
                    </c:choose>
                </span>
                <div class="card-title-row">
                    <span class="card-title"><c:out value="${model}"/></span>
                </div>
                <c:if test="${showHp eq 'true' or showSpeed eq 'true' or showConsumption eq 'true' or showAirbags eq 'true' or showFuelType eq 'true' or showPrice eq 'true' or showYear eq 'true'}">
                <div class="card-spec-tags">
                    <c:if test="${showYear eq 'true'}">
                        <span class="card-spec-tag">
                            <c:choose>
                                <c:when test="${not empty year}"><c:out value="${year}"/></c:when>
                                <c:otherwise>Año N/A</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showHp eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="bolt" size="11"/>
                            <c:choose>
                                <c:when test="${not empty horsepower}"><c:out value="${horsepower}"/> HP</c:when>
                                <c:otherwise>-- HP</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showSpeed eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="speedometer" size="11"/>
                            <c:choose>
                                <c:when test="${not empty maxSpeedKmh}"><c:out value="${maxSpeedKmh}"/> km/h</c:when>
                                <c:otherwise>-- km/h</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showConsumption eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="droplet" size="11"/>
                            <c:choose>
                                <c:when test="${not empty fuelConsumption}"><c:out value="${fuelConsumption}"/>L/100km</c:when>
                                <c:otherwise>--L/100km</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showAirbags eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="shield" size="11"/>
                            <c:choose>
                                <c:when test="${not empty airbagCount}"><c:out value="${airbagCount}"/> Airbags</c:when>
                                <c:otherwise>-- Airbags</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showFuelType eq 'true'}">
                        <span class="card-spec-tag">
                            <c:choose>
                                <c:when test="${fuelType eq 'hybrid'}">
                                    <pa:icon name="eco" size="11" cssClass="card-spec-fuel-icon"/>
                                    Híbrido
                                </c:when>
                                <c:when test="${fuelType eq 'electric'}">
                                    <pa:icon name="bolt" size="11" cssClass="card-spec-fuel-icon"/>
                                    Eléctrico
                                </c:when>
                                <c:when test="${fuelType eq 'combustion'}">
                                    <pa:icon name="gas-pump" size="11" cssClass="card-spec-fuel-icon"/>
                                    Combustión
                                </c:when>
                                <c:otherwise>--</c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showPrice eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="dollar" size="11"/>
                            <c:choose>
                                <c:when test="${not empty priceUsd}">USD <fmt:formatNumber value="${priceUsd}" groupingUsed="true" maxFractionDigits="0"/></c:when>
                                <c:otherwise>-- USD</c:otherwise>
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
                                <pa:icon name="star-filled" size="12"/>
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
                <%-- <div class="card-footer">
                    <span class="card-specs-link">
                        <c:choose>
                            <c:when test="${not empty actionText}"><c:out value="${actionText}"/></c:when>
                            <c:otherwise>Ver reseñas</c:otherwise>
                        </c:choose>
                        <pa:icon name="arrow-right" size="12"/>
                        </span>
                </div> --%>
            </div>
        </div>
    </a>
</div>
