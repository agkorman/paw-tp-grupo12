<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="criteria" required="true" type="ar.edu.itba.paw.model.CommunitySearchCriteria" %>
<%@ attribute name="authenticated" required="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="closeFiltersLabel" code="communities.filter.close"/>
<div id="communityFiltersOverlay" class="cars-filters-overlay" data-close-community-filters-panel></div>

<aside id="communityFiltersPanel"
       class="cars-filters-panel"
       hidden
       role="dialog"
       aria-modal="true"
       aria-labelledby="communityFiltersPanelTitle">

    <div class="cars-filters-panel-inner">
        <div class="cars-filters-header">
            <h2 id="communityFiltersPanelTitle" class="cars-filters-title"><spring:message code="communities.filter.title"/></h2>
            <button type="button" class="cars-filters-close" data-close-community-filters-panel aria-label="${closeFiltersLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </div>

        <section class="filters-panel-section">
            <h3 class="filters-panel-section-title"><spring:message code="communities.filter.membership"/></h3>
            <div class="filter-toggle-group" data-community-filter-target="panelJoinedOnly">
                <button type="button" class="filter-toggle-option${not criteria.joinedOnly ? ' is-selected' : ''}" data-value="">
                    <spring:message code="communities.filter.membership.all"/>
                </button>
                <button type="button"
                        class="filter-toggle-option${criteria.joinedOnly ? ' is-selected' : ''}"
                        data-value="true"
                        <c:if test="${not authenticated}">disabled</c:if>>
                    <spring:message code="communities.filter.membership.joined"/>
                </button>
            </div>
            <c:set var="joinedOnlyValue" value="${criteria.joinedOnly ? 'true' : ''}"/>
            <input type="hidden" id="panelJoinedOnly" name="joinedOnly" value="<c:out value='${joinedOnlyValue}'/>">
            <c:if test="${not authenticated}">
                <p class="filters-field-help"><spring:message code="communities.filter.membership.login"/></p>
            </c:if>
        </section>

        <div class="cars-filters-footer">
            <button type="button" id="communityFiltersClearBtn" class="filters-clear-btn"><spring:message code="cars.filter.clear"/></button>
            <button type="button" id="communityFiltersApplyBtn" class="btn-primary filters-apply-btn">
                <spring:message code="cars.filter.apply"/>
            </button>
        </div>
    </div>
</aside>
