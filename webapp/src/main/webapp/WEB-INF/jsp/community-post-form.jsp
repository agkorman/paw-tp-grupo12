<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.postForm.title" styles="/css/community-post-common.css|/css/community-post-form.css|/css/communities-responsive.css"/>
<body>
    <pa:nav activePage="communities"/>
    <spring:message var="communityPostFormToolbarAria" code="communities.postForm.toolbar.aria"/>
    <c:choose>
        <c:when test="${editMode}">
            <c:url var="communityPostFormActionUrl" value="/communities/${community.slug}/posts/${postSlug}/edit"/>
        </c:when>
        <c:otherwise>
            <c:url var="communityPostFormActionUrl" value="/communities/${community.slug}/posts"/>
        </c:otherwise>
    </c:choose>
    <main class="community-post-form-page">
        <form:form cssClass="community-post-form-shell"
                   modelAttribute="communityPostForm"
                   method="post"
                   action="${fn:escapeXml(communityPostFormActionUrl)}"
                   novalidate="novalidate">
            <form:errors cssClass="alert alert-error" element="div"/>
            <div class="community-post-form-header">
                <p class="community-post-form-target">
                    <spring:message code="communities.postForm.postingTo"/>
                    <strong class="community-post-form-target-name"># <c:out value="${community.name}"/></strong>
                </p>
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
            </div>

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
    <pa:footer/>
</body>
</html>
