<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="highlights" required="false" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty highlights}">
    <div class="recommendation-reason">
        <span class="recommendation-reason-label">Reviewers say</span>
        <ul class="recommendation-highlight-list">
            <c:forEach var="highlight" items="${highlights}">
                <li class="review-tag-chip review-tag-chip--display review-tag-chip--positive recommendation-highlight">
                    <span class="review-tag-chip-glyph" aria-hidden="true">+</span>
                    <span><c:out value="${highlight.tag.labelEs}"/></span>
                    <span class="recommendation-highlight-percent"><c:out value="${highlight.percentage}"/>%</span>
                </li>
            </c:forEach>
        </ul>
    </div>
</c:if>
