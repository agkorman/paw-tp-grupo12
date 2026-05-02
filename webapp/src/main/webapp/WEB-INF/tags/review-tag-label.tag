<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="tag" required="true" type="ar.edu.itba.paw.model.ReviewTag" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:remove var="tagLabelResolveError" scope="page"/>
<c:remove var="tagLabelResolved" scope="page"/>
<c:set var="tagLabelKey" value="review.tag.${tag.code}"/>
<c:catch var="tagLabelResolveError">
    <spring:message code="${tagLabelKey}" var="tagLabelResolved"/>
</c:catch>
<c:choose>
    <c:when test="${empty tagLabelResolveError}">
        <c:out value="${tagLabelResolved}"/>
    </c:when>
    <c:otherwise>
        <spring:message code="review.tag.missing">
            <spring:argument value="${tag.code}"/>
        </spring:message>
    </c:otherwise>
</c:choose>
