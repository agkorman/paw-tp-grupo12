<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="requestAdminUrl" value="/admin-requests"/>
<spring:message var="motivationPlaceholder" code="request.admin.placeholder.motivation"/>
<spring:message var="bioPlaceholder" code="request.admin.placeholder.bio"/>
<spring:message var="justificationPlaceholder" code="request.admin.placeholder.justification"/>
<spring:message var="jsRequiredGeneric" code="js.form.required.generic"/>
<spring:message var="jsLengthMax" code="js.form.length.max"/>
<spring:message var="jsRequiredMotivation" code="js.adminRequest.required.motivation"/>
<spring:message var="jsRequiredBio" code="js.adminRequest.required.bio"/>
<spring:message var="jsRequiredJustification" code="js.adminRequest.required.justification"/>

<div id="requestAdminModal" class="modal admin-request-submit-modal" hidden>
    <div class="modal-overlay" data-close-request-admin-modal></div>
    <section class="modal-dialog admin-request-submit-dialog"
             role="dialog" aria-modal="true" aria-labelledby="requestAdminModalTitle">
        <header class="modal-header">
            <div>
                <span class="modal-kicker"><spring:message code="request.admin.kicker"/></span>
                <h2 id="requestAdminModalTitle"><spring:message code="request.admin.title"/></h2>
            </div>
        </header>

        <p class="car-modal-subtitle">
            <spring:message code="request.admin.description"/>
        </p>

        <form id="requestAdminForm" class="admin-request-submit-form" method="post" action="${requestAdminUrl}"
              enctype="multipart/form-data" novalidate="novalidate"
              data-msg-required-generic="${fn:escapeXml(jsRequiredGeneric)}"
              data-msg-length-max="${fn:escapeXml(jsLengthMax)}"
              data-msg-required-motivation="${fn:escapeXml(jsRequiredMotivation)}"
              data-msg-required-bio="${fn:escapeXml(jsRequiredBio)}"
              data-msg-required-justification="${fn:escapeXml(jsRequiredJustification)}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="modal-field modal-field-wide">
                <label for="requestAdminMotivation"><spring:message code="request.admin.motivation"/></label>
                <textarea id="requestAdminMotivation" name="motivation" rows="5" maxlength="2000"
                          required
                          placeholder="${motivationPlaceholder}"></textarea>
            </div>

            <div class="modal-field modal-field-wide">
                <label for="requestAdminBio"><spring:message code="request.admin.bio"/></label>
                <textarea id="requestAdminBio" name="bio" rows="5" maxlength="2000"
                          required
                          placeholder="${bioPlaceholder}"></textarea>
            </div>

            <div class="modal-field modal-field-wide">
                <label for="requestAdminJustification"><spring:message code="request.admin.justification"/></label>
                <textarea id="requestAdminJustification" name="justification" rows="5" maxlength="2000"
                          required
                          placeholder="${justificationPlaceholder}"></textarea>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn-secondary" data-close-request-admin-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary"><spring:message code="request.admin.submit"/></button>
            </div>
        </form>
    </section>
</div>
