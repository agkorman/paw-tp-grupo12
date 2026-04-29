<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="mode" required="true" type="java.lang.String" %>
<%@ attribute name="tagsBySentiment" required="false" type="java.util.Map" %>
<%@ attribute name="tags" required="false" type="java.util.List" %>
<%@ attribute name="selectedTagIds" required="false" type="java.util.Collection" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:choose>
    <c:when test="${mode eq 'edit'}">
        <c:if test="${not empty tagsBySentiment}">
            <div class="review-tag-chips review-tag-chips--edit"
                 data-review-tag-chips
                 data-review-tag-max="6">
                <fieldset class="review-tag-chips-group review-tag-chips-group--positive">
                    <legend>¿Qué destacarías de este auto?</legend>
                    <div class="review-tag-chips-row">
                        <c:forEach var="tag" items="${tagsBySentiment['positive']}">
                            <c:set var="isChecked" value="${not empty selectedTagIds and selectedTagIds.contains(tag.id)}"/>
                            <label class="review-tag-chip review-tag-chip--positive ${isChecked ? 'is-selected' : ''}">
                                <input type="checkbox"
                                       name="tagIds"
                                       value="${tag.id}"
                                       data-dimension="${fn:escapeXml(tag.dimension)}"
                                       <c:if test="${isChecked}">checked</c:if>>
                                <span><c:out value="${tag.labelEs}"/></span>
                            </label>
                        </c:forEach>
                    </div>
                </fieldset>
                <fieldset class="review-tag-chips-group review-tag-chips-group--negative">
                    <legend>¿Qué no te gustó?</legend>
                    <div class="review-tag-chips-row">
                        <c:forEach var="tag" items="${tagsBySentiment['negative']}">
                            <c:set var="isChecked" value="${not empty selectedTagIds and selectedTagIds.contains(tag.id)}"/>
                            <label class="review-tag-chip review-tag-chip--negative ${isChecked ? 'is-selected' : ''}">
                                <input type="checkbox"
                                       name="tagIds"
                                       value="${tag.id}"
                                       data-dimension="${fn:escapeXml(tag.dimension)}"
                                       <c:if test="${isChecked}">checked</c:if>>
                                <span><c:out value="${tag.labelEs}"/></span>
                            </label>
                        </c:forEach>
                    </div>
                </fieldset>
                <p class="review-tag-chips-hint">Podés elegir hasta 6 etiquetas. No mezcles opuestos del mismo tema.</p>
            </div>
        </c:if>
    </c:when>
    <c:otherwise>
        <c:if test="${not empty tags}">
            <ul class="review-tag-chips review-tag-chips--display">
                <c:forEach var="tag" items="${tags}">
                    <li class="review-tag-chip review-tag-chip--display review-tag-chip--${tag.sentiment}">
                        <c:if test="${tag.sentiment eq 'positive'}">
                            <span class="review-tag-chip-glyph" aria-hidden="true">+</span>
                        </c:if>
                        <c:if test="${tag.sentiment eq 'negative'}">
                            <span class="review-tag-chip-glyph" aria-hidden="true">−</span>
                        </c:if>
                        <span><c:out value="${tag.labelEs}"/></span>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </c:otherwise>
</c:choose>
