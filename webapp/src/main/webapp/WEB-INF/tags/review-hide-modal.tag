<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="reasonPlaceholder" code="review.hide.reason.placeholder"/>
<spring:message var="successMessage" code="review.hide.toast.success"/>
<spring:message var="errorMessage" code="review.hide.toast.error"/>

<div id="hideReviewModal"
     class="review-modal review-hide-modal"
     hidden
     data-hide-review-modal
     data-success-message="${fn:escapeXml(successMessage)}"
     data-error-message="${fn:escapeXml(errorMessage)}">
    <div class="review-modal-overlay" data-close-hide-review-modal></div>
    <section class="review-modal-dialog review-hide-dialog" role="dialog" aria-modal="true" aria-labelledby="hideReviewTitle">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker"><spring:message code="review.hide.kicker"/></span>
                <h2 id="hideReviewTitle"><spring:message code="review.hide.title"/></h2>
            </div>
            <button type="button" class="review-modal-close" data-close-hide-review-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>
        <form id="hideReviewForm"
              class="review-modal-form review-hide-form"
              method="post"
              novalidate="novalidate">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <p class="review-modal-subtitle"><spring:message code="review.hide.body"/></p>
            <div class="review-modal-field review-modal-field-wide">
                <label for="hideReviewReason"><spring:message code="review.hide.reason.label"/></label>
                <textarea id="hideReviewReason"
                          name="reason"
                          rows="5"
                          placeholder="${fn:escapeXml(reasonPlaceholder)}"></textarea>
                <span class="client-form-error" data-hide-review-error hidden></span>
            </div>
            <div class="review-modal-actions">
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
