<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="titleCode" required="true" %>
<%@ attribute name="bodyCode" required="true" %>
<%@ attribute name="confirmCode" required="false" %>
<%@ attribute name="confirmCssClass" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="closeModalLabel" code="common.action.close"/>
<c:set var="resolvedConfirmCode" value="${empty confirmCode ? 'common.action.confirm' : confirmCode}"/>
<c:set var="resolvedConfirmCssClass" value="${empty confirmCssClass ? 'btn-primary' : confirmCssClass}"/>

<div id="${id}" class="profile-modal confirmation-modal" hidden data-confirmation-modal>
    <div class="profile-modal-overlay" data-close-confirmation-modal></div>
    <section class="profile-modal-dialog profile-confirmation-dialog"
             role="dialog"
             aria-modal="true"
             aria-labelledby="${id}Title">
        <div class="profile-modal-header">
            <h2 id="${id}Title"><spring:message code="${titleCode}"/></h2>
            <button type="button" class="profile-modal-close" data-close-confirmation-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </div>

        <div class="profile-confirmation-body">
            <p><spring:message code="${bodyCode}"/></p>
            <div class="profile-modal-actions">
                <button type="button" class="btn-secondary" data-close-confirmation-modal>
                    <spring:message code="common.action.cancel"/>
                </button>
                <button type="button" class="${resolvedConfirmCssClass} profile-confirmation-confirm" data-confirmation-submit>
                    <spring:message code="${resolvedConfirmCode}"/>
                </button>
            </div>
        </div>
    </section>
</div>
