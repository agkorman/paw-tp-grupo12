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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="toolbarSearchPlaceholder" code="cars.toolbar.search.placeholder"/>
<spring:message var="toolbarSearchLabel" code="cars.toolbar.search.label"/>
<spring:message var="brandAria" code="cars.toolbar.brand.aria"/>
<spring:message var="bodyTypeAria" code="cars.toolbar.bodyType.aria"/>
<spring:message var="filtersAria" code="cars.toolbar.filters.aria"/>
<spring:message var="sortAria" code="cars.toolbar.sort.aria"/>

<form class="cars-toolbar" method="get" action="<c:url value='/cars'/>" id="car-filter-form"
      novalidate="novalidate">
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
                    placeholder="${toolbarSearchPlaceholder}"
                    autocomplete="off"
                    aria-label="${toolbarSearchLabel}">
        </label>

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <pa:icon name="tag" size="22"/>
                </span>
                <span class="cars-toolbar-field-copy">
                    <span class="cars-toolbar-label"><spring:message code="cars.form.brand"/></span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="brand">
                        <c:choose>
                            <c:when test="${not empty selectedBrand}"><c:out value="${selectedBrand}"/></c:when>
                            <c:otherwise><spring:message code="cars.toolbar.brand.all"/></c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <pa:icon name="chevron-down" size="12"/>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-brand" name="brand" aria-label="${brandAria}">
                <option value="" <c:if test="${empty selectedBrand}">selected</c:if>><spring:message code="cars.toolbar.brand.all"/></option>
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
                    <span class="cars-toolbar-label"><spring:message code="cars.form.bodyType"/></span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="bodyType">
                        <c:choose>
                            <c:when test="${not empty selectedBodyType}"><c:out value="${selectedBodyType}"/></c:when>
                            <c:otherwise><spring:message code="cars.toolbar.bodyType.all"/></c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <pa:icon name="chevron-down" size="12"/>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-body" name="bodyType" aria-label="${bodyTypeAria}">
                <option value="" <c:if test="${empty selectedBodyType}">selected</c:if>><spring:message code="cars.toolbar.bodyType.all"/></option>
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
                aria-label="${filtersAria}">
            <pa:icon name="options" size="22"/>
            <spring:message code="cars.toolbar.filters"/>
        </button>
        <span class="cars-toolbar-count" hidden aria-live="polite"></span>

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <pa:icon name="sort" size="22"/>
                </span>
                <span class="cars-toolbar-field-copy">
                    <span class="cars-toolbar-label"><spring:message code="cars.toolbar.sort"/></span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="sortBy">
                        <c:choose>
                            <c:when test="${sortBy eq 'name_asc'}"><spring:message code="cars.toolbar.sort.nameAsc"/></c:when>
                            <c:when test="${sortBy eq 'hp_desc'}"><spring:message code="cars.toolbar.sort.hpDesc"/></c:when>
                            <c:when test="${sortBy eq 'hp_asc'}"><spring:message code="cars.toolbar.sort.hpAsc"/></c:when>
                            <c:when test="${sortBy eq 'speed_desc'}"><spring:message code="cars.toolbar.sort.speedDesc"/></c:when>
                            <c:when test="${sortBy eq 'consumption_asc'}"><spring:message code="cars.toolbar.sort.consumptionAsc"/></c:when>
                            <c:when test="${sortBy eq 'price_asc'}"><spring:message code="cars.toolbar.sort.priceAsc"/></c:when>
                            <c:when test="${sortBy eq 'price_desc'}"><spring:message code="cars.toolbar.sort.priceDesc"/></c:when>
                            <c:otherwise><spring:message code="cars.toolbar.sort.rating"/></c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <pa:icon name="chevron-down" size="12"/>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-sort" name="sortBy" aria-label="${sortAria}">
                <option value="" <c:if test="${empty sortBy}">selected</c:if>><spring:message code="cars.toolbar.sort.rating"/></option>
                <option value="name_asc" <c:if test="${sortBy eq 'name_asc'}">selected</c:if>><spring:message code="cars.toolbar.sort.nameAsc"/></option>
                <option value="hp_desc" <c:if test="${sortBy eq 'hp_desc'}">selected</c:if>><spring:message code="cars.toolbar.sort.hpDesc"/></option>
                <option value="hp_asc" <c:if test="${sortBy eq 'hp_asc'}">selected</c:if>><spring:message code="cars.toolbar.sort.hpAsc"/></option>
                <option value="speed_desc" <c:if test="${sortBy eq 'speed_desc'}">selected</c:if>><spring:message code="cars.toolbar.sort.speedDesc"/></option>
                <option value="consumption_asc" <c:if test="${sortBy eq 'consumption_asc'}">selected</c:if>><spring:message code="cars.toolbar.sort.consumptionAsc"/></option>
                <option value="price_asc" <c:if test="${sortBy eq 'price_asc'}">selected</c:if>><spring:message code="cars.toolbar.sort.priceAsc"/></option>
                <option value="price_desc" <c:if test="${sortBy eq 'price_desc'}">selected</c:if>><spring:message code="cars.toolbar.sort.priceDesc"/></option>
            </select>
        </div>
        <button type="submit" class="btn-secondary cars-toolbar-apply">
            <spring:message code="common.action.apply"/>
        </button>
    </div>
</form>
