<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ attribute name="imageUrls" required="false" type="java.util.List" %>
<%@ attribute name="imageUrlsJoined" required="false" %>
<%@ attribute name="altKey" required="true" %>
<%@ attribute name="cssClass" required="false" %>
<%@ attribute name="maxVisible" required="false" type="java.lang.Integer" %>

<c:set var="resolvedImageUrls" value="${imageUrls}"/>
<c:if test="${empty resolvedImageUrls and not empty imageUrlsJoined}">
    <c:set var="resolvedImageUrls" value="${fn:split(imageUrlsJoined, '|')}"/>
</c:if>

<c:if test="${not empty resolvedImageUrls}">
    <c:set var="resolvedCssClass" value="${empty cssClass ? '' : ' '.concat(cssClass)}"/>
    <c:set var="totalCount" value="${fn:length(resolvedImageUrls)}"/>
    <c:set var="joinedImageUrls" value=""/>
    <c:forEach var="imageUrl" items="${resolvedImageUrls}" varStatus="status">
        <c:url var="resolvedUrl" value="${imageUrl}"/>
        <c:set var="joinedImageUrls" value="${joinedImageUrls}${status.first ? '' : '|'}${resolvedUrl}"/>
    </c:forEach>
    <c:set var="hasOverflow" value="${not empty maxVisible and totalCount > maxVisible}"/>
    <c:set var="overflowCount" value="${hasOverflow ? totalCount - maxVisible : 0}"/>
    <spring:message var="overflowLabel" code="image.gallery.overflow" arguments="${overflowCount}"/>
    <div class="image-gallery${fn:escapeXml(resolvedCssClass)}"
         data-image-gallery
         data-image-gallery-urls="${fn:escapeXml(joinedImageUrls)}">
        <c:forEach var="imageUrl" items="${resolvedImageUrls}" varStatus="status">
            <c:if test="${not hasOverflow or status.index < maxVisible}">
                <c:url var="resolvedUrl" value="${imageUrl}"/>
                <spring:message var="galleryAlt" code="${altKey}" arguments="${status.index + 1}"/>
                <button type="button"
                        class="image-gallery-thumb-button"
                        data-image-gallery-index="${status.index}"
                        aria-label="${fn:escapeXml(galleryAlt)}">
                    <img src="${fn:escapeXml(resolvedUrl)}"
                         alt="${fn:escapeXml(galleryAlt)}"
                         class="image-gallery-thumb"/>
                </button>
            </c:if>
        </c:forEach>
        <c:if test="${hasOverflow}">
            <button type="button"
                    class="image-gallery-overflow-button"
                    data-image-gallery-index="${maxVisible}"
                    aria-label="${fn:escapeXml(overflowLabel)}">
                <span class="image-gallery-overflow-count">+<c:out value="${overflowCount}"/></span>
            </button>
        </c:if>
    </div>
</c:if>
