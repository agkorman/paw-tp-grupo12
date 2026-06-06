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
<%@ attribute name="showTransmission" required="false" %>
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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="modalImageUrl" value=""/>
<c:if test="${not empty imageUrl}">
    <c:url var="modalImageUrl" value="${imageUrl}"/>
</c:if>
<c:set var="electricCar" value="${fuelType eq 'electric'}"/>
<c:set var="showVisibleConsumption" value="${showConsumption eq 'true' and not electricCar}"/>
<c:set var="modalImageUrls" value=""/>
<c:if test="${not empty requestImageUrls}">
    <c:forEach var="requestImageUrl" items="${fn:split(requestImageUrls, '|')}">
        <c:if test="${not empty requestImageUrl}">
            <c:url var="resolvedRequestImageUrl" value="${requestImageUrl}"/>
            <c:set var="modalImageUrls" value="${modalImageUrls}${empty modalImageUrls ? '' : '|'}${resolvedRequestImageUrl}"/>
        </c:if>
    </c:forEach>
</c:if>
<spring:message var="viewCarLabel" code="cars.card.view.aria" arguments="${model}"/>

<div class="car-card-shell">
    <a href="${fn:escapeXml(href)}"
       class="car-card-link"
       aria-label="${fn:escapeXml(viewCarLabel)}"
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
                        <c:otherwise><spring:message code="cars.card.vehicle"/></c:otherwise>
                    </c:choose>
                </span>
                <div class="card-title-row">
                    <span class="card-title"><c:out value="${model}"/></span>
                </div>
                <c:if test="${showHp eq 'true' or showSpeed eq 'true' or showVisibleConsumption or showAirbags eq 'true' or showTransmission eq 'true' or showFuelType eq 'true' or showPrice eq 'true' or showYear eq 'true'}">
                <div class="card-spec-tags">
                    <c:if test="${showYear eq 'true'}">
                        <span class="card-spec-tag">
                            <c:choose>
                                <c:when test="${not empty year}"><c:out value="${year}"/></c:when>
                                <c:otherwise><spring:message code="cars.card.year.na"/></c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showHp eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="bolt" size="11"/>
                            <c:choose>
                                <c:when test="${not empty horsepower}"><c:out value="${horsepower}"/> HP</c:when>
                                <c:otherwise><spring:message code="cars.card.horsepower.na"/></c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showSpeed eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="speedometer" size="11"/>
                            <c:choose>
                                <c:when test="${not empty maxSpeedKmh}"><c:out value="${maxSpeedKmh}"/> km/h</c:when>
                                <c:otherwise><spring:message code="cars.card.speed.na"/></c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showVisibleConsumption}">
                        <span class="card-spec-tag">
                            <pa:icon name="droplet" size="11"/>
                            <c:choose>
                                <c:when test="${not empty fuelConsumption}"><c:out value="${fuelConsumption}"/>L/100km</c:when>
                                <c:otherwise><spring:message code="cars.card.consumption.na"/></c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showAirbags eq 'true'}">
                        <span class="card-spec-tag">
                            <pa:icon name="shield" size="11"/>
                            <c:choose>
                                <c:when test="${not empty airbagCount}"><c:out value="${airbagCount}"/> <spring:message code="cars.card.airbags.unit"/></c:when>
                                <c:otherwise><spring:message code="cars.card.airbags.na"/></c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showTransmission eq 'true'}">
                        <span class="card-spec-tag">
                            <c:choose>
                                <c:when test="${transmission eq 'manual'}"><spring:message code="domain.transmission.manual"/></c:when>
                                <c:when test="${transmission eq 'automatic'}"><spring:message code="domain.transmission.automatic"/></c:when>
                                <c:otherwise><spring:message code="cars.card.transmission.na"/></c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                    <c:if test="${showFuelType eq 'true'}">
                        <span class="card-spec-tag">
                            <c:choose>
                                <c:when test="${fuelType eq 'hybrid'}">
                                    <pa:icon name="eco" size="11" cssClass="card-spec-fuel-icon"/>
                                    <spring:message code="domain.fuel.hybrid"/>
                                </c:when>
                                <c:when test="${fuelType eq 'electric'}">
                                    <pa:icon name="bolt" size="11" cssClass="card-spec-fuel-icon"/>
                                    <spring:message code="domain.fuel.electric"/>
                                </c:when>
                                <c:when test="${fuelType eq 'combustion'}">
                                    <pa:icon name="gas-pump" size="11" cssClass="card-spec-fuel-icon"/>
                                    <spring:message code="domain.fuel.combustion"/>
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
                                <c:otherwise><spring:message code="cars.card.price.na"/></c:otherwise>
                            </c:choose>
                        </span>
                    </c:if>
                </div>
                </c:if>
                <div class="card-rating-row">
                    <c:choose>
                        <c:when test="${not empty submitter}">
                            <span class="card-rating-empty"><spring:message code="common.label.sentBy"/> <c:out value="${submitter}"/></span>
                        </c:when>
                        <c:when test="${reviewCount gt 0}">
                            <span class="card-rating-badge">
                                <pa:icon name="star-filled" size="12"/>
                                <span class="card-rating-value"><c:out value="${averageRating}"/></span>
                            </span>
                            <span class="card-rating-count">
                                <c:out value="${reviewCount}"/>
                                <c:choose>
                                    <c:when test="${reviewCount eq 1}"><spring:message code="cars.card.review.singular"/></c:when>
                                    <c:otherwise><spring:message code="cars.card.review.plural"/></c:otherwise>
                                </c:choose>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="card-rating-empty"><spring:message code="cars.card.review.empty"/></span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <%-- <div class="card-footer">
                    <span class="card-specs-link">
                        <c:choose>
                            <c:when test="${not empty actionText}"><c:out value="${actionText}"/></c:when>
                            <c:otherwise><spring:message code="cars.card.review.view"/></c:otherwise>
                        </c:choose>
                        <pa:icon name="arrow-right" size="12"/>
                        </span>
                </div> --%>
            </div>
        </div>
    </a>
</div>
