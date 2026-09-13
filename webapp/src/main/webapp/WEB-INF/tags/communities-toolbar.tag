<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="topics" required="true" type="java.util.List" %>
<%@ attribute name="selectedTopic" required="false" %>
<%@ attribute name="selectedMembership" required="false" %>
<%@ attribute name="searchQuery" required="false" %>
<%@ attribute name="sortBy" required="false" %>
<%@ attribute name="authenticated" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="toolbarSearchPlaceholder" code="communities.toolbar.search.placeholder"/>
<spring:message var="toolbarSearchLabel" code="communities.toolbar.search.label"/>
<spring:message var="topicAria" code="communities.toolbar.topic.aria"/>
<spring:message var="membershipAria" code="communities.toolbar.membership.aria"/>
<spring:message var="sortAria" code="communities.toolbar.sort.aria"/>

<form class="cars-toolbar communities-toolbar" method="get" action="<c:url value='/communities'/>" id="community-filter-form"
      enctype="multipart/form-data" novalidate="novalidate">
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

        <div class="cars-toolbar-field">
            <span class="cars-toolbar-field-ui" aria-hidden="true">
                <span class="cars-toolbar-icon">
                    <pa:icon name="users" size="22"/>
                </span>
                <span class="cars-toolbar-field-copy">
                    <span class="cars-toolbar-label"><spring:message code="communities.toolbar.membership"/></span>
                    <span class="cars-toolbar-value" data-toolbar-select-value="membership">
                        <c:choose>
                            <c:when test="${selectedMembership eq 'joined'}"><spring:message code="communities.toolbar.membership.joined"/></c:when>
                            <c:when test="${selectedMembership eq 'not_joined'}"><spring:message code="communities.toolbar.membership.notJoined"/></c:when>
                            <c:otherwise><spring:message code="communities.toolbar.membership.all"/></c:otherwise>
                        </c:choose>
                    </span>
                </span>
                <span class="cars-toolbar-chevron" aria-hidden="true">
                    <pa:icon name="chevron-down" size="12"/>
                </span>
            </span>
            <select class="cars-toolbar-select cars-toolbar-select-overlay" id="filter-membership" name="membership" aria-label="${membershipAria}">
                <option value="" <c:if test="${empty selectedMembership}">selected</c:if>><spring:message code="communities.toolbar.membership.all"/></option>
                <option value="joined"
                        <c:if test="${selectedMembership eq 'joined'}">selected</c:if>
                        <c:if test="${not authenticated}">disabled</c:if>>
                    <spring:message code="communities.toolbar.membership.joined"/>
                </option>
                <option value="not_joined" <c:if test="${selectedMembership eq 'not_joined'}">selected</c:if>><spring:message code="communities.toolbar.membership.notJoined"/></option>
            </select>
        </div>

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
