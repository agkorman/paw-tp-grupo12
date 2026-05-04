<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="adminBaseUrl" value="/admin"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="commentsEmptyPlaceholder" code="admin.catalogRequest.comments.empty"/>
<spring:message var="acceptSuccessMsg" code="admin.request.accept.toast.success"/>
<spring:message var="rejectSuccessMsg" code="admin.request.reject.toast.success"/>
<spring:message var="requestErrorMsg" code="admin.request.toast.error"/>
<spring:message var="brandKicker" code="admin.catalogRequest.brand.kicker"/>
<spring:message var="brandTitle" code="admin.catalogRequest.brand.title"/>
<spring:message var="bodyTypeKicker" code="admin.catalogRequest.bodyType.kicker"/>
<spring:message var="bodyTypeTitle" code="admin.catalogRequest.bodyType.title"/>

<div id="adminCatalogRequestModal"
     class="modal admin-catalog-request-review-modal"
     hidden
     data-admin-base-url="${adminBaseUrl}"
     data-accept-success-msg="${fn:escapeXml(acceptSuccessMsg)}"
     data-reject-success-msg="${fn:escapeXml(rejectSuccessMsg)}"
     data-error-msg="${fn:escapeXml(requestErrorMsg)}"
     data-brand-kicker="${fn:escapeXml(brandKicker)}"
     data-brand-title="${fn:escapeXml(brandTitle)}"
     data-body-type-kicker="${fn:escapeXml(bodyTypeKicker)}"
     data-body-type-title="${fn:escapeXml(bodyTypeTitle)}">
    <div class="modal-overlay" data-close-admin-catalog-request-modal></div>
    <section class="modal-dialog admin-catalog-request-review-dialog"
             role="dialog" aria-modal="true" aria-labelledby="adminCatalogRequestTitle">
        <header class="modal-header">
            <div>
                <span id="adminCatalogRequestKicker" class="modal-kicker"><spring:message code="admin.catalogRequest.kicker"/></span>
                <h2 id="adminCatalogRequestTitle"><spring:message code="admin.catalogRequest.title"/></h2>
            </div>
            <button type="button" class="modal-close"
                    data-close-admin-catalog-request-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <div class="admin-catalog-request-review-grid">
            <div class="modal-field">
                <label for="adminCatalogRequestName"><spring:message code="admin.catalogRequest.name"/></label>
                <input id="adminCatalogRequestName" type="text" maxlength="80" readonly>
            </div>

            <div class="modal-field">
                <label for="adminCatalogRequestSubmitter"><spring:message code="common.label.sentBy"/></label>
                <input id="adminCatalogRequestSubmitter" type="text" maxlength="100" readonly>
            </div>

            <div class="modal-field modal-field-wide">
                <label for="adminCatalogRequestComments"><spring:message code="admin.catalogRequest.comments"/></label>
                <textarea id="adminCatalogRequestComments" rows="4" maxlength="500" readonly
                          placeholder="${commentsEmptyPlaceholder}"></textarea>
            </div>
        </div>

        <div class="modal-actions">
            <form id="adminCatalogRejectForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="btn-secondary admin-reject-btn"><spring:message code="common.action.reject"/></button>
            </form>
            <form id="adminCatalogAcceptForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="name" id="adminCatalogAcceptName">
                <button type="submit" class="btn-primary"><spring:message code="common.action.accept"/></button>
            </form>
        </div>
    </section>
</div>
