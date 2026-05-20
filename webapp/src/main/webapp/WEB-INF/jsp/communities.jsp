<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.title" styles="/css/communities.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="createCommunityUrl" value="/communities/new"/>
    <spring:message var="communitiesFiltersAria" code="communities.hub.filters.aria"/>
    <spring:message var="communitiesGridAria" code="communities.hub.grid.aria"/>
    <main class="communities-hub-page">
        <section class="communities-hub-hero">
            <div class="communities-hub-copy">
                <p class="communities-hub-kicker"><spring:message code="communities.hub.kicker"/></p>
                <h1 class="communities-hub-title"><spring:message code="communities.hub.title"/></h1>
                <p class="communities-hub-description"><spring:message code="communities.hub.description"/></p>
            </div>
            <a class="btn-primary communities-hub-cta" href="${createCommunityUrl}">
                <spring:message code="communities.hub.createAction"/>
            </a>
        </section>

        <section class="communities-filter-row" aria-label="${communitiesFiltersAria}">
            <span class="communities-filter-pill is-active"><spring:message code="communities.hub.filter.all"/></span>
            <span class="communities-filter-pill"><spring:message code="communities.hub.filter.brands"/></span>
            <span class="communities-filter-pill"><spring:message code="communities.hub.filter.eras"/></span>
            <span class="communities-filter-pill"><spring:message code="communities.hub.filter.bodyTypes"/></span>
            <span class="communities-filter-pill"><spring:message code="communities.hub.filter.lifestyle"/></span>
            <span class="communities-filter-pill"><spring:message code="communities.hub.filter.local"/></span>
        </section>

        <section class="communities-card-grid" aria-label="${communitiesGridAria}">
            <pa:community-card
                    href="/communities/classics"
                    accentClass="community-card-accent--bronze"
                    badgeCode="communities.card.badge.joined"
                    titleCode="communities.card.classics.title"
                    descriptionCode="communities.card.classics.description"
                    metaCode="communities.card.classics.meta"
                    actionCode="communities.card.action.joined"
                    joined="true"/>
            <pa:community-card
                    href="/communities/classics"
                    accentClass="community-card-accent--burgundy"
                    titleCode="communities.card.jdm.title"
                    descriptionCode="communities.card.jdm.description"
                    metaCode="communities.card.jdm.meta"
                    actionCode="communities.card.action.join"
                    joined="false"/>
            <pa:community-card
                    href="/communities/classics"
                    accentClass="community-card-accent--teal"
                    badgeCode="communities.card.badge.joined"
                    titleCode="communities.card.ev.title"
                    descriptionCode="communities.card.ev.description"
                    metaCode="communities.card.ev.meta"
                    actionCode="communities.card.action.joined"
                    joined="true"/>
            <pa:community-card
                    href="/communities/classics"
                    accentClass="community-card-accent--slate"
                    titleCode="communities.card.daily.title"
                    descriptionCode="communities.card.daily.description"
                    metaCode="communities.card.daily.meta"
                    actionCode="communities.card.action.join"
                    joined="false"/>
            <pa:community-card
                    href="/communities/classics"
                    accentClass="community-card-accent--amber"
                    titleCode="communities.card.offroad.title"
                    descriptionCode="communities.card.offroad.description"
                    metaCode="communities.card.offroad.meta"
                    actionCode="communities.card.action.join"
                    joined="false"/>
            <pa:community-card
                    href="/communities/classics"
                    accentClass="community-card-accent--violet"
                    titleCode="communities.card.rally.title"
                    descriptionCode="communities.card.rally.description"
                    metaCode="communities.card.rally.meta"
                    actionCode="communities.card.action.join"
                    joined="false"/>
        </section>
    </main>
    <pa:footer/>
</body>
</html>
