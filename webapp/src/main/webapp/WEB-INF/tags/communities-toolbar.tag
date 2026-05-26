<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="topics" required="true" type="java.util.List" %>
<%@ attribute name="selectedTopic" required="false" %>
<%@ attribute name="searchQuery" required="false" %>
<%@ attribute name="sortBy" required="false" %>
<%@ attribute name="hasAdvancedFilters" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="toolbarSearchPlaceholder" code="communities.toolbar.search.placeholder"/>
<spring:message var="toolbarSearchLabel" code="communities.toolbar.search.label"/>
<spring:message var="topicAria" code="communities.toolbar.topic.aria"/>
<spring:message var="filtersAria" code="communities.toolbar.filters.aria"/>
<spring:message var="sortAria" code="communities.toolbar.sort.aria"/>

<form class="cars-toolbar" method="get" action="<c:url value='/communities'/>" id="community-filter-form"
      novalidate="novalidate">
    <div class="cars-toolbar-shell">
        <label class="cars-toolbar-search" for="communities-toolbar-search">
            <span class="cars-toolbar-icon" aria-hidden="true">
                <pa:icon name="search" size="22"/>
            </span>
            <input
                    id="communities-toolbar-search"
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
                    <span class="cars-toolbar-label"><spring:message code="communities.toolbar.topic"/></span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="topic">
                        <c:choose>
                            <c:when test="${not empty selectedTopic}">
                                <spring:message code="communities.topic.${selectedTopic}"/>
                            </c:when>
                            <c:otherwise><spring:message code="communities.toolbar.topic.all"/></c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <pa:icon name="chevron-down" size="12"/>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-topic" name="topic" aria-label="${topicAria}">
                <option value="" <c:if test="${empty selectedTopic}">selected</c:if>><spring:message code="communities.toolbar.topic.all"/></option>
                <c:forEach items="${topics}" var="topic">
                    <option value="<c:out value='${topic.code}'/>" <c:if test="${selectedTopic eq topic.code}">selected</c:if>>
                        <spring:message code="${topic.labelCode}"/>
                    </option>
                </c:forEach>
            </select>
        </div>

        <button type="button"
                id="communityFiltersToggleBtn"
                class="cars-toolbar-filters-btn${hasAdvancedFilters ? ' is-active' : ''}"
                data-open-community-filters-panel
                aria-expanded="false"
                aria-controls="communityFiltersPanel"
                aria-label="${filtersAria}">
            <pa:icon name="options" size="22"/>
            <spring:message code="communities.toolbar.filters"/>
        </button>

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <pa:icon name="sort" size="22"/>
                </span>
                <span class="cars-toolbar-field-copy">
                    <span class="cars-toolbar-label"><spring:message code="communities.toolbar.sort"/></span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="sortBy">
                        <c:choose>
                            <c:when test="${sortBy eq 'members'}"><spring:message code="communities.toolbar.sort.members"/></c:when>
                            <c:when test="${sortBy eq 'name_asc'}"><spring:message code="communities.toolbar.sort.nameAsc"/></c:when>
                            <c:when test="${sortBy eq 'newest'}"><spring:message code="communities.toolbar.sort.newest"/></c:when>
                            <c:otherwise><spring:message code="communities.toolbar.sort.active"/></c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <pa:icon name="chevron-down" size="12"/>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-sort" name="sortBy" aria-label="${sortAria}">
                <option value="" <c:if test="${empty sortBy}">selected</c:if>><spring:message code="communities.toolbar.sort.active"/></option>
                <option value="members" <c:if test="${sortBy eq 'members'}">selected</c:if>><spring:message code="communities.toolbar.sort.members"/></option>
                <option value="name_asc" <c:if test="${sortBy eq 'name_asc'}">selected</c:if>><spring:message code="communities.toolbar.sort.nameAsc"/></option>
                <option value="newest" <c:if test="${sortBy eq 'newest'}">selected</c:if>><spring:message code="communities.toolbar.sort.newest"/></option>
            </select>
        </div>
        <button type="submit" class="btn-secondary cars-toolbar-apply">
            <spring:message code="common.action.apply"/>
        </button>
    </div>
</form>
