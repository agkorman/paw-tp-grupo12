<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="requestBrandUrl" value="/brand-requests"/>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="brandNamePlaceholder" code="request.brand.placeholder.name"/>
<spring:message var="brandCommentsPlaceholder" code="request.brand.placeholder.comments"/>

<div id="requestBrandModal" class="review-modal catalog-request-modal" hidden>
    <div class="review-modal-overlay" data-close-catalog-request-modal></div>
    <section class="review-modal-dialog catalog-request-dialog" role="dialog" aria-modal="true" aria-labelledby="requestBrandModalTitle">
        <header class="review-modal-header">
            <div>
                <span class="review-modal-kicker"><spring:message code="request.brand.kicker"/></span>
                <h2 id="requestBrandModalTitle"><spring:message code="request.brand.title"/></h2>
            </div>
            <button type="button" class="review-modal-close" data-close-catalog-request-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </header>

        <p class="car-modal-subtitle">
            <spring:message code="request.brand.description"/>
        </p>

        <form id="requestBrandForm" class="catalog-request-form" method="post" action="${requestBrandUrl}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestBrandName"><spring:message code="request.brand.name"/></label>
                <input id="requestBrandName" name="name" type="text"
                       maxlength="80" required
                       placeholder="${brandNamePlaceholder}">
            </div>

            <div class="review-modal-field review-modal-field-wide">
                <label for="requestBrandComments"><spring:message code="request.brand.comments"/></label>
                <textarea id="requestBrandComments" name="comments" rows="3" maxlength="500"
                          placeholder="${brandCommentsPlaceholder}"></textarea>
            </div>

            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-catalog-request-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary"><spring:message code="common.action.submit"/></button>
            </div>
        </form>
    </section>
</div>
