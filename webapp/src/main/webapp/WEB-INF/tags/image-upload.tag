<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ attribute name="namePrefix" required="true" %>
<%@ attribute name="inputName" required="true" %>
<%@ attribute name="required" required="false" type="java.lang.Boolean" %>
<%@ attribute name="mode" required="false" %>
<%@ attribute name="adminMode" required="false" type="java.lang.Boolean" %>
<%@ attribute name="existingImageIds" required="false" type="java.util.Collection" %>
<%@ attribute name="emptyFileStatus" required="false" %>
<%@ attribute name="previousImageLabel" required="false" %>
<%@ attribute name="nextImageLabel" required="false" %>
<%@ attribute name="removeImageLabel" required="false" %>
<%@ attribute name="titleCreateKey" required="false" %>
<%@ attribute name="titleEditKey" required="false" %>
<%@ attribute name="titleReadonlyKey" required="false" %>
<%@ attribute name="helpKey" required="false" %>
<%@ attribute name="helpGalleryKey" required="false" %>
<%@ attribute name="labelKey" required="false" %>
<%@ attribute name="errorPath" required="false" %>

<c:set var="resolvedRequired" value="${empty required ? false : required}"/>
<c:set var="resolvedAdminMode" value="${empty adminMode ? false : adminMode}"/>
<c:set var="resolvedMode" value="${empty mode ? 'create' : mode}"/>
<c:set var="resolvedTitleCreateKey" value="${empty titleCreateKey ? 'cars.form.image.uploadTitle' : titleCreateKey}"/>
<c:set var="resolvedTitleEditKey" value="${empty titleEditKey ? 'cars.form.image.currentPlural' : titleEditKey}"/>
<c:set var="resolvedTitleReadonlyKey" value="${empty titleReadonlyKey ? 'cars.form.image.reviewReadonly' : titleReadonlyKey}"/>
<c:set var="resolvedHelpKey" value="${empty helpKey ? 'cars.form.image.help' : helpKey}"/>
<c:set var="resolvedHelpGalleryKey" value="${empty helpGalleryKey ? 'cars.form.image.galleryHelp' : helpGalleryKey}"/>
<c:set var="resolvedLabelKey" value="${empty labelKey ? 'cars.form.images' : labelKey}"/>
<c:set var="resolvedErrorPath" value="${empty errorPath ? 'files' : errorPath}"/>

<spring:message var="resolvedPreviousImageLabel" code="cars.image.previous"/>
<spring:message var="resolvedNextImageLabel" code="cars.image.next"/>
<spring:message var="resolvedRemoveImageLabel" code="cars.form.image.remove"/>
<spring:message var="resolvedEmptyFileStatus" code="cars.form.image.none"/>
<c:if test="${not empty previousImageLabel}"><c:set var="resolvedPreviousImageLabel" value="${previousImageLabel}"/></c:if>
<c:if test="${not empty nextImageLabel}"><c:set var="resolvedNextImageLabel" value="${nextImageLabel}"/></c:if>
<c:if test="${not empty removeImageLabel}"><c:set var="resolvedRemoveImageLabel" value="${removeImageLabel}"/></c:if>
<c:if test="${not empty emptyFileStatus}"><c:set var="resolvedEmptyFileStatus" value="${emptyFileStatus}"/></c:if>

<div class="modal-field modal-field-wide car-image-field">
    <span class="car-image-label"><spring:message code="${resolvedLabelKey}"/></span>
    <div class="car-image-upload">
        <c:if test="${resolvedAdminMode and not empty existingImageIds}">
            <span id="${namePrefix}RetainedImageInputs" hidden>
                <c:forEach var="existingImageId" items="${existingImageIds}">
                    <input type="hidden" name="retainedImageIds" value="${existingImageId}">
                </c:forEach>
            </span>
        </c:if>
        <c:choose>
            <c:when test="${resolvedRequired}">
                <form:input id="${namePrefix}File" path="${inputName}" type="file"
                            cssClass="car-image-upload-input"
                            accept="image/jpeg,image/png,image/webp"
                            multiple="multiple"
                            required="required"
                            aria-describedby="${namePrefix}FileHelp ${namePrefix}FileStatus"/>
            </c:when>
            <c:otherwise>
                <form:input id="${namePrefix}File" path="${inputName}" type="file"
                            cssClass="car-image-upload-input"
                            accept="image/jpeg,image/png,image/webp"
                            multiple="multiple"
                            aria-describedby="${namePrefix}FileHelp ${namePrefix}FileStatus"/>
            </c:otherwise>
        </c:choose>
        <label class="car-image-upload-card" for="${namePrefix}File">
            <span class="car-image-upload-icon" aria-hidden="true">
                <pa:icon name="image-upload" size="28"/>
            </span>
            <span id="${namePrefix}ImagePreview" class="car-image-upload-preview" hidden aria-hidden="true">
                <button id="${namePrefix}ImagePrev" class="car-image-upload-preview-nav car-image-upload-preview-prev" type="button" aria-label="${resolvedPreviousImageLabel}">
                    <pa:icon name="chevron-left" size="14"/>
                </button>
                <img id="${namePrefix}ImagePreviewImg" alt="">
                <button id="${namePrefix}ImageRemove" class="car-image-upload-preview-remove" type="button" aria-label="${resolvedRemoveImageLabel}" hidden>
                    <pa:icon name="close" size="14"/>
                </button>
                <button id="${namePrefix}ImageNext" class="car-image-upload-preview-nav car-image-upload-preview-next" type="button" aria-label="${resolvedNextImageLabel}">
                    <pa:icon name="chevron-right" size="14"/>
                </button>
                <span id="${namePrefix}ImageCounter" class="car-image-upload-preview-counter">1 / 1</span>
            </span>
            <span class="car-image-upload-copy">
                <strong id="${namePrefix}FileTitle">
                    <c:choose>
                        <c:when test="${resolvedMode eq 'review-request'}"><spring:message code="${resolvedTitleReadonlyKey}"/></c:when>
                        <c:when test="${resolvedMode eq 'edit-car' or resolvedMode eq 'edit'}"><spring:message code="${resolvedTitleEditKey}"/></c:when>
                        <c:otherwise><spring:message code="${resolvedTitleCreateKey}"/></c:otherwise>
                    </c:choose>
                </strong>
                <span id="${namePrefix}FileHelp">
                    <c:choose>
                        <c:when test="${resolvedAdminMode}"><spring:message code="${resolvedHelpGalleryKey}"/></c:when>
                        <c:otherwise><spring:message code="${resolvedHelpKey}"/></c:otherwise>
                    </c:choose>
                </span>
                <span id="${namePrefix}FileStatus" class="car-image-upload-status"><c:out value="${resolvedEmptyFileStatus}"/></span>
                <span id="${namePrefix}ImageThumbnails" class="car-image-upload-thumbnails" hidden></span>
            </span>
        </label>
    </div>
    <form:errors path="${resolvedErrorPath}" cssClass="form-error" element="span"/>
</div>
