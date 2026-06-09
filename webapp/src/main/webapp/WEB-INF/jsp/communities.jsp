<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.title" styles="/css/cars.css|/css/communities-hub.css|/css/communities-responsive.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="createCommunityUrl" value="/communities/new"/>
    <spring:message var="communitiesGridAria" code="communities.hub.grid.aria"/>
    <c:set var="resultCount" value="${empty communitiesTotalItems ? fn:length(communityCards) : communitiesTotalItems}"/>
    <main class="communities-hub-page">
        <section class="communities-hub-hero">
            <div class="communities-hub-copy">
                <p class="communities-hub-kicker"><spring:message code="communities.hub.kicker"/></p>
                <h1 class="communities-hub-title"><spring:message code="communities.hub.title"/></h1>
                <p class="communities-hub-description"><spring:message code="communities.hub.description"/></p>
            </div>
            <a class="btn-primary communities-hub-cta" href="${fn:escapeXml(createCommunityUrl)}">
                <spring:message code="communities.hub.createAction"/>
            </a>
        </section>

        <pa:communities-toolbar
                topics="${communityTopics}"
                selectedTopic="${selectedTopic}"
                selectedMembership="${selectedMembership}"
                searchQuery="${searchQuery}"
                sortBy="${sortBy}"
                authenticated="${authenticated}"/>

        <section class="communities-card-grid" aria-label="${fn:escapeXml(communitiesGridAria)}">
            <c:forEach var="card" items="${communityCards}">
                <c:url var="cardHref" value="/communities/${card.slug}"/>
                <pa:community-card
                        href="${cardHref}"
                        title="${card.title}"
                        description="${card.description}"
                        memberCount="${card.memberCount}"
                        weeklyPostCount="${card.weeklyPostCount}"
                        joined="${card.joined}"/>
            </c:forEach>
        </section>

        <c:if test="${communitiesTotalPages > 1}">
            <jsp:useBean id="communitiesPaginationParams" class="java.util.LinkedHashMap"/>
            <c:if test="${not empty criteria.q}"><c:set target="${communitiesPaginationParams}" property="q" value="${criteria.q}"/></c:if>
            <c:if test="${not empty criteria.topic}"><c:set target="${communitiesPaginationParams}" property="topic" value="${criteria.topic}"/></c:if>
            <c:if test="${not empty criteria.membership}"><c:set target="${communitiesPaginationParams}" property="membership" value="${criteria.membership}"/></c:if>
            <c:if test="${not empty criteria.sortBy}"><c:set target="${communitiesPaginationParams}" property="sortBy" value="${criteria.sortBy}"/></c:if>
            <spring:message var="communitiesPaginationAria" code="communities.pagination.aria"/>
            <pa:pagination currentPage="${communitiesCurrentPage}"
                           totalPages="${communitiesTotalPages}"
                           baseUrl="/communities"
                           extraParams="${communitiesPaginationParams}"
                           ariaLabel="${communitiesPaginationAria}"/>
        </c:if>
    </main>
    <pa:script src="/js/cars/cars-toolbar.js"/>
    <pa:footer/>
</body>
</html>
