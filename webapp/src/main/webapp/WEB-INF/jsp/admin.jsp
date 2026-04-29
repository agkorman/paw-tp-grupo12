<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/admin.css'/>">
</head>
<body>
    <pa:nav activePage="admin"/>

    <main class="admin-page">
        <section class="admin-hero">
            <div class="admin-hero-copy">
                <div class="admin-hero-heading">
                    <h1>Panel de administración</h1>

                    <div class="admin-section-actions">
                        <form method="post" action="<c:url value='/admin/digest/preview'/>">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button type="submit" class="btn-secondary">
                                Enviar resumen semanal
                            </button>
                        </form>
                        <div class="admin-status" aria-label="Solicitudes pendientes">
                            <span>Pendientes</span>
                            <strong><c:out value="${empty totalItems ? fn:length(pendingRequests) : totalItems}"/></strong>
                        </div>
                    </div>
                </div>
                <p>Solicitudes enviadas por usuarios para revisar antes de cerrar su estado.</p>
            </div>
        </section>

        <section class="admin-requests-section" aria-label="Formularios pendientes">
            <div class="admin-section-heading">
                <div>
                    <span class="admin-kicker">Formularios</span>
                    <h2>Solicitudes de autos</h2>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty pendingRequests}">
                    <div class="admin-empty-state">
                        <p>No hay formularios pendientes para moderar.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="admin-requests-grid">
                        <c:forEach var="request" items="${pendingRequests}">
                            <pa:car-card
                                    model="${request.brandName} ${request.model}"
                                    year="${request.year}"
                                    bodyType="${request.bodyTypeName}"
                                    carId="${request.id}"
                                    hasImage="${request.hasImage}"
                                    imageUrl="${request.imageUrl}"
                                    href="#"
                                    submitter="${request.submitter}"
                                    footerText="Solicitud pendiente"
                                    actionText="Revisar"
                                    openModal="true"
                                    requestId="${request.id}"
                                    requestBrand="${request.brandName}"
                                    requestModel="${request.model}"
                                    requestYear="${request.year}"
                                    requestBodyType="${request.bodyTypeName}"
                                    requestDescription="${request.description}"
                                    requestSubmitter="${request.submitter}"
                                    requestImageUrls="${request.imageUrls}"
                                    requestFuelType="${request.fuelType}"
                                    requestHorsepower="${request.horsepower}"
                                    requestAirbagCount="${request.airbagCount}"
                                    requestTransmission="${request.transmission}"
                                    requestFuelConsumption="${request.fuelConsumption}"
                                    requestMaxSpeedKmh="${request.maxSpeedKmh}"
                                    requestPriceUsd="${request.priceUsd}"/>
                        </c:forEach>
                    </div>
                    <c:if test="${not empty totalPages and totalPages > 1}">
                        <c:url var="adminBaseUrl" value="/admin"/>
                        <pa:pagination currentPage="${currentPage}"
                                       totalPages="${totalPages}"
                                       baseUrl="${adminBaseUrl}"
                                       ariaLabel="Paginación de solicitudes"/>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>

    </main>

    <pa:admin-car-form brands="${brands}" bodyTypes="${bodyTypes}" mode="admin"/>
    <script src="<c:url value='/js/car-form.js?v=1'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
