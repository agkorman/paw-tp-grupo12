<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="activity.title" styles="/css/cars.css|/css/activity.css|/css/reactions.css|/css/image-lightbox.css"/>
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

            <spring:message var="activitySortAria" code="activity.filter.sort.label"/>
            <spring:message var="activityTypeAria" code="activity.filter.type.label"/>
            <spring:message var="activityTimeframeAria" code="activity.filter.timeframe.label"/>
            <form class="cars-toolbar activity-toolbar" method="get" action="<c:url value='/activity'/>" id="activity-filter-form"
                  novalidate="novalidate">
                <div class="cars-toolbar-shell">
                    <div class="cars-toolbar-field">
                        <span class="cars-toolbar-field-ui" aria-hidden="true">
                            <span class="cars-toolbar-icon"><pa:icon name="sort" size="22"/></span>
                            <span class="cars-toolbar-field-copy">
                                <span class="cars-toolbar-label"><spring:message code="activity.filter.sort.label"/></span>
                                <span class="cars-toolbar-value" data-toolbar-select-value="sort">
                                    <c:choose>
                                        <c:when test="${activityCriteria.sort eq 'controversial'}"><spring:message code="activity.filter.sort.controversial"/></c:when>
                                        <c:when test="${activityCriteria.sort eq 'latest'}"><spring:message code="activity.filter.sort.latest"/></c:when>
                                        <c:otherwise><spring:message code="activity.filter.sort.trending"/></c:otherwise>
                                    </c:choose>
                                </span>
                            </span>
                            <span class="cars-toolbar-chevron" aria-hidden="true"><pa:icon name="chevron-down" size="12"/></span>
                        </span>
                        <select class="cars-toolbar-select cars-toolbar-select-overlay" id="activity-filter-sort" name="sort" aria-label="${activitySortAria}">
                            <option value="trending" <c:if test="${activityCriteria.sort eq 'trending'}">selected</c:if>><spring:message code="activity.filter.sort.trending"/></option>
                            <option value="controversial" <c:if test="${activityCriteria.sort eq 'controversial'}">selected</c:if>><spring:message code="activity.filter.sort.controversial"/></option>
                            <option value="latest" <c:if test="${activityCriteria.sort eq 'latest'}">selected</c:if>><spring:message code="activity.filter.sort.latest"/></option>
                        </select>
                    </div>

                    <div class="cars-toolbar-field">
                        <span class="cars-toolbar-field-ui" aria-hidden="true">
                            <span class="cars-toolbar-icon"><pa:icon name="filter" size="22"/></span>
                            <span class="cars-toolbar-field-copy">
                                <span class="cars-toolbar-label"><spring:message code="activity.filter.type.label"/></span>
                                <span class="cars-toolbar-value" data-toolbar-select-value="type">
                                    <c:choose>
                                        <c:when test="${activityCriteria.type eq 'reviews'}"><spring:message code="activity.filter.type.reviews"/></c:when>
                                        <c:when test="${activityCriteria.type eq 'community'}"><spring:message code="activity.filter.type.community"/></c:when>
                                        <c:otherwise><spring:message code="activity.filter.type.all"/></c:otherwise>
                                    </c:choose>
                                </span>
                            </span>
                            <span class="cars-toolbar-chevron" aria-hidden="true"><pa:icon name="chevron-down" size="12"/></span>
                        </span>
                        <select class="cars-toolbar-select cars-toolbar-select-overlay" id="activity-filter-type" name="type" aria-label="${activityTypeAria}">
                            <option value="all" <c:if test="${activityCriteria.type eq 'all'}">selected</c:if>><spring:message code="activity.filter.type.all"/></option>
                            <option value="reviews" <c:if test="${activityCriteria.type eq 'reviews'}">selected</c:if>><spring:message code="activity.filter.type.reviews"/></option>
                            <option value="community" <c:if test="${activityCriteria.type eq 'community'}">selected</c:if>><spring:message code="activity.filter.type.community"/></option>
                        </select>
                    </div>

                    <div class="cars-toolbar-field">
                        <span class="cars-toolbar-field-ui" aria-hidden="true">
                            <span class="cars-toolbar-icon"><pa:icon name="clock" size="22"/></span>
                            <span class="cars-toolbar-field-copy">
                                <span class="cars-toolbar-label"><spring:message code="activity.filter.timeframe.label"/></span>
                                <span class="cars-toolbar-value" data-toolbar-select-value="timeframe">
                                    <c:choose>
                                        <c:when test="${activityCriteria.timeframe eq 'today'}"><spring:message code="activity.filter.timeframe.today"/></c:when>
                                        <c:when test="${activityCriteria.timeframe eq 'week'}"><spring:message code="activity.filter.timeframe.week"/></c:when>
                                        <c:when test="${activityCriteria.timeframe eq 'month'}"><spring:message code="activity.filter.timeframe.month"/></c:when>
                                        <c:otherwise><spring:message code="activity.filter.timeframe.all"/></c:otherwise>
                                    </c:choose>
                                </span>
                            </span>
                            <span class="cars-toolbar-chevron" aria-hidden="true"><pa:icon name="chevron-down" size="12"/></span>
                        </span>
                        <select class="cars-toolbar-select cars-toolbar-select-overlay" id="activity-filter-timeframe" name="timeframe" aria-label="${activityTimeframeAria}">
                            <option value="all" <c:if test="${activityCriteria.timeframe eq 'all'}">selected</c:if>><spring:message code="activity.filter.timeframe.all"/></option>
                            <option value="today" <c:if test="${activityCriteria.timeframe eq 'today'}">selected</c:if>><spring:message code="activity.filter.timeframe.today"/></option>
                            <option value="week" <c:if test="${activityCriteria.timeframe eq 'week'}">selected</c:if>><spring:message code="activity.filter.timeframe.week"/></option>
                            <option value="month" <c:if test="${activityCriteria.timeframe eq 'month'}">selected</c:if>><spring:message code="activity.filter.timeframe.month"/></option>
                        </select>
                    </div>

                    <button type="submit" class="btn-secondary cars-toolbar-apply">
                        <spring:message code="common.action.apply"/>
                    </button>
                </div>
            </form>

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
                        <jsp:useBean id="activityPaginationParams" class="java.util.LinkedHashMap"/>
                        <c:set target="${activityPaginationParams}" property="sort" value="${activityCriteria.sort}"/>
                        <c:set target="${activityPaginationParams}" property="type" value="${activityCriteria.type}"/>
                        <c:set target="${activityPaginationParams}" property="timeframe" value="${activityCriteria.timeframe}"/>
                        <pa:pagination currentPage="${activityCurrentPage}"
                                       totalPages="${activityTotalPages}"
                                       baseUrl="/activity"
                                       extraParams="${activityPaginationParams}"
                                       ariaLabel="${activityPaginationAria}"/>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
    <pa:image-lightbox/>
    <pa:script src="/js/shared/image-lightbox.js"/>
    <pa:script src="/js/cars/cars-toolbar.js"/>
    <pa:footer/>
</body>
</html>
