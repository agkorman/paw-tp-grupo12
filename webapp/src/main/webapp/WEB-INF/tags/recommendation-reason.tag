<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="positives" required="false" type="java.util.List" %>
<%@ attribute name="negatives" required="false" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:if test="${not empty positives or not empty negatives}">
    <spring:message code="review.tags.filterCarsByTag" var="filterCarsByTagAria"/>
    <div class="recommendation-reason">
        <c:if test="${not empty positives}">
            <ul class="recommendation-rail-list">
                <c:forEach var="highlight" items="${positives}">
                    <c:if test="${highlight.visible}">
                        <c:set var="recTagEmojiKey" value="review.tag.emoji.${highlight.tag.code}"/>
                        <spring:message code="review.tag.emoji.fallback" var="recTagEmojiFallback" text="🏷️"/>
                        <spring:message code="${recTagEmojiKey}" var="recTagEmojiDisplay" text="${recTagEmojiFallback}"/>
                        <c:url var="recTagFilterUrl" value="/cars">
                            <c:param name="tagCode" value="${highlight.tag.code}"/>
                        </c:url>
                        <li class="review-tag-chip review-tag-chip--display review-tag-chip--positive recommendation-rail-item">
                            <a class="review-tag-chip-link" href="${recTagFilterUrl}" aria-label="${fn:escapeXml(filterCarsByTagAria)}">
                                <span class="review-tag-chip-glyph" aria-hidden="true"><c:out value="${recTagEmojiDisplay}"/></span>
                                <span class="review-tag-chip-label"><pa:review-tag-label tag="${highlight.tag}"/></span>
                            </a>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </c:if>
        <c:if test="${not empty negatives}">
            <ul class="recommendation-rail-list">
                <c:forEach var="highlight" items="${negatives}">
                    <c:if test="${highlight.visible}">
                        <c:set var="recTagEmojiKey" value="review.tag.emoji.${highlight.tag.code}"/>
                        <spring:message code="review.tag.emoji.fallback" var="recTagEmojiFallback" text="🏷️"/>
                        <spring:message code="${recTagEmojiKey}" var="recTagEmojiDisplay" text="${recTagEmojiFallback}"/>
                        <c:url var="recTagFilterUrl" value="/cars">
                            <c:param name="tagCode" value="${highlight.tag.code}"/>
                        </c:url>
                        <li class="review-tag-chip review-tag-chip--display review-tag-chip--negative recommendation-rail-item">
                            <a class="review-tag-chip-link" href="${recTagFilterUrl}" aria-label="${fn:escapeXml(filterCarsByTagAria)}">
                                <span class="review-tag-chip-glyph" aria-hidden="true"><c:out value="${recTagEmojiDisplay}"/></span>
                                <span class="review-tag-chip-label"><pa:review-tag-label tag="${highlight.tag}"/></span>
                            </a>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </c:if>
    </div>
</c:if>
