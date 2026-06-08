<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="review.detail.title" styles="/css/reviews.css|/css/review-tags.css|/css/profile-modal.css|/css/image-lightbox.css"/>
<body>
    <pa:nav activePage="reviews"/>

    <main class="reviews-page review-detail-page">
        <section class="review-hero">
            <div class="review-hero-inner">
                <div>
                    <h1>
                        <c:out value="${selectedCar.brandName}"/> <c:out value="${selectedCar.model}"/>
                    </h1>
                    <p class="subtitle"><spring:message code="review.detail.subtitle"/></p>
                </div>
                <div class="review-hero-actions">
                    <c:url var="backToCarUrl" value="/reviews/car/${selectedCar.id}"/>
                    <a href="${backToCarUrl}" class="btn-secondary">
                        <spring:message code="review.detail.backToCar"/>
                    </a>
                </div>
            </div>
        </section>

        <c:set var="repliesBaseRedirect" value="/reviews/${reviewThread.review.id}"/>
        <c:if test="${not empty repliesCurrentPage and repliesCurrentPage > 1}">
            <c:set var="repliesBaseRedirect" value="${repliesBaseRedirect}?repliesPage=${repliesCurrentPage}"/>
        </c:if>

        <pa:reviews-feed reviews="${reviews}"
                         reviewThreads="${reviewThreads}"
                         carId="${selectedCar.id}"
                         currentUserId="${currentUserId}"
                         hideFeedHeader="${true}"
                         repliesPaginated="${true}"
                         repliesCurrentPage="${repliesCurrentPage}"
                         repliesTotalPages="${repliesTotalPages}"
                         contextRedirectBase="${repliesBaseRedirect}"/>
    </main>

    <pa:auth-required-modal/>
    <pa:toast messageCode="${actionToastCode}"/>
    <sec:authorize access="isAuthenticated()">
        <pa:confirmation-modal id="deleteReviewConfirmModal"
                               titleCode="review.delete.title"
                               bodyCode="review.delete.body"
                               confirmCode="common.action.delete"
                               confirmCssClass="btn-primary"/>
    </sec:authorize>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:review-hide-modal/>
    </sec:authorize>

    <pa:script src="/js/shared/action-menu.js"/>
    <pa:script src="/js/reviews/review-anchor-highlight.js"/>
    <pa:script src="/js/reviews/reply-validation.js"/>
    <pa:script src="/js/reviews/reply-disclosure.js"/>
    <pa:script src="/js/reviews/review-tag-chips.js" defer="true"/>
    <pa:script src="/js/shared/modal-utils.js"/>
    <pa:script src="/js/auth/auth-required-modal.js"/>
    <pa:script src="/js/shared/toast.js"/>
    <sec:authorize access="isAuthenticated()">
        <pa:script src="/js/shared/confirmation-modal.js"/>
        <pa:script src="/js/reviews/reply-edit.js"/>
    </sec:authorize>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:script src="/js/reviews/review-moderation.js"/>
    </sec:authorize>
    <pa:footer/>
</body>
</html>
