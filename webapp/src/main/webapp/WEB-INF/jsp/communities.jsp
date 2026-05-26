<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.title" styles="/css/communities-hub.css|/css/communities-responsive.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="createCommunityUrl" value="/communities/new"/>
    <spring:message var="communitiesGridAria" code="communities.hub.grid.aria"/>
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

        <section class="communities-card-grid" aria-label="${fn:escapeXml(communitiesGridAria)}">
            <c:forEach var="card" items="${communityCards}">
                <pa:community-card
                        href="${card.href}"
                        title="${card.title}"
                        description="${card.description}"
                        memberCount="${card.memberCount}"
                        weeklyPostCount="${card.weeklyPostCount}"
                        joined="${card.joined}"/>
            </c:forEach>
        </section>
    </main>
    <pa:footer/>
</body>
</html>
