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

    <pa:cars-toolbar
            brands="${brands}"
            bodyTypes="${bodyTypes}"
            selectedBrand="${selectedBrand}"
            selectedBodyType="${selectedBodyType}"
            searchQuery="${searchQuery}"
            resultCount="${fn:length(cars)}"/>

    <pa:cars-content cars="${cars}" reviewStatsByCarId="${reviewStatsByCarId}"/>

    <pa:footer/>
    <script src="<c:url value='/js/enhanced-filters.js'/>"></script>

</body>
</html>
