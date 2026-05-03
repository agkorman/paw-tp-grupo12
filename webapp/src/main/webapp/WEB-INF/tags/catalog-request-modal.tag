<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="id" required="true" %>
<%@ attribute name="formId" required="true" %>
<%@ attribute name="action" required="true" %>
<%@ attribute name="titleId" required="true" %>
<%@ attribute name="kickerCode" required="true" %>
<%@ attribute name="titleCode" required="true" %>
<%@ attribute name="descriptionCode" required="true" %>
<%@ attribute name="nameLabelCode" required="true" %>
<%@ attribute name="commentsLabelCode" required="true" %>
<%@ attribute name="nameInputId" required="true" %>
<%@ attribute name="commentsInputId" required="true" %>
<%@ attribute name="namePlaceholder" required="false" %>
<%@ attribute name="commentsPlaceholder" required="false" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="closeModalLabel" code="common.action.close"/>

<div id="${fn:escapeXml(id)}" class="review-modal catalog-request-modal" hidden>
    <div class="review-modal-overlay" data-close-catalog-request-modal></div>
    <section class="review-modal-dialog catalog-request-dialog" role="dialog" aria-modal="true" aria-labelledby="${fn:escapeXml(titleId)}">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker"><spring:message code="${kickerCode}"/></span>
                <h2 id="${fn:escapeXml(titleId)}"><spring:message code="${titleCode}"/></h2>
            </div>
            <button type="button" class="review-modal-close" data-close-catalog-request-modal aria-label="${fn:escapeXml(closeModalLabel)}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="car-modal-subtitle">
            <spring:message code="${descriptionCode}"/>
        </p>

        <form id="${fn:escapeXml(formId)}" class="catalog-request-form" method="post" action="${fn:escapeXml(action)}"
              novalidate="novalidate" data-catalog-request-form="true">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="review-modal-field review-modal-field-wide">
                <label for="${fn:escapeXml(nameInputId)}"><spring:message code="${nameLabelCode}"/></label>
                <input id="${fn:escapeXml(nameInputId)}" name="name" type="text"
                       maxlength="80" required
                       placeholder="${fn:escapeXml(namePlaceholder)}">
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="${fn:escapeXml(commentsInputId)}"><spring:message code="${commentsLabelCode}"/></label>
                <textarea id="${fn:escapeXml(commentsInputId)}" name="comments" rows="3" maxlength="500"
                          placeholder="${fn:escapeXml(commentsPlaceholder)}"></textarea>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-catalog-request-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary"><spring:message code="common.action.submit"/></button>
            </div>
        </form>
    </section>
</div>
