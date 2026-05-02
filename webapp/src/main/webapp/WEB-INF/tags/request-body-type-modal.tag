<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="requestBodyTypeUrl" value="/body-type-requests"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="bodyTypeNamePlaceholder" code="request.bodyType.placeholder.name"/>
<spring:message var="bodyTypeCommentsPlaceholder" code="request.bodyType.placeholder.comments"/>

<div id="requestBodyTypeModal" class="review-modal catalog-request-modal" hidden>
    <div class="review-modal-overlay" data-close-catalog-request-modal></div>
    <section class="review-modal-dialog catalog-request-dialog" role="dialog" aria-modal="true" aria-labelledby="requestBodyTypeModalTitle">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker"><spring:message code="request.bodyType.kicker"/></span>
                <h2 id="requestBodyTypeModalTitle"><spring:message code="request.bodyType.title"/></h2>
            </div>
            <button type="button" class="review-modal-close" data-close-catalog-request-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="car-modal-subtitle">
            <spring:message code="request.bodyType.description"/>
        </p>

        <form id="requestBodyTypeForm" class="catalog-request-form" method="post" action="${requestBodyTypeUrl}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestBodyTypeName"><spring:message code="request.bodyType.name"/></label>
                <input id="requestBodyTypeName" name="name" type="text"
                       maxlength="80" required
                       placeholder="${bodyTypeNamePlaceholder}">
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestBodyTypeComments"><spring:message code="request.bodyType.comments"/></label>
                <textarea id="requestBodyTypeComments" name="comments" rows="3" maxlength="500"
                          placeholder="${bodyTypeCommentsPlaceholder}"></textarea>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-catalog-request-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary"><spring:message code="common.action.submit"/></button>
            </div>
        </form>
    </section>
</div>
