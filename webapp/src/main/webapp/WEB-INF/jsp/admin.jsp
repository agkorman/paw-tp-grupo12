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
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=2'/>">
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

                    <%--
                    <button type="button" class="btn-primary admin-hero-action" data-open-create-car-modal="create">
                        Agregar auto
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true">
                            <path d="M5 12h14M12 5l7 7-7 7"/>
                        </svg>
                    </button>
                    --%>
                    <div class="admin-section-actions">
                        <div class="admin-status" aria-label="Solicitudes pendientes">
                            <span>Pendientes</span>
                            <strong><c:out value="${fn:length(pendingRequests)}"/></strong>
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
                                    bodyType="${request.bodyTypeName}"
                                    carId="${request.id}"
                                    hasImage="${request.hasImage}"
                                    imageUrl="${request.imageUrl}"
                                    href="#"
                                    showFavorite="false"
                                    submitter="${request.submitter}"
                                    footerText="Solicitud pendiente"
                                    actionText="Revisar"
                                    openModal="true"
                                    requestId="${request.id}"
                                    requestBrand="${request.brandName}"
                                    requestModel="${request.model}"
                                    requestBodyType="${request.bodyTypeName}"
                                    requestDescription="${request.description}"
                                    requestSubmitter="${request.submitter}"/>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <pa:create-car-modal brands="${brands}" bodyTypes="${bodyTypes}" mode="admin"/>
    <script src="<c:url value='/js/create-car-modal.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
