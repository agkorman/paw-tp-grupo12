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
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=5'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/admin.css?v=10'/>">
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
                            <strong><c:out value="${totalPendingItems}"/></strong>
                        </div>
                    </div>
                </div>
                <p>Solicitudes enviadas por usuarios para revisar antes de cerrar su estado.</p>
            </div>
        </section>

        <section class="admin-requests-section" aria-label="Solicitudes pendientes">
            <c:url var="carsTabUrl" value="/admin">
                <c:param name="tab" value="cars"/>
            </c:url>
            <c:url var="brandsTabUrl" value="/admin">
                <c:param name="tab" value="brands"/>
            </c:url>
            <c:url var="bodyTypesTabUrl" value="/admin">
                <c:param name="tab" value="body-types"/>
            </c:url>
            <c:url var="moderatorsTabUrl" value="/admin">
                <c:param name="tab" value="moderators"/>
            </c:url>
            <pa:subtabs tabCount="4"
                        labels="Autos|Marcas|Carrocerías|Moderador"
                        hrefs="${carsTabUrl}|${brandsTabUrl}|${bodyTypesTabUrl}|${moderatorsTabUrl}"
                        counts="${carRequestCount}|${brandRequestCount}|${bodyTypeRequestCount}|${adminRequestCount}"
                        values="cars|brands|body-types|moderators"
                        activeValue="${activeTab}"
                        ariaLabel="Tipos de solicitudes"/>

            <c:url var="adminBaseUrl" value="/admin"/>
            <jsp:useBean id="adminPaginationParams" class="java.util.LinkedHashMap" scope="page"/>
            <c:set target="${adminPaginationParams}" property="tab" value="${activeTab}"/>

            <c:choose>
                <c:when test="${activeTab eq 'brands'}">
                    <div class="admin-section-heading">
                        <div>
                            <h2>Solicitudes de marcas</h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty pendingBrandRequests}">
                            <div class="admin-empty-state">
                                <p>No hay solicitudes de marcas pendientes.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-catalog-request-grid">
                                <c:forEach var="brandRequest" items="${pendingBrandRequests}">
                                    <pa:admin-catalog-request-card
                                            id="${brandRequest.id}"
                                            name="${brandRequest.name}"
                                            submitter="${brandRequest.submitter}"
                                            comments="${brandRequest.comments}"
                                            type="brand"
                                            kicker="Marca"/>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:when test="${activeTab eq 'body-types'}">
                    <div class="admin-section-heading">
                        <div>
                            <h2>Solicitudes de carrocerías</h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty pendingBodyTypeRequests}">
                            <div class="admin-empty-state">
                                <p>No hay solicitudes de carrocerías pendientes.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-catalog-request-grid">
                                <c:forEach var="bodyTypeRequest" items="${pendingBodyTypeRequests}">
                                    <pa:admin-catalog-request-card
                                            id="${bodyTypeRequest.id}"
                                            name="${bodyTypeRequest.name}"
                                            submitter="${bodyTypeRequest.submitter}"
                                            comments="${bodyTypeRequest.comments}"
                                            type="body-type"
                                            kicker="Carrocería"/>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:when test="${activeTab eq 'moderators'}">
                    <div class="admin-section-heading">
                        <div>
                            <h2>Solicitudes de moderador</h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty pendingAdminRequests}">
                            <div class="admin-empty-state">
                                <p>No hay solicitudes de moderador pendientes.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-catalog-request-grid">
                                <c:forEach var="adminRequest" items="${pendingAdminRequests}">
                                    <button type="button"
                                            class="admin-catalog-request-card"
                                            data-open-admin-request-review
                                            data-request-id="${adminRequest.id}"
                                            data-request-submitter="${fn:escapeXml(adminRequest.label)}"
                                            data-request-motivation="${fn:escapeXml(adminRequest.motivation)}"
                                            data-request-bio="${fn:escapeXml(adminRequest.bio)}"
                                            data-request-justification="${fn:escapeXml(adminRequest.justification)}">
                                        <span class="admin-catalog-request-card-kicker">Moderador</span>
                                        <span class="admin-catalog-request-card-name">
                                            <c:out value="${adminRequest.username}"/>
                                        </span>
                                    </button>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <div class="admin-section-heading">
                        <div>
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
                                    <c:url var="requestReviewUrl" value="/admin/requests/${request.id}/review"/>
                                    <pa:car-card
                                            model="${request.brandName} ${request.model}"
                                            year="${request.year}"
                                            bodyType="${request.bodyTypeName}"
                                            carId="${request.id}"
                                            hasImage="${request.hasImage}"
                                            imageUrl="${request.imageUrl}"
                                            href="${requestReviewUrl}"
                                            submitter="${request.submitter}"
                                            footerText="Solicitud pendiente"
                                            actionText="Revisar"/>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>

            <c:if test="${not empty totalPages and totalPages > 1}">
                <pa:pagination currentPage="${currentPage}"
                               totalPages="${totalPages}"
                               baseUrl="${adminBaseUrl}"
                               extraParams="${adminPaginationParams}"
                               ariaLabel="Paginación de solicitudes"/>
            </c:if>
        </section>

    </main>

    <pa:admin-catalog-request-modal/>
    <pa:admin-request-review-modal/>
    <script src="<c:url value='/js/admin-catalog-modal.js'/>"></script>
    <script src="<c:url value='/js/admin-request-modal.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
</body>
</html>
