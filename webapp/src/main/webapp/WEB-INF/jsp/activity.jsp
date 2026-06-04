<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="activity.title" styles="/css/activity.css|/css/reactions.css|/css/image-lightbox.css"/>
<body>
    <pa:nav activePage="activity"/>
    <spring:message var="activityFeedAria" code="activity.feed.aria"/>
    <spring:message var="activityPaginationAria" code="activity.pagination.aria"/>
    <main class="activity-page">
        <section class="activity-feed-panel" aria-labelledby="activityFeedTitle">
            <div class="activity-feed-header">
                <div class="activity-feed-heading">
                    <p class="activity-section-kicker"><spring:message code="activity.feed.kicker"/></p>
                    <h1 id="activityFeedTitle" class="activity-section-title"><spring:message code="activity.feed.title"/></h1>
                </div>
            </div>

            <c:choose>
                <c:when test="${empty activityCards}">
                    <div class="activity-empty-state">
                        <p><spring:message code="activity.empty.latest"/></p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="activity-feed" aria-label="${fn:escapeXml(activityFeedAria)}">
                        <c:forEach var="activityCard" items="${activityCards}">
                            <pa:activity-card activityCard="${activityCard}"/>
                        </c:forEach>
                    </div>
                    <c:if test="${activityTotalPages > 1}">
                        <pa:pagination currentPage="${activityCurrentPage}"
                                       totalPages="${activityTotalPages}"
                                       baseUrl="/activity"
                                       ariaLabel="${activityPaginationAria}"/>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
    <pa:image-lightbox/>
    <pa:script src="/js/shared/image-lightbox.js"/>
    <pa:footer/>
</body>
</html>
