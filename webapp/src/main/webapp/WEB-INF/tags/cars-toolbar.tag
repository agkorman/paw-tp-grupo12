<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="brands" required="true" type="java.util.List" %>
<%@ attribute name="bodyTypes" required="true" type="java.util.List" %>
<%@ attribute name="selectedBrand" required="false" %>
<%@ attribute name="selectedBodyType" required="false" %>
<%@ attribute name="searchQuery" required="false" %>
<%@ attribute name="sortBy" required="false" %>
<%@ attribute name="hasAdvancedFilters" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:url var="carsContentUrl" value="/cars/content"/>

<form class="cars-toolbar" method="get" action="<c:url value='/cars'/>" id="car-filter-form"
      data-enhanced-filter="true"
      data-fragment-url="${carsContentUrl}"
      data-target="#carsCatalogContent"
      data-auto-submit="true">
    <div class="cars-toolbar-shell">
        <label class="cars-toolbar-search" for="cars-toolbar-search">
            <span class="cars-toolbar-icon" aria-hidden="true">
                <pa:icon name="search" size="22"/>
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
                    <pa:icon name="tag" size="22"/>
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
                    <pa:icon name="chevron-down" size="12"/>
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
                    <pa:icon name="car" size="22"/>
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
                    <pa:icon name="chevron-down" size="12"/>
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
            <pa:icon name="options" size="22"/>
            Filtros
        </button>
        <span class="cars-toolbar-count" hidden aria-live="polite"></span>

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <pa:icon name="sort" size="22"/>
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
                            <c:when test="${sortBy eq 'price_asc'}">Menor precio</c:when>
                            <c:when test="${sortBy eq 'price_desc'}">Mayor precio</c:when>
                            <c:otherwise>Mejor puntuado</c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <pa:icon name="chevron-down" size="12"/>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-sort" name="sortBy" aria-label="Ordenar resultados">
                <option value="" <c:if test="${empty sortBy}">selected</c:if>>Mejor puntuado</option>
                <option value="name_asc" <c:if test="${sortBy eq 'name_asc'}">selected</c:if>>Nombre A-Z</option>
                <option value="hp_desc" <c:if test="${sortBy eq 'hp_desc'}">selected</c:if>>Mayor potencia</option>
                <option value="hp_asc" <c:if test="${sortBy eq 'hp_asc'}">selected</c:if>>Menor potencia</option>
                <option value="speed_desc" <c:if test="${sortBy eq 'speed_desc'}">selected</c:if>>Mayor velocidad</option>
                <option value="consumption_asc" <c:if test="${sortBy eq 'consumption_asc'}">selected</c:if>>Menor consumo</option>
                <option value="price_asc" <c:if test="${sortBy eq 'price_asc'}">selected</c:if>>Menor precio</option>
                <option value="price_desc" <c:if test="${sortBy eq 'price_desc'}">selected</c:if>>Mayor precio</option>
            </select>
        </div>
    </div>
</form>
