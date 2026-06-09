<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="href" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="description" required="true" %>
<%@ attribute name="memberCount" required="true" type="java.lang.Long" %>
<%@ attribute name="weeklyPostCount" required="true" type="java.lang.Long" %>
<%@ attribute name="joined" required="true" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="communityMemberCountText"
                code="${memberCount eq 1 ? 'communities.card.members.one' : 'communities.card.members.many'}"
                arguments="${memberCount}"/>
<spring:message var="communityWeeklyPostCountText"
                code="${weeklyPostCount eq 1 ? 'communities.card.weeklyPosts.one' : 'communities.card.weeklyPosts.many'}"
                arguments="${weeklyPostCount}"/>
<spring:message var="communityCardMetaText" code="communities.card.meta" arguments="${communityMemberCountText},${communityWeeklyPostCountText}"/>

<a class="community-card" href="${fn:escapeXml(href)}">
    <div class="community-card-body">
        <c:if test="${joined}">
            <p class="community-card-badge">
                <pa:icon name="star-filled" size="12"/>
                <span><spring:message code="communities.card.badge.joined"/></span>
            </p>
        </c:if>
        <h2 class="community-card-title"><c:out value="${title}"/></h2>
        <p class="community-card-description"><c:out value="${description}"/></p>
        <div class="community-card-footer">
            <p class="community-card-meta"><c:out value="${communityCardMetaText}"/></p>
        </div>
    </div>
</a>
