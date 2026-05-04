<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="requestAdminUrl" value="/admin-requests"/>
<spring:message var="motivationPlaceholder" code="request.admin.placeholder.motivation"/>
<spring:message var="bioPlaceholder" code="request.admin.placeholder.bio"/>
<spring:message var="justificationPlaceholder" code="request.admin.placeholder.justification"/>
<spring:message var="genericRequiredMessage" code="js.form.required.generic"/>
<spring:message var="motivationRequiredMessage" code="js.adminRequest.required.motivation"/>
<spring:message var="bioRequiredMessage" code="js.adminRequest.required.bio"/>
<spring:message var="justificationRequiredMessage" code="js.adminRequest.required.justification"/>

<div id="requestAdminModal" class="review-modal admin-request-submit-modal" hidden>
    <div class="review-modal-overlay" data-close-request-admin-modal></div>
    <section class="review-modal-dialog admin-request-submit-dialog"
             role="dialog" aria-modal="true" aria-labelledby="requestAdminModalTitle">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker"><spring:message code="request.admin.kicker"/></span>
                <h2 id="requestAdminModalTitle"><spring:message code="request.admin.title"/></h2>
            </div>
        </header>

        <p class="car-modal-subtitle">
            <spring:message code="request.admin.description"/>
        </p>

        <form id="requestAdminForm" class="admin-request-submit-form" method="post" action="${requestAdminUrl}"
              novalidate="novalidate"
              data-msg-required-generic="${fn:escapeXml(genericRequiredMessage)}"
              data-msg-required-motivation="${fn:escapeXml(motivationRequiredMessage)}"
              data-msg-required-bio="${fn:escapeXml(bioRequiredMessage)}"
              data-msg-required-justification="${fn:escapeXml(justificationRequiredMessage)}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestAdminMotivation"><spring:message code="request.admin.motivation"/></label>
                <textarea id="requestAdminMotivation" name="motivation" rows="5"
                          maxlength="2000" required
                          placeholder="${motivationPlaceholder}"></textarea>
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestAdminBio"><spring:message code="request.admin.bio"/></label>
                <textarea id="requestAdminBio" name="bio" rows="5"
                          maxlength="2000" required
                          placeholder="${bioPlaceholder}"></textarea>
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestAdminJustification"><spring:message code="request.admin.justification"/></label>
                <textarea id="requestAdminJustification" name="justification" rows="5"
                          maxlength="2000" required
                          placeholder="${justificationPlaceholder}"></textarea>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-request-admin-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary"><spring:message code="request.admin.submit"/></button>
            </div>
        </form>
    </section>
</div>
