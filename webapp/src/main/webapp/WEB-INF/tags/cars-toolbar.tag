<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.List" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.List" %>
<%@ attribute name="selectedBrand" required="false" %>
<%@ attribute name="selectedBodyType" required="false" %>
<%@ attribute name="searchQuery" required="false" %>
<%@ attribute name="sortBy" required="false" %>
<%@ attribute name="hasAdvancedFilters" required="false" %>
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

        <button type="button"
                id="filtersToggleBtn"
                class="cars-toolbar-filters-btn${hasAdvancedFilters ? ' is-active' : ''}"
                data-open-filters-panel
                aria-expanded="false"
                aria-controls="carsFiltersPanel"
                aria-label="Filtros avanzados">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
                <line x1="4" y1="6" x2="20" y2="6"></line>
                <line x1="8" y1="12" x2="16" y2="12"></line>
                <line x1="10" y1="18" x2="14" y2="18"></line>
            </svg>
            Filtros
        </button>
        <span class="cars-toolbar-count" hidden aria-live="polite"></span>

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9">
                        <path d="M3 6h18M7 12h10M11 18h2"/>
                    </svg>
                </span>
                <span class="cars-toolbar-field-copy">
                    <span class="cars-toolbar-label">Ordenar por</span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="sortBy">
                        <c:choose>
                            <c:when test="${sortBy eq 'name_asc'}">Nombre A-Z</c:when>
                            <c:when test="${sortBy eq 'hp_desc'}">Mayor potencia</c:when>
                            <c:when test="${sortBy eq 'hp_asc'}">Menor potencia</c:when>
                            <c:when test="${sortBy eq 'speed_desc'}">Mayor velocidad</c:when>
                            <c:when test="${sortBy eq 'consumption_asc'}">Menor consumo</c:when>
                            <c:otherwise>Mejor puntuado</c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <polyline points="6 9 12 15 18 9"></polyline>
                    </svg>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-sort" name="sortBy" aria-label="Ordenar resultados">
                <option value="" <c:if test="${empty sortBy}">selected</c:if>>Mejor puntuado</option>
                <option value="name_asc" <c:if test="${sortBy eq 'name_asc'}">selected</c:if>>Nombre A-Z</option>
                <option value="hp_desc" <c:if test="${sortBy eq 'hp_desc'}">selected</c:if>>Mayor potencia</option>
                <option value="hp_asc" <c:if test="${sortBy eq 'hp_asc'}">selected</c:if>>Menor potencia</option>
                <option value="speed_desc" <c:if test="${sortBy eq 'speed_desc'}">selected</c:if>>Mayor velocidad</option>
                <option value="consumption_asc" <c:if test="${sortBy eq 'consumption_asc'}">selected</c:if>>Menor consumo</option>
            </select>
        </div>
    </div>
</form>
