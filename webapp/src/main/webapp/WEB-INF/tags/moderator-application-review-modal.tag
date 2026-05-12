<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="adminBaseUrl" value="/admin"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<div id="adminRequestReviewModal"
     class="modal admin-request-review-modal"
     hidden
     data-admin-base-url="${adminBaseUrl}">
    <div class="modal-overlay" data-close-admin-request-review-modal></div>
    <section class="modal-dialog admin-request-review-dialog"
             role="dialog" aria-modal="true" aria-labelledby="adminRequestReviewTitle">
        <header class="modal-header">
            <div>
                <span class="modal-kicker"><spring:message code="admin.moderatorRequest.kicker"/></span>
                <h2 id="adminRequestReviewTitle"><spring:message code="admin.moderatorRequest.title"/></h2>
            </div>
            <button type="button" class="modal-close"
                    data-close-admin-request-review-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="car-modal-subtitle">
            <spring:message code="admin.moderatorRequest.from"/> <strong id="adminRequestReviewSubmitter"></strong>.
        </p>

        <div class="admin-request-review-fields">
            <div class="modal-field modal-field-wide">
                <label for="adminRequestReviewMotivation"><spring:message code="admin.moderatorRequest.motivation"/></label>
                <textarea id="adminRequestReviewMotivation" rows="4" readonly></textarea>
            </div>
            <div class="modal-field modal-field-wide">
                <label for="adminRequestReviewBio"><spring:message code="admin.moderatorRequest.bio"/></label>
                <textarea id="adminRequestReviewBio" rows="4" readonly></textarea>
            </div>
            <div class="modal-field modal-field-wide">
                <label for="adminRequestReviewJustification"><spring:message code="admin.moderatorRequest.justification"/></label>
                <textarea id="adminRequestReviewJustification" rows="4" readonly></textarea>
            </div>
        </div>

        <div class="modal-actions">
            <form id="adminRequestRejectForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="btn-secondary admin-reject-btn"><spring:message code="common.action.reject"/></button>
            </form>
            <form id="adminRequestAcceptForm" method="post" action="">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <button type="submit" class="btn-primary"><spring:message code="common.action.accept"/></button>
            </form>
        </div>
    </section>
</div>
