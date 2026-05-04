<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="mode" required="true" type="java.lang.String" %>
<%@ attribute name="tagsBySentiment" required="false" type="java.util.Map" %>
<%@ attribute name="tags" required="false" type="java.util.List" %>
<%@ attribute name="selectedTagIds" required="false" type="java.util.Collection" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<c:choose>
    <c:when test="${mode eq 'edit'}">
        <c:if test="${not empty tagsBySentiment}">
            <spring:message var="jsMsgTagMaxSelected" code="js.review.tags.maxSelected"/>
            <spring:message var="jsMsgTagOpposites" code="js.review.tags.opposites"/>
            <div class="review-tag-chips review-tag-chips--edit"
                 data-review-tag-chips
                 data-max-selected="6"
                 data-msg-max-selected="${fn:escapeXml(jsMsgTagMaxSelected)}"
                 data-msg-opposites="${fn:escapeXml(jsMsgTagOpposites)}">
                <fieldset class="review-tag-chips-group review-tag-chips-group--positive">
                    <legend><spring:message code="review.tags.positive"/></legend>
                    <div class="review-tag-chips-row">
                        <c:forEach var="tag" items="${tagsBySentiment['positive']}">
                            <c:set var="isChecked" value="${not empty selectedTagIds and selectedTagIds.contains(tag.id)}"/>
                            <c:set var="tagEmojiKey" value="review.tag.emoji.${tag.code}"/>
                            <spring:message code="review.tag.emoji.fallback" var="tagEmojiFallback" text="🏷️"/>
                            <spring:message code="${tagEmojiKey}" var="tagEmojiDisplay" text="${tagEmojiFallback}"/>
                            <label class="review-tag-chip review-tag-chip--positive ${isChecked ? 'is-selected' : ''}">
                                <input type="checkbox"
                                       name="tagIds"
                                       value="${tag.id}"
                                       data-dimension="${fn:escapeXml(tag.dimension)}"
                                       <c:if test="${isChecked}">checked</c:if>>
                                <span class="review-tag-chip-emoji" aria-hidden="true"><c:out value="${tagEmojiDisplay}"/></span>
                                <span class="review-tag-chip-text"><pa:review-tag-label tag="${tag}"/></span>
                            </label>
                        </c:forEach>
                    </div>
                </fieldset>
                <fieldset class="review-tag-chips-group review-tag-chips-group--negative">
                    <legend><spring:message code="review.tags.negative"/></legend>
                    <div class="review-tag-chips-row">
                        <c:forEach var="tag" items="${tagsBySentiment['negative']}">
                            <c:set var="isChecked" value="${not empty selectedTagIds and selectedTagIds.contains(tag.id)}"/>
                            <c:set var="tagEmojiKey" value="review.tag.emoji.${tag.code}"/>
                            <spring:message code="review.tag.emoji.fallback" var="tagEmojiFallback" text="🏷️"/>
                            <spring:message code="${tagEmojiKey}" var="tagEmojiDisplay" text="${tagEmojiFallback}"/>
                            <label class="review-tag-chip review-tag-chip--negative ${isChecked ? 'is-selected' : ''}">
                                <input type="checkbox"
                                       name="tagIds"
                                       value="${tag.id}"
                                       data-dimension="${fn:escapeXml(tag.dimension)}"
                                       <c:if test="${isChecked}">checked</c:if>>
                                <span class="review-tag-chip-emoji" aria-hidden="true"><c:out value="${tagEmojiDisplay}"/></span>
                                <span class="review-tag-chip-text"><pa:review-tag-label tag="${tag}"/></span>
                            </label>
                        </c:forEach>
                    </div>
                </fieldset>
                <p class="review-tag-chips-hint"><spring:message code="review.tags.help"/></p>
            </div>
        </c:if>
    </c:when>
    <c:otherwise>
        <c:if test="${not empty tags}">
            <ul class="review-tag-chips review-tag-chips--display">
                <c:forEach var="tag" items="${tags}">
                    <c:set var="tagEmojiKey" value="review.tag.emoji.${tag.code}"/>
                    <spring:message code="review.tag.emoji.fallback" var="tagEmojiFallback" text="🏷️"/>
                    <spring:message code="${tagEmojiKey}" var="tagEmojiDisplay" text="${tagEmojiFallback}"/>
                    <li class="review-tag-chip review-tag-chip--display review-tag-chip--${tag.sentiment}">
                        <span class="review-tag-chip-glyph" aria-hidden="true"><c:out value="${tagEmojiDisplay}"/></span>
                        <span class="review-tag-chip-label"><pa:review-tag-label tag="${tag}"/></span>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </c:otherwise>
</c:choose>
