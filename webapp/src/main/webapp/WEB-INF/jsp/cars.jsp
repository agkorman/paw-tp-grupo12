<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="La Posta Autos" styles="/css/cars.css|/css/reviews.css|/css/review-tags.css"/>
<body>
    <pa:nav activePage="reviews"/>
    <c:set var="resultCount" value="${empty totalItems ? fn:length(cars) : totalItems}"/>

    <pa:cars-toolbar
            brands="${brands}"
            bodyTypes="${bodyTypes}"
            selectedBrand="${selectedBrand}"
            selectedBodyType="${selectedBodyType}"
            searchQuery="${searchQuery}"
            sortBy="${criteria.sortBy}"
            hasAdvancedFilters="${hasAdvancedFilters}"/>

    <pa:cars-filters-panel
            criteria="${criteria}"/>

    <c:if test="${not empty criteria.tagCodes}">
        <div class="active-tag-filter">
            <span class="active-tag-filter-label"><spring:message code="cars.activeTag.label"/></span>
            <c:forEach var="activeTagCode" items="${criteria.tagCodes}">
                <c:set var="activeTag" value="${reviewTagsByCode[activeTagCode]}"/>
                <c:if test="${not empty activeTag}">
                    <c:url var="clearTagUrl" value="/cars">
                        <c:if test="${not empty criteria.q}"><c:param name="q" value="${criteria.q}"/></c:if>
                        <c:if test="${not empty criteria.brand}"><c:param name="brand" value="${criteria.brand}"/></c:if>
                        <c:if test="${not empty criteria.bodyType}"><c:param name="bodyType" value="${criteria.bodyType}"/></c:if>
                        <c:if test="${not empty criteria.yearMin}"><c:param name="yearMin" value="${criteria.yearMin}"/></c:if>
                        <c:if test="${not empty criteria.yearMax}"><c:param name="yearMax" value="${criteria.yearMax}"/></c:if>
                        <c:if test="${not empty criteria.fuelType}"><c:param name="fuelType" value="${criteria.fuelType}"/></c:if>
                        <c:if test="${not empty criteria.horsepowerMin}"><c:param name="horsepowerMin" value="${criteria.horsepowerMin}"/></c:if>
                        <c:if test="${not empty criteria.horsepowerMax}"><c:param name="horsepowerMax" value="${criteria.horsepowerMax}"/></c:if>
                        <c:if test="${not empty criteria.airbagMin}"><c:param name="airbagMin" value="${criteria.airbagMin}"/></c:if>
                        <c:if test="${not empty criteria.transmission}"><c:param name="transmission" value="${criteria.transmission}"/></c:if>
                        <c:if test="${not empty criteria.fuelConsumptionMax and not criteria.electricOnly}"><c:param name="fuelConsumptionMax" value="${criteria.fuelConsumptionMax}"/></c:if>
                        <c:if test="${not empty criteria.maxSpeedMin}"><c:param name="maxSpeedMin" value="${criteria.maxSpeedMin}"/></c:if>
                        <c:if test="${not empty criteria.priceMin}"><c:param name="priceMin" value="${criteria.priceMin}"/></c:if>
                        <c:if test="${not empty criteria.priceMax}"><c:param name="priceMax" value="${criteria.priceMax}"/></c:if>
                        <c:if test="${not empty criteria.sortBy}"><c:param name="sortBy" value="${criteria.sortBy}"/></c:if>
                        <c:forEach var="otherTagCode" items="${criteria.tagCodes}">
                            <c:if test="${otherTagCode ne activeTagCode}">
                                <c:param name="tagCode" value="${otherTagCode}"/>
                            </c:if>
                        </c:forEach>
                    </c:url>
                    <c:set var="tagEmojiKey" value="review.tag.emoji.${activeTag.code}"/>
                    <spring:message code="review.tag.emoji.fallback" var="tagEmojiFallback" text="🏷️"/>
                    <spring:message code="${tagEmojiKey}" var="tagEmojiDisplay" text="${tagEmojiFallback}"/>
                    <spring:message var="clearTagLabel" code="cars.activeTag.clear"/>
                    <span class="review-tag-chip review-tag-chip--display review-tag-chip--${activeTag.sentiment}">
                        <span class="review-tag-chip-glyph" aria-hidden="true"><c:out value="${tagEmojiDisplay}"/></span>
                        <span class="review-tag-chip-label"><pa:review-tag-label tag="${activeTag}"/></span>
                        <a class="active-tag-filter-clear" href="${fn:escapeXml(clearTagUrl)}" aria-label="${fn:escapeXml(clearTagLabel)}">
                            <pa:icon name="close" size="14"/>
                        </a>
                    </span>
                </c:if>
            </c:forEach>
        </div>
    </c:if>

    <pa:cars-content
            cars="${cars}"
            resultCount="${resultCount}"
            reviewStatsByCarId="${reviewStatsByCarId}"
            showHp="${showHp}"
            showSpeed="${showSpeed}"
            showConsumption="${showConsumption}"
            showAirbags="${showAirbags}"
            showTransmission="${showTransmission}"
            showFuelType="${showFuelType}"
            showPrice="${showPrice}"
            showYear="${showYear}"
            currentPage="${currentPage}"
            totalPages="${totalPages}"
            criteria="${criteria}"/>
    <c:choose>
        <c:when test="${showSubmittedToast}">
            <pa:toast messageCode="${submittedToastMessageCode}"/>
        </c:when>
        <c:otherwise>
            <pa:toast/>
        </c:otherwise>
    </c:choose>

    <pa:script src="/js/cars/cars-toolbar.js"/>
    <pa:script src="/js/cars/cars-filters-panel.js"/>
    <pa:script src="/js/shared/modal-utils.js"/>
    <pa:script src="/js/shared/toast.js"/>

    <pa:footer/>
</body>
</html>
