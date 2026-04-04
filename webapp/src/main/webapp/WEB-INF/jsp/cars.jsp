<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/cars.css'/>">
</head>
<body>

    <pa:nav activePage="explore"/>

    <c:url var="carsContentUrl" value="/cars/content"/>

    <form class="filter-bar" method="get" action="<c:url value='/cars'/>" id="car-filter-form"
          data-enhanced-filter="true"
          data-fragment-url="${carsContentUrl}"
          data-target="#carsCatalogContent"
          data-auto-submit="true">
        <div class="filters">
            <div class="filter-row">
                <label class="filter-row-label" for="filter-brand">Brand</label>
                <select class="filter-select" id="filter-brand" name="brand">
                    <option value="" <c:if test="${empty selectedBrand}">selected</c:if>>All brands</option>
                    <c:forEach items="${brands}" var="b">
                        <option value="<c:out value='${b.name}'/>" <c:if test="${selectedBrand eq b.name}">selected</c:if>>
                            <c:out value="${b.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
            <div class="filter-row">
                <label class="filter-row-label" for="filter-body">Body</label>
                <select class="filter-select" id="filter-body" name="bodyType">
                    <option value="" <c:if test="${empty selectedBodyType}">selected</c:if>>All body types</option>
                    <c:forEach items="${bodyTypes}" var="bt">
                        <option value="<c:out value='${bt.name}'/>" <c:if test="${selectedBodyType eq bt.name}">selected</c:if>>
                            <c:out value="${bt.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="filter-meta">
            <noscript>
                <button type="submit" class="btn-primary">Apply filters</button>
            </noscript>
        </div>
    </form>

    <pa:cars-content cars="${cars}" reviewStatsByCarId="${reviewStatsByCarId}"/>

    <pa:footer/>
    <script src="<c:url value='/js/enhanced-filters.js'/>"></script>

</body>
</html>
