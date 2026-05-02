<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="positives" required="false" type="java.util.List" %>
<%@ attribute name="negatives" required="false" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:if test="${not empty positives or not empty negatives}">
    <div class="recommendation-reason">
        <c:if test="${not empty positives}">
            <ul class="recommendation-highlight-list">
                <c:forEach var="highlight" items="${positives}">
                    <c:if test="${highlight.visible}">
                        <li class="review-tag-chip review-tag-chip--display recommendation-highlight ${highlight.tierClass}">
                            <span class="recommendation-highlight-emoji recommendation-highlight-emoji--tier" aria-hidden="true"><c:out value="${highlight.tierEmoji}"/></span>
                            <c:set var="recTagEmojiKey" value="review.tag.emoji.${highlight.tag.code}"/>
                            <spring:message code="review.tag.emoji.fallback" var="recTagEmojiFallback" text="🏷️"/>
                            <spring:message code="${recTagEmojiKey}" var="recTagEmojiDisplay" text="${recTagEmojiFallback}"/>
                            <span class="recommendation-highlight-emoji recommendation-highlight-emoji--topic" aria-hidden="true"><c:out value="${recTagEmojiDisplay}"/></span>
                            <span class="recommendation-highlight-tag"><c:out value="${highlight.tag.labelEs}"/></span>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </c:if>
        <c:if test="${not empty negatives}">
            <ul class="recommendation-highlight-list recommendation-highlight-list--warning">
                <c:forEach var="highlight" items="${negatives}">
                    <c:if test="${highlight.visible}">
                        <li class="review-tag-chip review-tag-chip--display recommendation-highlight ${highlight.tierClass}">
                            <span class="recommendation-highlight-emoji recommendation-highlight-emoji--tier" aria-hidden="true"><c:out value="${highlight.tierEmoji}"/></span>
                            <c:set var="recTagEmojiKey" value="review.tag.emoji.${highlight.tag.code}"/>
                            <spring:message code="review.tag.emoji.fallback" var="recTagEmojiFallback" text="🏷️"/>
                            <spring:message code="${recTagEmojiKey}" var="recTagEmojiDisplay" text="${recTagEmojiFallback}"/>
                            <span class="recommendation-highlight-emoji recommendation-highlight-emoji--topic" aria-hidden="true"><c:out value="${recTagEmojiDisplay}"/></span>
                            <span class="recommendation-highlight-tag"><c:out value="${highlight.tag.labelEs}"/></span>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </c:if>
    </div>
</c:if>
