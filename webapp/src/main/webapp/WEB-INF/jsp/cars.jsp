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

    <div class="filter-bar">
        <div class="filters">
            <pa:filter-chip label="Brand: All"/>
            <pa:filter-chip label="Body: All"/>
            <pa:filter-chip label="Generation: All"/>
        </div>
        <div class="filter-meta">
            <c:if test="${not empty cars}">
                <span class="count-label">${fn:length(cars)} vehicles found</span>
            </c:if>
            <pa:button text="Apply Focus" variant="primary"/>
        </div>
    </div>

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
                        <pa:car-card
                            model="${car.model}"
                            generation="${car.generation}"
                            bodyType="${car.bodyType}"
                            imageUrl="${car.imageUrl}"/>
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
