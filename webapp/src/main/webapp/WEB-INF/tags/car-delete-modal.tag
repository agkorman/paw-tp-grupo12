<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="closeModalLabel" code="common.action.close"/>

<div id="deleteCarModal" class="review-modal" hidden>
    <div class="review-modal-overlay" data-close-delete-car-modal></div>
    <section class="review-modal-dialog" role="dialog" aria-modal="true" aria-labelledby="deleteCarTitle">
        <div class="review-modal-header">
            <div>
                <span class="review-modal-kicker"><spring:message code="cars.delete.kicker"/></span>
                <h2 id="deleteCarTitle"><spring:message code="cars.delete.title"/></h2>
            </div>
            <button type="button" class="review-modal-close" data-close-delete-car-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </div>
        <form id="deleteCarForm" class="car-delete-form" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <p><spring:message code="cars.delete.body"/></p>
            <strong data-delete-car-title></strong>
            <div class="review-modal-actions">
                <button type="button" class="btn-secondary" data-close-delete-car-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary car-delete-confirm-button"><spring:message code="common.action.delete"/></button>
            </div>
        </form>
    </section>
</div>
