<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="reasonRequiredMessage" code="review.hide.reason.required"/>
<spring:message var="reasonMinMessage" code="review.hide.reason.min" arguments="10"/>
<spring:message var="reasonMaxMessage" code="review.hide.reason.max" arguments="600"/>
<spring:message var="reasonPlaceholder" code="review.hide.reason.placeholder"/>
<div id="hideReviewModal"
     class="modal review-hide-modal"
     hidden
     data-hide-review-modal>
    <div class="modal-overlay" data-close-hide-review-modal></div>
    <section class="modal-dialog review-hide-dialog" role="dialog" aria-modal="true" aria-labelledby="hideReviewTitle">
        <header class="modal-header">
            <div>
                <span class="modal-kicker"><spring:message code="review.hide.kicker"/></span>
                <h2 id="hideReviewTitle"><spring:message code="review.hide.title"/></h2>
            </div>
            <button type="button" class="modal-close" data-close-hide-review-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>
        <form id="hideReviewForm"
              class="modal-form review-hide-form"
              method="post"
              novalidate="novalidate"
              data-reason-required-message="${fn:escapeXml(reasonRequiredMessage)}"
              data-reason-min-message="${fn:escapeXml(reasonMinMessage)}"
              data-reason-max-message="${fn:escapeXml(reasonMaxMessage)}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="redirect" value="" data-hide-review-redirect>
            <p class="modal-subtitle"><spring:message code="review.hide.body"/></p>
            <div class="modal-field modal-field-wide">
                <label for="hideReviewReason"><spring:message code="review.hide.reason.label"/></label>
                <textarea id="hideReviewReason"
                          name="reason"
                          rows="5"
                          minlength="10"
                          maxlength="600"
                          required
                          aria-describedby="hideReviewReasonError"
                          placeholder="${fn:escapeXml(reasonPlaceholder)}"></textarea>
                <span id="hideReviewReasonError" class="client-form-error" data-hide-review-error hidden></span>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn-secondary" data-close-hide-review-modal>
                    <spring:message code="common.action.cancel"/>
                </button>
                <button type="submit" class="btn-primary">
                    <spring:message code="review.hide.confirm"/>
                </button>
            </div>
        </form>
    </section>
</div>
