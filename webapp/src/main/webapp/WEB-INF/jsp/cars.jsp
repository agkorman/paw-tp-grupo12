<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=6'/>">
    <link rel="stylesheet" href="<c:url value='/css/cars.css?v=8'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
</head>
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

    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/enhanced-filters.js?v=6'/>"></script>
    <script src="<c:url value='/js/cars-filters-panel.js?v=15'/>"></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
    <script src="<c:url value='/js/submitted-toast.js'/>"></script>

    <pa:footer/>
</body>
</html>
