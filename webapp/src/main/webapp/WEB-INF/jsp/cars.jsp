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

    <pa:nav activePage="reviews"/>

    <form class="filter-bar" method="get" action="<c:url value='/cars'/>" id="car-filter-form">
        <div class="filters">
            <div class="filter-row filter-row-search">
                <label class="filter-row-label" for="filter-query">Search</label>
                <div class="filter-search-wrap">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" aria-hidden="true">
                        <circle cx="11" cy="11" r="7"></circle>
                        <path d="m20 20-3.5-3.5"></path>
                    </svg>
                    <input
                            class="filter-search-input"
                            id="filter-query"
                            type="search"
                            name="q"
                            value="<c:out value='${searchQuery}'/>"
                            placeholder="Buscar marcas, modelos o carrocerías...">
                </div>
            </div>
            <div class="filter-row">
                <label class="filter-row-label" for="filter-brand">Brand</label>
                <select class="filter-select" id="filter-brand" name="brand"
                        onchange="document.getElementById('car-filter-form').submit()">
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
                <select class="filter-select" id="filter-body" name="bodyType"
                        onchange="document.getElementById('car-filter-form').submit()">
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
            <span class="count-label">${fn:length(cars)} vehicles found</span>
            <button type="submit" class="btn-primary">Apply filters</button>
        </div>
    </form>

    <section class="catalog-section">
        <c:choose>
            <c:when test="${empty cars}">
                <div class="empty-state">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                        <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                    </svg>
                    <p>No vehicles found in the gallery.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="cars-grid">
                    <c:forEach var="car" items="${cars}">
                        <c:url var="reviewUrl" value="/reviews">
                            <c:param name="carId" value="${car.id}"/>
                        </c:url>
                        <pa:car-card
                            model="${car.model}"
                            bodyType="${car.bodyType}"
                            imageUrl="${car.imageUrl}"
                            href="${reviewUrl}"
                            averageRating="${reviewStatsByCarId[car.id].averageRating}"
                            reviewCount="${reviewStatsByCarId[car.id].reviewCount}"/>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <c:if test="${not empty cars}">
        <div class="discover-wrap">
            <pa:button text="Discover More" variant="secondary" icon="chevron-down"/>
        </div>
    </c:if>

    <pa:footer/>

</body>
</html>
