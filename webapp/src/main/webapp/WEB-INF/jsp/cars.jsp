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
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/cars.css'/>">
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
            favoritedCarIds="${favoritedCarIds}"
            showHp="${showHp}"
            showSpeed="${showSpeed}"
            showConsumption="${showConsumption}"
            showAirbags="${showAirbags}"
            showFuelType="${showFuelType}"
            currentPage="${currentPage}"
            totalPages="${totalPages}"
            criteria="${criteria}"/>
    <pa:create-car-modal
            brands="${brands}"
            bodyTypes="${bodyTypes}"/>
    <pa:auth-required-modal/>

    <c:if test="${showSubmittedToast}">
        <div class="submitted-toast" id="submittedToast" role="status" aria-live="polite">
            <svg class="submitted-toast-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
                <circle cx="12" cy="12" r="10" fill="#4caf7a"/>
                <path d="M7.5 12.5l3 3 6-6" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <span class="submitted-toast-text">Tu auto ha sido enviado correctamente y está en proceso de moderación.</span>
            <button type="button" class="submitted-toast-action" data-dismiss-submitted-toast>ENTENDIDO</button>
        </div>
    </c:if>

    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/enhanced-filters.js?v=5'/>"></script>
    <script src="<c:url value='/js/cars-filters-panel.js?v=4'/>"></script>
    <script src="<c:url value='/js/create-car-modal.js'/>"></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
    <script src="<c:url value='/js/submitted-toast.js'/>"></script>

</body>
</html>
