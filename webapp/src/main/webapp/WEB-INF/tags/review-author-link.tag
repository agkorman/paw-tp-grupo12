<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="review" required="true" type="ar.edu.itba.paw.model.Review" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:choose>
    <c:when test="${not empty review.userId}">
        <c:url var="authorProfileUrl" value="/users/${review.userId}"/>
        <a class="review-author-link" href="${authorProfileUrl}">
            <c:choose>
                <c:when test="${not empty review.reviewerUsername}">
                    <c:out value="${review.reviewerUsername}"/>
                </c:when>
                <c:when test="${not empty review.reviewerEmail}">
                    <c:out value="${review.reviewerEmail}"/>
                </c:when>
                <c:otherwise><spring:message code="review.author.fallback"/></c:otherwise>
            </c:choose>
        </a>
    </c:when>
    <c:when test="${not empty review.reviewerUsername}">
        <span><c:out value="${review.reviewerUsername}"/></span>
    </c:when>
    <c:otherwise>
        <span><spring:message code="review.author.anonymous"/></span>
    </c:otherwise>
</c:choose>
