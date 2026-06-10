<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="reviewId" required="true" %>
<%@ attribute name="liked" required="false" %>
<%@ attribute name="likeCount" required="false" %>
<%@ attribute name="disabled" required="false" %>
<%@ attribute name="action" required="false" %>
<%@ attribute name="label" required="false" %>
<%@ attribute name="redirect" required="false" type="java.lang.String" %>
<%@ attribute name="addAriaLabel" required="false" type="java.lang.String" %>
<%@ attribute name="removeAriaLabel" required="false" type="java.lang.String" %>
<%@ attribute name="readonly" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="defaultLikeLabel" code="review.like.label"/>
<spring:message var="likeLoginLabel" code="review.like.login"/>
<spring:message var="likeAddLabel" code="review.like.add.aria"/>
<spring:message var="likeRemoveLabel" code="review.like.remove.aria"/>
<c:set var="likeLabel" value="${empty label ? defaultLikeLabel : label}"/>
<c:set var="likeDisabled" value="${empty disabled ? false : disabled}"/>
<c:set var="likeReadonly" value="${empty readonly ? false : readonly}"/>
<c:set var="effectiveLikeAddLabel" value="${empty addAriaLabel ? likeAddLabel : addAriaLabel}"/>
<c:set var="effectiveLikeRemoveLabel" value="${empty removeAriaLabel ? likeRemoveLabel : removeAriaLabel}"/>
<c:set var="likeCurrentPath" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
<c:if test="${empty likeCurrentPath}">
    <c:set var="likeCurrentPath" value="/"/>
</c:if>
<c:set var="likeQueryStr" value="${requestScope['javax.servlet.forward.query_string']}"/>
<c:url var="likeLoginUrl" value="/login">
    <c:param name="redirect" value="${empty likeQueryStr ? likeCurrentPath : likeCurrentPath.concat('?').concat(likeQueryStr)}"/>
</c:url>

<c:choose>
    <c:when test="${likeReadonly}">
        <span
                class="review-like-toggle ${liked ? 'is-active' : ''}"
                aria-disabled="true">
            <pa:icon name="heart" size="17"/>
            <span class="review-like-label"><c:out value="${likeLabel}"/></span>
            <span class="review-like-count" data-review-like-count><c:out value="${empty likeCount ? 0 : likeCount}"/></span>
        </span>
    </c:when>
    <c:when test="${not empty action}">
        <c:choose>
            <c:when test="${likeDisabled}">
                <a href="${likeLoginUrl}"
                   class="review-like-toggle ${liked ? 'is-active' : ''}"
                   aria-label="${fn:escapeXml(likeLoginLabel)}">
                    <pa:icon name="heart" size="17"/>
                    <span class="review-like-label"><c:out value="${likeLabel}"/></span>
                    <span class="review-like-count" data-review-like-count><c:out value="${empty likeCount ? 0 : likeCount}"/></span>
                </a>
            </c:when>
            <c:otherwise>
                <form method="post"
                      action="${fn:escapeXml(action)}"
                      class="review-like-form">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <c:if test="${not empty redirect}">
                        <input type="hidden" name="redirect" value="${fn:escapeXml(redirect)}">
                    </c:if>
                    <button
                            type="submit"
                            class="review-like-toggle ${liked ? 'is-active' : ''}"
                            aria-pressed="${liked}"
                            aria-label="${fn:escapeXml(liked ? effectiveLikeRemoveLabel : effectiveLikeAddLabel)}">
                        <pa:icon name="heart" size="17"/>
                        <span class="review-like-label"><c:out value="${likeLabel}"/></span>
                        <span class="review-like-count" data-review-like-count><c:out value="${empty likeCount ? 0 : likeCount}"/></span>
                    </button>
                </form>
            </c:otherwise>
        </c:choose>
    </c:when>
</c:choose>
