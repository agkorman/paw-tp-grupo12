<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
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

    <c:if test="${not empty criteria.tagCodes}">
        <spring:message code="cars.activeTag.clear" var="clearTagAria"/>
        <div class="active-tag-filter" data-active-tag-filter>
            <span class="active-tag-filter-label"><spring:message code="cars.activeTag.label"/></span>
            <c:forEach var="activeTagCode" items="${criteria.tagCodes}">
                <c:set var="activeTag" value="${reviewTagsByCode[activeTagCode]}"/>
                <c:if test="${not empty activeTag}">
                    <c:set var="activeTagEmojiKey" value="review.tag.emoji.${activeTag.code}"/>
                    <spring:message code="review.tag.emoji.fallback" var="activeTagEmojiFallback" text="🏷️"/>
                    <spring:message code="${activeTagEmojiKey}" var="activeTagEmoji" text="${activeTagEmojiFallback}"/>
                    <c:url var="removeTagUrl" value="/cars">
                        <c:forEach var="entry" items="${paramValues}">
                            <c:if test="${entry.key ne 'tagCode' and entry.key ne 'page'}">
                                <c:forEach var="entryValue" items="${entry.value}">
                                    <c:if test="${not empty entryValue}">
                                        <c:param name="${entry.key}" value="${entryValue}"/>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                        </c:forEach>
                        <c:forEach var="otherCode" items="${criteria.tagCodes}">
                            <c:if test="${otherCode ne activeTag.code}">
                                <c:param name="tagCode" value="${otherCode}"/>
                            </c:if>
                        </c:forEach>
                    </c:url>
                    <span class="review-tag-chip review-tag-chip--display review-tag-chip--${activeTag.sentiment}">
                        <span class="review-tag-chip-glyph" aria-hidden="true"><c:out value="${activeTagEmoji}"/></span>
                        <span class="review-tag-chip-label"><pa:review-tag-label tag="${activeTag}"/></span>
                        <a class="active-tag-filter-clear" href="${removeTagUrl}" aria-label="${fn:escapeXml(clearTagAria)}">
                            <span aria-hidden="true">&times;</span>
                        </a>
                    </span>
                </c:if>
            </c:forEach>
        </div>
    </c:if>

    <pa:cars-filters-panel
            criteria="${criteria}"/>

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
    <pa:auth-required-modal/>

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
    <pa:script src="/js/auth/auth-required-modal.js"/>
    <pa:script src="/js/shared/form-submit-lock.js"/>
    <pa:script src="/js/shared/toast.js"/>

    <pa:footer/>
</body>
</html>
