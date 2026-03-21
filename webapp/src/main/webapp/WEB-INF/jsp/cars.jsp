<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cars</title>
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <style>
        body { font-family: sans-serif; margin: 2rem; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }
        th { background-color: #f4f4f4; }
        tr:nth-child(even) { background-color: #fafafa; }
    </style>
</head>
<body>
    <h1>Cars</h1>

    <c:choose>
        <c:when test="${empty cars}">
            <p>No cars found.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead>
                    <tr>
                        <th>Brand</th>
                        <th>Model</th>
                        <th>Generation</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="car" items="${cars}">
                        <tr>
                            <td><c:out value="${car.brand}"/></td>
                            <td><c:out value="${car.model}"/></td>
                            <td><c:out value="${car.generation}"/></td>
                            <td><c:out value="${car.description}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>

    <p><a href="<c:url value='/'/>">Back to home</a></p>
</body>
</html>
