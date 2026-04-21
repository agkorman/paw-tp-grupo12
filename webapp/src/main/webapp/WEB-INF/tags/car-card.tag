<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="model"      required="true" %>
<%@ attribute name="bodyType"   required="false" %>
<%@ attribute name="carId"      required="true" %>
<%@ attribute name="hasImage"   required="true" %>
<%@ attribute name="href"       required="true" %>
<%@ attribute name="averageRating" required="false" %>
<%@ attribute name="reviewCount" required="false" %>
<%@ attribute name="imageUrl" required="false" %>
<%@ attribute name="submitter" required="false" %>
<%@ attribute name="footerText" required="false" %>
<%@ attribute name="actionText" required="false" %>
<%@ attribute name="showFavorite" required="false" %>
<%@ attribute name="favorited" required="false" %>
<%@ attribute name="openModal" required="false" %>
<%@ attribute name="requestId" required="false" %>
<%@ attribute name="requestBrand" required="false" %>
<%@ attribute name="requestModel" required="false" %>
<%@ attribute name="requestBodyType" required="false" %>
<%@ attribute name="requestDescription" required="false" %>
<%@ attribute name="requestSubmitter" required="false" %>
<%@ attribute name="adminBrand" required="false" %>
<%@ attribute name="adminModel" required="false" %>
<%@ attribute name="adminDescription" required="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="modalImageUrl" value=""/>
<c:if test="${not empty imageUrl}">
    <c:url var="modalImageUrl" value="${imageUrl}"/>
</c:if>
<c:url var="defaultCarImageUrl" value="/car-image">
    <c:param name="carId" value="${carId}"/>
</c:url>
<c:url var="adminCarEditUrl" value="/admin/cars/${carId}"/>
<c:url var="adminCarDeleteUrl" value="/admin/cars/${carId}/delete"/>

<div class="car-card-shell">
    <a href="${fn:escapeXml(href)}"
       class="car-card-link"
       aria-label="Ver ${fn:escapeXml(model)}"
       data-open-create-car-modal="${openModal ? 'true' : 'false'}"
       data-request-id="${fn:escapeXml(requestId)}"
       data-request-brand="${fn:escapeXml(requestBrand)}"
       data-request-model="${fn:escapeXml(requestModel)}"
       data-request-body-type="${fn:escapeXml(requestBodyType)}"
       data-request-description="${fn:escapeXml(requestDescription)}"
       data-request-submitter="${fn:escapeXml(requestSubmitter)}"
       data-request-image-url="${fn:escapeXml(modalImageUrl)}">
        <div class="car-card">
            <div class="card-image-wrap">
                <c:choose>
                    <c:when test="${hasImage}">
                        <c:choose>
                            <c:when test="${not empty imageUrl}">
                                <c:url var="resolvedImageUrl" value="${imageUrl}"/>
                            </c:when>
                            <c:otherwise>
                                <c:url var="resolvedImageUrl" value="/car-image">
                                    <c:param name="carId" value="${carId}"/>
                                </c:url>
                            </c:otherwise>
                        </c:choose>
                        <img src="${resolvedImageUrl}" alt="${fn:escapeXml(model)}" loading="lazy">
                    </c:when>
                    <c:otherwise>
                        <div class="img-placeholder">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                                <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                            </svg>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">
                <span class="card-category">
                    <c:choose>
                        <c:when test="${not empty bodyType}"><c:out value="${bodyType}"/></c:when>
                        <c:otherwise>Vehículo</c:otherwise>
                    </c:choose>
                </span>
                <div class="card-title-row">
                    <span class="card-title"><c:out value="${model}"/></span>
                </div>
                <div class="card-rating-row">
                    <c:choose>
                        <c:when test="${not empty submitter}">
                            <span class="card-rating-empty">Enviado por <c:out value="${submitter}"/></span>
                        </c:when>
                        <c:when test="${reviewCount gt 0}">
                            <span class="card-rating-badge">
                                <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                                    <path d="M12 2.75l2.91 5.9 6.51.95-4.71 4.59 1.11 6.48L12 17.62l-5.82 3.05 1.11-6.48-4.71-4.59 6.51-.95L12 2.75z"/>
                                </svg>
                                <span class="card-rating-value"><c:out value="${averageRating}"/></span>
                            </span>
                            <span class="card-rating-count">
                                <c:out value="${reviewCount}"/>
                                <c:choose>
                                    <c:when test="${reviewCount eq 1}">reseña</c:when>
                                    <c:otherwise>reseñas</c:otherwise>
                                </c:choose>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="card-rating-empty">Sin reseñas todavía</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="card-footer">
                    <span class="card-meta">
                        <c:choose>
                            <c:when test="${not empty footerText}"><c:out value="${footerText}"/></c:when>
                            <c:when test="${reviewCount gt 0}">Puntaje de la comunidad sobre 5</c:when>
                            <c:otherwise>Comparte la primera impresión</c:otherwise>
                        </c:choose>
                    </span>
                    <span class="card-specs-link">
                        <c:choose>
                            <c:when test="${not empty actionText}"><c:out value="${actionText}"/></c:when>
                            <c:otherwise>Ver reseñas</c:otherwise>
                        </c:choose>
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <path d="M5 12h14M12 5l7 7-7 7"/>
                        </svg>
                        </span>
                </div>
            </div>
        </div>
    </a>
    <c:set var="showAdminMenu" value="false"/>
    <sec:authorize access="hasRole('ADMIN')">
        <c:if test="${openModal ne true and not empty adminBrand and not empty adminModel}">
            <c:set var="showAdminMenu" value="true"/>
        </c:if>
    </sec:authorize>
    <c:if test="${showFavorite ne false or showAdminMenu}">
        <div class="car-card-controls">
            <c:if test="${showFavorite ne false}">
                <div class="car-card-favorite">
                    <pa:car-favorite-button carId="${carId}" favorited="${favorited}"/>
                </div>
            </c:if>
            <c:if test="${showAdminMenu}">
                <div class="car-card-admin-menu">
                    <pa:action-menu label="Abrir opciones de auto">
                        <button
                                type="button"
                                data-open-create-car-modal="edit-car"
                                data-car-action="${fn:escapeXml(adminCarEditUrl)}"
                                data-car-id="${fn:escapeXml(carId)}"
                                data-car-brand="${fn:escapeXml(adminBrand)}"
                                data-car-model="${fn:escapeXml(adminModel)}"
                                data-car-body-type="${fn:escapeXml(bodyType)}"
                                data-car-description="${fn:escapeXml(adminDescription)}"
                                data-car-image-url="${hasImage ? fn:escapeXml(defaultCarImageUrl) : ''}">
                            Editar
                        </button>
                        <button
                                type="button"
                                class="action-menu-danger"
                                data-open-delete-car-modal
                                data-car-delete-action="${fn:escapeXml(adminCarDeleteUrl)}"
                                data-car-title="${fn:escapeXml(adminBrand)} ${fn:escapeXml(adminModel)}">
                            Eliminar
                        </button>
                    </pa:action-menu>
                </div>
            </c:if>
        </div>
    </c:if>
</div>
