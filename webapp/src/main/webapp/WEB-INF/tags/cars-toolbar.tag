<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.List" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.List" %>
<%@ attribute name="selectedBrand" required="false" %>
<%@ attribute name="selectedBodyType" required="false" %>
<%@ attribute name="searchQuery" required="false" %>
<%@ attribute name="resultCount" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="carsContentUrl" value="/cars/content"/>

<form class="cars-toolbar" method="get" action="<c:url value='/cars'/>" id="car-filter-form"
      data-enhanced-filter="true"
      data-fragment-url="${carsContentUrl}"
      data-target="#carsCatalogContent"
      data-auto-submit="true">
    <div class="cars-toolbar-shell">
        <label class="cars-toolbar-search" for="cars-toolbar-search">
            <span class="cars-toolbar-icon" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1">
                    <circle cx="11" cy="11" r="7"></circle>
                    <path d="m20 20-3.5-3.5"></path>
                </svg>
            </span>
            <input
                    id="cars-toolbar-search"
                    type="search"
                    name="q"
                    value="<c:out value='${searchQuery}'/>"
                    placeholder="Busca marca, modelo o estilo..."
                    autocomplete="off"
                    aria-label="Buscar autos">
        </label>

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9">
                        <path d="M10.5 20H7a2 2 0 0 1-2-2v-3.2a2 2 0 0 1 .59-1.41l6.8-6.8a2 2 0 0 1 2.82 0l2.79 2.79a2 2 0 0 1 0 2.82l-6.8 6.8A2 2 0 0 1 10.5 20Z"></path>
                        <path d="m13.5 8.5 2 2"></path>
                    </svg>
                </span>
                <span class="cars-toolbar-field-copy">
                    <span class="cars-toolbar-label">Marca</span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="brand">
                        <c:choose>
                            <c:when test="${not empty selectedBrand}"><c:out value="${selectedBrand}"/></c:when>
                            <c:otherwise>Todas</c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <polyline points="6 9 12 15 18 9"></polyline>
                    </svg>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-brand" name="brand" aria-label="Filtrar por marca">
                <option value="" <c:if test="${empty selectedBrand}">selected</c:if>>Todas</option>
                <c:forEach items="${brands}" var="b">
                    <option value="<c:out value='${b.name}'/>" <c:if test="${selectedBrand eq b.name}">selected</c:if>>
                        <c:out value="${b.name}"/>
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9">
                        <path d="M6.2 11.4A2 2 0 0 1 8.08 10h7.84a2 2 0 0 1 1.88 1.4L19 15H5l1.2-3.6Z"></path>
                        <path d="M4 15h16v2.2a.8.8 0 0 1-.8.8h-.8a2.2 2.2 0 0 1-4.4 0H10a2.2 2.2 0 0 1-4.4 0h-.8a.8.8 0 0 1-.8-.8V15Z"></path>
                        <path d="M7.5 12.5h1.5"></path>
                        <path d="M15 12.5h1.5"></path>
                    </svg>
                </span>
                <span class="cars-toolbar-field-copy">
                    <span class="cars-toolbar-label">Carrocería</span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="bodyType">
                        <c:choose>
                            <c:when test="${not empty selectedBodyType}"><c:out value="${selectedBodyType}"/></c:when>
                            <c:otherwise>Todas</c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <polyline points="6 9 12 15 18 9"></polyline>
                    </svg>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-body" name="bodyType" aria-label="Filtrar por carrocería">
                <option value="" <c:if test="${empty selectedBodyType}">selected</c:if>>Todas</option>
                <c:forEach items="${bodyTypes}" var="bt">
                    <option value="<c:out value='${bt.name}'/>" <c:if test="${selectedBodyType eq bt.name}">selected</c:if>>
                        <c:out value="${bt.name}"/>
                    </option>
                </c:forEach>
            </select>
        </div>

        <div class="cars-toolbar-meta">
            <span class="cars-toolbar-count"><c:out value="${resultCount}"/> vehículos encontrados</span>
        </div>
    </div>
</form>
