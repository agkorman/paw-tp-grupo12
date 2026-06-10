<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="titleCode" required="true" %>
<%@ attribute name="bodyCode" required="true" %>
<%@ attribute name="confirmCode" required="true" %>
<%@ attribute name="placeholderCode" required="true" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="reasonRequiredMessage" code="communities.hide.reason.required"/>
<spring:message var="reasonMinMessage" code="communities.hide.reason.min" arguments="10"/>
<spring:message var="reasonMaxMessage" code="communities.hide.reason.max" arguments="600"/>
<spring:message var="reasonPlaceholder" code="${placeholderCode}"/>
<div id="${fn:escapeXml(id)}"
     class="modal community-hide-modal"
     hidden
     data-community-hide-modal>
    <div class="modal-overlay" data-close-community-hide-modal></div>
    <section class="modal-dialog review-hide-dialog" role="dialog" aria-modal="true" aria-labelledby="${fn:escapeXml(id)}Title">
        <header class="modal-header">
            <div>
                <span class="modal-kicker"><spring:message code="review.hide.kicker"/></span>
                <h2 id="${fn:escapeXml(id)}Title"><spring:message code="${titleCode}"/></h2>
            </div>
            <button type="button" class="modal-close" data-close-community-hide-modal aria-label="${fn:escapeXml(closeModalLabel)}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>
        <form class="modal-form review-hide-form"
              method="post"
              enctype="multipart/form-data"
              novalidate="novalidate"
              data-community-hide-form
              data-reason-required-message="${fn:escapeXml(reasonRequiredMessage)}"
              data-reason-min-message="${fn:escapeXml(reasonMinMessage)}"
              data-reason-max-message="${fn:escapeXml(reasonMaxMessage)}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="redirect" value="" data-community-hide-redirect>
            <p class="modal-subtitle"><spring:message code="${bodyCode}"/></p>
            <div class="modal-field modal-field-wide">
                <label for="${fn:escapeXml(id)}Reason"><spring:message code="communities.hide.reason.label"/></label>
                <textarea id="${fn:escapeXml(id)}Reason"
                          name="reason"
                          rows="5"
                          minlength="10"
                          maxlength="600"
                          required
                          aria-describedby="${fn:escapeXml(id)}ReasonError"
                          placeholder="${fn:escapeXml(reasonPlaceholder)}"
                          data-community-hide-reason></textarea>
                <span id="${fn:escapeXml(id)}ReasonError" class="client-form-error" data-community-hide-error hidden></span>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn-secondary" data-close-community-hide-modal>
                    <spring:message code="common.action.cancel"/>
                </button>
                <button type="submit" class="btn-primary">
                    <spring:message code="${confirmCode}"/>
                </button>
            </div>
        </form>
    </section>
</div>
