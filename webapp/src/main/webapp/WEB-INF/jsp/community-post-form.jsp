<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="${repostMode ? 'communities.postForm.repost.title' : 'communities.postForm.title'}" styles="/css/community-post-common.css|/css/community-post-form.css|/css/communities-responsive.css|/css/car-image-upload.css|/css/reposted-review-card.css|/css/review-tags.css"/>
<body>
    <pa:nav activePage="communities"/>
    <spring:message var="communityPostFormToolbarAria" code="communities.postForm.toolbar.aria"/>
    <c:choose>
        <c:when test="${repostMode}">
            <c:url var="communityPostFormActionUrl" value="/reviews/${communityPostForm.linkedReviewId}/repost"/>
        </c:when>
        <c:when test="${editMode}">
            <c:url var="communityPostFormActionUrl" value="/communities/${community.slug}/posts/${postSlug}/edit"/>
        </c:when>
        <c:otherwise>
            <c:url var="communityPostFormActionUrl" value="/communities/${community.slug}/posts"/>
        </c:otherwise>
    </c:choose>
    <c:set var="communityPostExistingImageUrls" value=""/>
    <c:set var="communityPostExistingImageIds" value=""/>
    <c:if test="${editMode and not empty existingPostImageIds}">
        <c:forTokens var="imgId" items="${existingPostImageIds}" delims=",">
            <c:url var="communityPostExistingImageUrl" value="/communities/${community.slug}/posts/${postSlug}/images/${imgId}"/>
            <c:set var="communityPostExistingImageUrls" value="${communityPostExistingImageUrls}${empty communityPostExistingImageUrls ? '' : '|'}${communityPostExistingImageUrl}"/>
            <c:set var="communityPostExistingImageIds" value="${communityPostExistingImageIds}${empty communityPostExistingImageIds ? '' : '|'}${imgId}"/>
        </c:forTokens>
    </c:if>
    <main class="community-post-form-page">
        <form:form cssClass="community-post-form-shell"
                   modelAttribute="communityPostForm"
                   method="post"
                   action="${fn:escapeXml(communityPostFormActionUrl)}"
                   enctype="multipart/form-data"
                   novalidate="novalidate">
            <c:if test="${editMode and not empty editRedirect}">
                <input type="hidden" name="redirect" value="${fn:escapeXml(editRedirect)}">
            </c:if>
            <c:if test="${repostMode}">
                <form:hidden path="linkedReviewId"/>
            </c:if>
            <form:errors cssClass="alert alert-error" element="div"/>
            <div class="community-post-form-header">
                <c:choose>
                    <c:when test="${repostMode}">
                        <div class="community-post-form-community-picker">
                            <label for="communitySlugSelect"><spring:message code="communities.postForm.repost.selectCommunity"/></label>
                            <spring:message var="communityPickerPlaceholder" code="communities.postForm.repost.selectCommunity.placeholder"/>
                            <select id="communitySlugSelect" name="communitySlug" required>
                                <option value="" disabled ${empty communityPostForm.communitySlug ? 'selected' : ''}><c:out value="${communityPickerPlaceholder}"/></option>
                                <c:forEach var="comm" items="${joinedCommunities}">
                                    <option value="${fn:escapeXml(comm.slug)}" ${communityPostForm.communitySlug eq comm.slug ? 'selected' : ''}>
                                        <c:out value="${comm.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                            <form:errors path="communitySlug" cssClass="form-error" element="span"/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="community-post-form-target">
                            <spring:message code="communities.postForm.postingTo"/>
                            <strong class="community-post-form-target-name"># <c:out value="${community.name}"/></strong>
                        </p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="community-post-form-card">
                <div class="community-post-form-title-block">
                    <label for="communityPostTitle"><spring:message code="communities.postForm.field.title"/></label>
                    <form:textarea id="communityPostTitle"
                                   path="title"
                                   rows="2"
                                   maxlength="120"
                                   required="required"/>
                    <form:errors path="title" cssClass="form-error" element="span"/>
                </div>

                <div class="community-post-form-body-block">
                    <label for="communityPostBody"><spring:message code="communities.postForm.field.body"/></label>
                    <form:textarea id="communityPostBody"
                                   path="body"
                                   rows="8"
                                   maxlength="5000"
                                   required="required"/>
                    <form:errors path="body" cssClass="form-error" element="span"/>
                </div>

                <pa:image-upload
                    namePrefix="communityPost"
                    inputName="files"
                    required="false"
                    mode="${editMode ? 'edit' : 'create'}"
                    maxImageCount="3"
                    existingImageUrlsJoined="${communityPostExistingImageUrls}"
                    existingImageIdsJoined="${communityPostExistingImageIds}"
                    labelKey="communities.postForm.images"
                    titleCreateKey="communities.postForm.image.uploadTitle"
                    titleEditKey="communities.postForm.image.addMore"
                    helpKey="communities.postForm.image.help"/>
            </div>

            <c:if test="${repostMode and not empty repostReview}">
                <div class="community-post-form-repost-preview">
                    <pa:reposted-review-card repostReview="${repostReview}"/>
                </div>
            </c:if>

            <div class="community-post-form-footer">
                <div class="community-post-form-actions">
                    <div class="community-post-form-actions-group">
                        <button type="submit" class="btn-primary">
                            <c:choose>
                                <c:when test="${editMode}"><spring:message code="communities.postForm.update"/></c:when>
                                <c:otherwise><spring:message code="communities.postForm.submit"/></c:otherwise>
                            </c:choose>
                        </button>
                    </div>
                </div>
            </div>
        </form:form>
    </main>
    <pa:script src="/js/shared/image-upload-picker.js"/>
    <c:if test="${repostMode}">
        <pa:script src="/js/communities/community-picker.js"/>
    </c:if>
    <pa:footer/>
</body>
</html>
