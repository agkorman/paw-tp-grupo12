<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="carId" required="true" %>
<%@ attribute name="model" required="true" %>
<%@ attribute name="images" required="false" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="imageCount" value="${empty images ? 0 : fn:length(images)}"/>
<spring:message var="previousImageLabel" code="cars.image.previous"/>
<spring:message var="nextImageLabel" code="cars.image.next"/>
<spring:message var="carouselLabel" code="cars.image.carousel"/>

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
                            data-carousel-prev aria-label="${previousImageLabel}">
                        <pa:icon name="chevron-left" size="18"/>
                    </button>
                    <button type="button" class="car-image-carousel-nav car-image-carousel-next"
                            data-carousel-next aria-label="${nextImageLabel}">
                        <pa:icon name="chevron-right" size="18"/>
                    </button>
                    <span class="car-image-carousel-count" data-carousel-count>1 / ${imageCount}</span>
                </c:if>
            </div>
            <c:if test="${imageCount gt 1}">
                <div class="car-image-carousel-thumbs" aria-label="${carouselLabel}">
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
            <pa:icon name="car-placeholder" size="48"/>
        </div>
    </c:otherwise>
</c:choose>
