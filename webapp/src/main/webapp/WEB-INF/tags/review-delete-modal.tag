<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="closeModalLabel" code="common.action.close"/>

<div id="deleteReviewModal" class="profile-modal" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-delete-dialog" role="dialog" aria-modal="true" aria-labelledby="deleteReviewTitle">
        <div class="profile-modal-header">
            <h2 id="deleteReviewTitle"><spring:message code="review.delete.title"/></h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="18"/>
            </button>
        </div>

        <form id="deleteReviewForm" class="profile-delete-form" method="post">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <p><spring:message code="review.delete.body"/></p>
            <p class="profile-delete-review-title" data-delete-review-title></p>

            <div class="profile-modal-actions">
                <button type="button" class="btn-secondary" data-close-profile-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary profile-delete-confirm-button"><spring:message code="common.action.delete"/></button>
            </div>
        </form>
    </section>
</div>
