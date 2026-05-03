<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="La Posta Autos" styles="/css/cars.css|/css/reviews.css"/>
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
            criteria="${criteria}"
            vehicleCount="${resultCount}"/>

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

    <c:if test="${showSubmittedToast}">
        <pa:submitted-toast messageCode="${submittedToastMessageCode}"/>
    </c:if>

    <pa:script src="/js/reactions.js"/>
    <pa:script src="/js/enhanced-filters.js"/>
    <pa:script src="/js/cars-filters-panel.js"/>
    <pa:script src="/js/auth-required-modal.js"/>
    <pa:script src="/js/form-submit-lock.js"/>
    <pa:script src="/js/submitted-toast.js"/>

    <pa:footer/>
</body>
</html>
