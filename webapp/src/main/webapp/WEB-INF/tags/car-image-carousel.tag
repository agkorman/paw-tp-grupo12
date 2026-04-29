<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="true" %>
<%@ attribute name="model" required="true" %>
<%@ attribute name="images" required="false" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="imageCount" value="${empty images ? 0 : fn:length(images)}"/>

<c:choose>
    <c:when test="${imageCount gt 0}">
        <div class="car-image-carousel" data-car-image-carousel>
            <div class="car-image-carousel-stage">
                <c:forEach var="image" items="${images}" varStatus="status">
                    <c:url var="imageUrl" value="/cars/${carId}/images/${image.imageId}"/>
                    <img class="car-image-carousel-slide"
                         src="${imageUrl}"
                         alt="${fn:escapeXml(model)}"
                         loading="${status.first ? 'eager' : 'lazy'}"
                         data-carousel-slide
                         <c:if test="${not status.first}">hidden</c:if>>
                </c:forEach>
                <c:if test="${imageCount gt 1}">
                    <button type="button" class="car-image-carousel-nav car-image-carousel-prev"
                            data-carousel-prev aria-label="Imagen anterior">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <path d="M15 18l-6-6 6-6"/>
                        </svg>
                    </button>
                    <button type="button" class="car-image-carousel-nav car-image-carousel-next"
                            data-carousel-next aria-label="Imagen siguiente">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                            <path d="M9 6l6 6-6 6"/>
                        </svg>
                    </button>
                    <span class="car-image-carousel-count" data-carousel-count>1 / ${imageCount}</span>
                </c:if>
            </div>
            <c:if test="${imageCount gt 1}">
                <div class="car-image-carousel-thumbs" aria-label="Imágenes del auto">
                    <c:forEach var="image" items="${images}" varStatus="status">
                        <c:url var="thumbUrl" value="/cars/${carId}/images/${image.imageId}"/>
                        <button type="button"
                                class="car-image-carousel-thumb ${status.first ? 'is-active' : ''}"
                                data-carousel-thumb="${status.index}"
                                aria-label="Ver imagen ${status.count}">
                            <img src="${thumbUrl}" alt="" loading="lazy">
                        </button>
                    </c:forEach>
                </div>
            </c:if>
        </div>
    </c:when>
    <c:otherwise>
        <div class="img-placeholder">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
            </svg>
        </div>
    </c:otherwise>
</c:choose>
