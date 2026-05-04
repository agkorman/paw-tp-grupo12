<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewId" required="true" %>
<%@ attribute name="liked" required="false" %>
<%@ attribute name="likeCount" required="false" %>
<%@ attribute name="disabled" required="false" %>
<%@ attribute name="action" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="defaultLikeLabel" code="review.like.label"/>
<spring:message var="likeActionLabel" code="review.like.action"/>
<spring:message var="likeLoginLabel" code="review.like.login"/>
<spring:message var="likeAddLabel" code="review.like.add.aria"/>
<spring:message var="likeRemoveLabel" code="review.like.remove.aria"/>
<c:set var="likeLabel" value="${empty label ? defaultLikeLabel : label}"/>
<c:set var="likeDisabled" value="${empty disabled ? false : disabled}"/>
<c:set var="likeCurrentPath" value="${pageContext.request.requestURI}"/>
<c:set var="likeQueryStr" value="${pageContext.request.queryString}"/>
<c:url var="likeLoginUrl" value="/login">
    <c:param name="redirect" value="${empty likeQueryStr ? likeCurrentPath : likeCurrentPath.concat('?').concat(likeQueryStr)}"/>
    <c:param name="intent" value="like-${reviewId}"/>
</c:url>

<c:choose>
    <c:when test="${not empty action}">
        <c:choose>
            <c:when test="${likeDisabled}">
                <a href="${likeLoginUrl}"
                   class="review-like-toggle ${liked ? 'is-active' : ''}"
                   data-auth-resume-intent="like-${fn:escapeXml(reviewId)}"
                   aria-label="${likeLoginLabel}">
                    <pa:icon name="heart" size="17"/>
                    <span class="review-like-label"><c:out value="${likeLabel}"/></span>
                    <span class="review-like-count" data-review-like-count><c:out value="${empty likeCount ? 0 : likeCount}"/></span>
                </a>
            </c:when>
            <c:otherwise>
                <form method="post"
                      action="${fn:escapeXml(action)}"
                      class="review-like-form"
                      data-enhanced-review-like="true"
                      data-auth-resume-intent="like-${fn:escapeXml(reviewId)}">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button
                            type="submit"
                            class="review-like-toggle ${liked ? 'is-active' : ''}"
                            data-review-like-toggle
                            data-review-id="${fn:escapeXml(reviewId)}"
                            data-liked="${liked}"
                            data-like-add-label="${fn:escapeXml(likeAddLabel)}"
                            data-like-remove-label="${fn:escapeXml(likeRemoveLabel)}"
                            aria-pressed="${liked}"
                            aria-label="${liked ? likeRemoveLabel : likeAddLabel}">
                        <pa:icon name="heart" size="17"/>
                        <span class="review-like-label"><c:out value="${likeLabel}"/></span>
                        <span class="review-like-count" data-review-like-count><c:out value="${empty likeCount ? 0 : likeCount}"/></span>
                    </button>
                </form>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <button
                type="button"
                class="review-like-toggle ${liked ? 'is-active' : ''}"
                data-review-like-toggle
                data-review-id="${fn:escapeXml(reviewId)}"
                data-liked="${liked}"
                data-like-add-label="${fn:escapeXml(likeAddLabel)}"
                data-like-remove-label="${fn:escapeXml(likeRemoveLabel)}"
                aria-pressed="${liked}"
                aria-label="${liked ? likeRemoveLabel : likeAddLabel}"
                data-auth-resume-intent="like-${fn:escapeXml(reviewId)}"
                <c:if test="${likeDisabled}">
                    aria-disabled="true"
                    data-auth-required="true"
                    data-auth-required-action="${likeActionLabel}"
                    data-auth-required-intent="like-${fn:escapeXml(reviewId)}"
                </c:if>>
                <pa:icon name="heart" size="17"/>
                <span class="review-like-label"><c:out value="${likeLabel}"/></span>
                <span class="review-like-count" data-review-like-count><c:out value="${empty likeCount ? 0 : likeCount}"/></span>
            </button>
    </c:otherwise>
</c:choose>
