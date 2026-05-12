<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="panelId" required="true" %>
<%@ attribute name="tab" required="true" %>
<%@ attribute name="reviews" required="true" type="java.util.List" %>
<%@ attribute name="currentPage" required="true" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="true" type="java.lang.Integer" %>
<%@ attribute name="emptyCode" required="true" %>
<%@ attribute name="feedAria" required="true" %>
<%@ attribute name="previewAria" required="true" %>
<%@ attribute name="idPrefix" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<section id="${fn:escapeXml(panelId)}" class="activity-tab-panel">
    <c:choose>
        <c:when test="${empty reviews}">
            <div class="activity-empty-state">
                <p><spring:message code="${emptyCode}"/></p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="activity-panel-layout">
                <div class="activity-feed" aria-label="${fn:escapeXml(feedAria)}">
                    <c:forEach var="activityReview" items="${reviews}" varStatus="status">
                        <pa:activity-review-card reviewCard="${activityReview}"
                                                 idPrefix="${idPrefix}-${currentPage}-${status.index}"/>
                    </c:forEach>
                </div>
                <aside class="activity-preview-column" aria-label="${fn:escapeXml(previewAria)}">
                    <c:forEach var="activityReview" items="${reviews}" varStatus="status">
                        <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                          idPrefix="${idPrefix}-${currentPage}-${status.index}"/>
                    </c:forEach>
                </aside>
            </div>
            <c:if test="${totalPages > 1}">
                <jsp:useBean id="activityPaginationParams" class="java.util.LinkedHashMap"/>
                <c:set target="${activityPaginationParams}" property="tab" value="${tab}"/>
                <spring:message var="activityPaginationAria" code="activity.pagination.aria"/>
                <pa:pagination currentPage="${currentPage}"
                               totalPages="${totalPages}"
                               baseUrl="/activity"
                               extraParams="${activityPaginationParams}"
                               fragment="${fn:escapeXml(panelId)}"
                               ariaLabel="${activityPaginationAria}"/>
            </c:if>
        </c:otherwise>
    </c:choose>
</section>
