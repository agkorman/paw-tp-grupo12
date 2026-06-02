<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="imageUrls" required="false" type="java.util.List" %>
<%@ attribute name="imageUrlsJoined" required="false" %>
<%@ attribute name="altKey" required="true" %>
<%@ attribute name="cssClass" required="false" %>

<c:set var="resolvedImageUrls" value="${imageUrls}"/>
<c:if test="${empty resolvedImageUrls and not empty imageUrlsJoined}">
    <c:set var="resolvedImageUrls" value="${fn:split(imageUrlsJoined, '|')}"/>
</c:if>

<c:if test="${not empty resolvedImageUrls}">
    <c:set var="resolvedCssClass" value="${empty cssClass ? '' : ' '.concat(cssClass)}"/>
    <c:set var="joinedImageUrls" value=""/>
    <c:forEach var="imageUrl" items="${resolvedImageUrls}" varStatus="status">
        <c:set var="joinedImageUrls" value="${joinedImageUrls}${status.first ? '' : '|'}${imageUrl}"/>
    </c:forEach>
    <div class="image-gallery${fn:escapeXml(resolvedCssClass)}"
         data-image-gallery
         data-image-gallery-urls="${fn:escapeXml(joinedImageUrls)}">
        <c:forEach var="imageUrl" items="${resolvedImageUrls}" varStatus="status">
            <spring:message var="galleryAlt" code="${altKey}" arguments="${status.index + 1}"/>
            <button type="button"
                    class="image-gallery-thumb-button"
                    data-image-gallery-index="${status.index}"
                    aria-label="${fn:escapeXml(galleryAlt)}">
                <img src="${fn:escapeXml(imageUrl)}"
                     alt="${fn:escapeXml(galleryAlt)}"
                     class="image-gallery-thumb"/>
            </button>
        </c:forEach>
    </div>
</c:if>
