<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="review" required="true" type="ar.edu.itba.paw.model.Review" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${not empty review.userId}">
        <c:url var="authorProfileUrl" value="/profiles/${review.userId}"/>
        <a class="review-author-link" href="${authorProfileUrl}">
            <c:choose>
                <c:when test="${not empty review.reviewerUsername}">
                    <c:out value="${review.reviewerUsername}"/>
                </c:when>
                <c:when test="${not empty review.reviewerEmail}">
                    <c:out value="${review.reviewerEmail}"/>
                </c:when>
                <c:otherwise>Usuario</c:otherwise>
            </c:choose>
        </a>
    </c:when>
    <c:when test="${not empty review.reviewerUsername}">
        <span><c:out value="${review.reviewerUsername}"/></span>
    </c:when>
    <c:otherwise>
        <span>anon</span>
    </c:otherwise>
</c:choose>
