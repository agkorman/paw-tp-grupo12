<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="adminBaseUrl" value="/admin"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="commentsEmptyPlaceholder" code="admin.catalogRequest.comments.empty"/>

<div id="adminCatalogRequestModal"
     class="review-modal admin-catalog-request-review-modal"
     hidden
     data-admin-base-url="${adminBaseUrl}">
    <div class="review-modal-overlay" data-close-admin-catalog-request-modal></div>
    <section class="review-modal-dialog admin-catalog-request-review-dialog"
             role="dialog" aria-modal="true" aria-labelledby="adminCatalogRequestTitle">
        <header class="review-modal-header">
            <div>
                <span id="adminCatalogRequestKicker" class="review-modal-kicker"><spring:message code="admin.catalogRequest.kicker"/></span>
                <h2 id="adminCatalogRequestTitle"><spring:message code="admin.catalogRequest.title"/></h2>
            </div>
            <button type="button" class="review-modal-close"
                    data-close-admin-catalog-request-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <div class="admin-catalog-request-review-grid">
            <div class="review-modal-field">
                <label for="adminCatalogRequestName"><spring:message code="admin.catalogRequest.name"/></label>
                <input id="adminCatalogRequestName" type="text" maxlength="80" readonly>
            </div>

            <div class="review-modal-field">
                <label for="adminCatalogRequestSubmitter"><spring:message code="common.label.sentBy"/></label>
                <input id="adminCatalogRequestSubmitter" type="text" maxlength="100" readonly>
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="adminCatalogRequestComments"><spring:message code="admin.catalogRequest.comments"/></label>
                <textarea id="adminCatalogRequestComments" rows="4" maxlength="500" readonly
                          placeholder="${commentsEmptyPlaceholder}"></textarea>
            </div>
        </div>

        <div class="review-modal-actions">
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
