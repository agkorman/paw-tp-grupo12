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

<c:url var="activityBaseUrl" value="/activity"/>

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
                    <c:if test="${currentPage < totalPages}">
                        <c:url var="activityShowMoreUrl" value="/activity">
                            <c:param name="tab" value="${tab}"/>
                            <c:param name="page" value="${currentPage + 1}"/>
                        </c:url>
                        <div class="reviews-feed-more profile-show-more">
                            <a class="btn-secondary reviews-show-more"
                               href="${activityShowMoreUrl}"
                               data-review-show-more="true"
                               data-fragment-url="${activityBaseUrl}"
                               data-target="#${fn:escapeXml(panelId)}"
                               data-list-selector=".activity-feed"
                               data-item-selector=".activity-feed > .activity-review-card"
                               data-preview-list-selector=".activity-preview-column"
                               data-preview-item-selector=".activity-preview-column > .activity-review-preview-panel">
                                <spring:message code="common.action.showMoreReviews"/>
                            </a>
                        </div>
                    </c:if>
                </div>
                <aside class="activity-preview-column" aria-label="${fn:escapeXml(previewAria)}">
                    <c:forEach var="activityReview" items="${reviews}" varStatus="status">
                        <pa:activity-review-preview-panel reviewCard="${activityReview}"
                                                          idPrefix="${idPrefix}-${currentPage}-${status.index}"/>
                    </c:forEach>
                </aside>
            </div>
        </c:otherwise>
    </c:choose>
</section>
