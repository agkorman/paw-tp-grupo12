<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<spring:message var="closeLabel" code="common.action.close"/>
<spring:message var="previousLabel" code="cars.image.previous"/>
<spring:message var="nextLabel" code="cars.image.next"/>

<div id="sharedImageLightbox" class="image-lightbox" hidden aria-hidden="true" role="dialog" aria-modal="true">
    <div class="image-lightbox-backdrop" data-image-lightbox-close></div>
    <div class="image-lightbox-stage">
        <button type="button" class="image-lightbox-close" data-image-lightbox-close
                aria-label="${closeLabel}">
            <pa:icon name="close" size="18"/>
        </button>
        <button type="button" class="image-lightbox-nav image-lightbox-prev" data-image-lightbox-prev
                aria-label="${previousLabel}">
            <pa:icon name="chevron-left" size="18"/>
        </button>
        <img class="image-lightbox-img" data-image-lightbox-img alt="">
        <button type="button" class="image-lightbox-nav image-lightbox-next" data-image-lightbox-next
                aria-label="${nextLabel}">
            <pa:icon name="chevron-right" size="18"/>
        </button>
        <span class="image-lightbox-count" data-image-lightbox-count>1 / 1</span>
    </div>
</div>
