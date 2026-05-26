<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="topics" required="true" type="java.util.Collection" %>
<%@ attribute name="selectedTopicIds" required="false" type="java.util.Collection" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:message var="jsMsgTopicMaxSelected" code="js.community.topics.maxSelected"/>
<div class="review-tag-chips review-tag-chips--edit community-topic-chips"
     data-community-topic-chips
     data-max-selected="4"
     data-msg-max-selected="${fn:escapeXml(jsMsgTopicMaxSelected)}">
    <fieldset class="review-tag-chips-group review-tag-chips-group--positive community-topic-chips-group">
        <legend><spring:message code="communities.create.topic.legend"/></legend>
        <div class="review-tag-chips-row">
            <c:forEach var="topic" items="${topics}">
                <c:set var="isChecked" value="false"/>
                <c:forEach var="selectedTopicId" items="${selectedTopicIds}">
                    <c:if test="${selectedTopicId eq topic.id}">
                        <c:set var="isChecked" value="true"/>
                    </c:if>
                </c:forEach>
                <c:set var="communityTopicChipClass" value="review-tag-chip review-tag-chip--positive"/>
                <c:if test="${isChecked}">
                    <c:set var="communityTopicChipClass" value="review-tag-chip review-tag-chip--positive is-selected"/>
                </c:if>
                <label class="${communityTopicChipClass}">
                    <input type="checkbox"
                           name="selectedTopicIds"
                           value="${topic.id}"
                           <c:if test="${isChecked}">checked</c:if>>
                    <span class="review-tag-chip-text"><spring:message code="${topic.labelCode}"/></span>
                </label>
            </c:forEach>
        </div>
    </fieldset>
    <p class="review-tag-chips-hint"><spring:message code="communities.create.topic.help"/></p>
</div>
