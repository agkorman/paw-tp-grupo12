<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="admin.title" styles="/css/reviews.css|/css/cars.css|/css/admin.css"/>
<body>
    <pa:nav activePage="admin"/>
    <spring:message var="brandKicker" code="cars.form.brand"/>
    <spring:message var="bodyTypeKicker" code="cars.form.bodyType"/>
    <spring:message var="pendingStatusLabel" code="common.status.pending"/>
    <spring:message var="reviewActionLabel" code="common.action.review"/>

    <main class="admin-page">
        <section class="admin-hero">
            <div class="admin-hero-copy">
                <div class="admin-hero-heading">
                    <h1><spring:message code="admin.heading"/></h1>

                    <div class="admin-section-actions">
                        <spring:message var="pendingLabel" code="admin.pending"/>
                        <div class="admin-status" aria-label="${pendingLabel}">
                            <span><c:out value="${pendingLabel}"/></span>
                            <strong><c:out value="${totalPendingItems}"/></strong>
                        </div>
                    </div>
                </div>
                <p><spring:message code="admin.pending.description"/></p>
            </div>
        </section>

        <spring:message var="requestTypesLabel" code="admin.requestTypes.aria"/>
        <section class="admin-requests-section" aria-label="${pendingLabel}">
            <c:url var="carsTabUrl" value="/admin">
                <c:param name="tab" value="cars"/>
            </c:url>
            <c:url var="brandsTabUrl" value="/admin">
                <c:param name="tab" value="brands"/>
            </c:url>
            <c:url var="bodyTypesTabUrl" value="/admin">
                <c:param name="tab" value="body-types"/>
            </c:url>
            <c:url var="moderatorsTabUrl" value="/admin">
                <c:param name="tab" value="moderators"/>
            </c:url>
            <spring:message var="adminCarsTabLabel" code="admin.tab.cars"/>
            <spring:message var="adminBrandsTabLabel" code="admin.tab.brands"/>
            <spring:message var="adminBodyTypesTabLabel" code="admin.tab.bodyTypes"/>
            <spring:message var="adminModeratorsTabLabel" code="admin.tab.moderators"/>
            <pa:subtabs tabCount="4"
                        labels="${adminCarsTabLabel}|${adminBrandsTabLabel}|${adminBodyTypesTabLabel}|${adminModeratorsTabLabel}"
                        hrefs="${carsTabUrl}|${brandsTabUrl}|${bodyTypesTabUrl}|${moderatorsTabUrl}"
                        counts="${carRequestCount}|${brandRequestCount}|${bodyTypeRequestCount}|${adminRequestCount}"
                        values="cars|brands|body-types|moderators"
                        activeValue="${activeTab}"
                        ariaLabel="${requestTypesLabel}"/>

            <c:set var="adminBaseUrl" value="/admin"/>
            <jsp:useBean id="adminPaginationParams" class="java.util.LinkedHashMap" scope="page"/>
            <c:set target="${adminPaginationParams}" property="tab" value="${activeTab}"/>
            <spring:message var="brandKicker"    code="admin.brandRequests.kicker"/>
            <spring:message var="bodyTypeKicker" code="admin.bodyTypeRequests.kicker"/>

            <c:choose>
                <c:when test="${activeTab eq 'brands'}">
                    <div class="admin-section-heading">
                        <div>
                            <h2><spring:message code="admin.brandRequests.title"/></h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty pendingBrandRequests}">
                            <div class="admin-empty-state">
                                <p><spring:message code="admin.brandRequests.empty"/></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-catalog-request-grid">
                                <c:forEach var="brandRequest" items="${pendingBrandRequests}">
                                    <pa:admin-catalog-request-card
                                            id="${brandRequest.id}"
                                            name="${brandRequest.name}"
                                            submitter="${brandRequest.submitter}"
                                            comments="${brandRequest.comments}"
                                            type="brand"
                                            kicker="${brandKicker}"/>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:when test="${activeTab eq 'body-types'}">
                    <div class="admin-section-heading">
                        <div>
                            <h2><spring:message code="admin.bodyTypeRequests.title"/></h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty pendingBodyTypeRequests}">
                            <div class="admin-empty-state">
                                <p><spring:message code="admin.bodyTypeRequests.empty"/></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-catalog-request-grid">
                                <c:forEach var="bodyTypeRequest" items="${pendingBodyTypeRequests}">
                                    <pa:admin-catalog-request-card
                                            id="${bodyTypeRequest.id}"
                                            name="${bodyTypeRequest.name}"
                                            submitter="${bodyTypeRequest.submitter}"
                                            comments="${bodyTypeRequest.comments}"
                                            type="body-type"
                                            kicker="${bodyTypeKicker}"/>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:when test="${activeTab eq 'moderators'}">
                    <div class="admin-section-heading">
                        <div>
                            <h2><spring:message code="admin.moderatorRequests.title"/></h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty pendingAdminRequests}">
                            <div class="admin-empty-state">
                                <p><spring:message code="admin.moderatorRequests.empty"/></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-catalog-request-grid">
                                <c:forEach var="adminRequest" items="${pendingAdminRequests}">
                                    <button type="button"
                                            class="admin-catalog-request-card"
                                            data-open-admin-request-review
                                            data-request-id="${adminRequest.id}"
                                            data-request-submitter="${fn:escapeXml(adminRequest.label)}"
                                            data-request-motivation="${fn:escapeXml(adminRequest.motivation)}"
                                            data-request-bio="${fn:escapeXml(adminRequest.bio)}"
                                            data-request-justification="${fn:escapeXml(adminRequest.justification)}">
                                        <span class="admin-catalog-request-card-kicker"><spring:message code="admin.tab.moderators"/></span>
                                        <span class="admin-catalog-request-card-name">
                                            <c:out value="${adminRequest.username}"/>
                                        </span>
                                    </button>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:when>
                <c:otherwise>
                    <div class="admin-section-heading">
                        <div>
                            <h2><spring:message code="admin.carRequests.title"/></h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty pendingRequests}">
                            <div class="admin-empty-state">
                                <p><spring:message code="admin.carRequests.empty"/></p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="admin-requests-grid">
                                <c:forEach var="request" items="${pendingRequests}">
                                    <c:url var="requestReviewUrl" value="/admin/requests/${request.id}/review"/>
                                    <pa:car-card
                                            model="${request.brandName} ${request.model}"
                                            year="${request.year}"
                                            bodyType="${request.bodyTypeName}"
                                            carId="${request.id}"
                                            hasImage="${request.hasImage}"
                                            imageUrl="${request.imageUrl}"
                                            href="${requestReviewUrl}"
                                            submitter="${request.submitter}"
                                            footerText="${pendingStatusLabel}"
                                            actionText="${reviewActionLabel}"/>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>

            <spring:message var="adminPaginationLabel" code="admin.pagination.aria"/>
            <c:if test="${not empty totalPages and totalPages > 1}">
                <pa:pagination currentPage="${currentPage}"
                               totalPages="${totalPages}"
                               baseUrl="${adminBaseUrl}"
                               extraParams="${adminPaginationParams}"
                               ariaLabel="${adminPaginationLabel}"/>
            </c:if>
        </section>

    </main>

    <pa:admin-catalog-request-review-modal/>
    <pa:moderator-application-review-modal/>
    <c:choose>
        <c:when test="${not empty param.carAccepted}">
            <pa:toast messageCode="admin.carRequest.accept.toast.success"/>
        </c:when>
        <c:when test="${not empty param.carRejected}">
            <pa:toast messageCode="admin.carRequest.reject.toast.success"/>
        </c:when>
        <c:when test="${not empty param.catalogAccepted}">
            <pa:toast messageCode="admin.catalogRequest.accept.toast.success"/>
        </c:when>
        <c:when test="${not empty param.catalogRejected}">
            <pa:toast messageCode="admin.catalogRequest.reject.toast.success"/>
        </c:when>
        <c:when test="${not empty param.catalogAcceptError}">
            <pa:toast messageCode="admin.catalogRequest.accept.toast.error" type="error"/>
        </c:when>
        <c:when test="${not empty param.catalogError}">
            <pa:toast messageCode="admin.catalogRequest.toast.error" type="error"/>
        </c:when>
        <c:when test="${not empty param.requestAccepted}">
            <pa:toast messageCode="admin.request.accept.toast.success"/>
        </c:when>
        <c:when test="${not empty param.requestRejected}">
            <pa:toast messageCode="admin.request.reject.toast.success"/>
        </c:when>
        <c:when test="${not empty param.requestError}">
            <pa:toast messageCode="admin.request.toast.error" type="error"/>
        </c:when>
        <c:otherwise>
            <pa:toast/>
        </c:otherwise>
    </c:choose>
    <pa:script src="/js/shared/modal-utils.js"/>
    <pa:script src="/js/admin/admin-catalog-request-modal.js"/>
    <pa:script src="/js/admin/moderator-application-review-modal.js"/>
    <pa:script src="/js/shared/toast.js"/>
    <pa:footer/>
</body>
</html>
