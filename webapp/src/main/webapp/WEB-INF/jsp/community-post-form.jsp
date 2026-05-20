<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.postForm.title" styles="/css/communities.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="communityDetailUrl" value="/communities/classics"/>
    <spring:message var="communityPostFormTypesAria" code="communities.postForm.typeTabs.aria"/>
    <spring:message var="communityPostFormToolbarAria" code="communities.postForm.toolbar.aria"/>
    <spring:message var="communityPostFormTitleValue" code="communities.postForm.sample.title"/>
    <spring:message var="communityPostFormBodyValue" code="communities.postForm.sample.body"/>
    <main class="community-post-form-page">
        <form class="community-post-form-shell" novalidate="novalidate">
            <div class="community-post-form-header">
                <p class="community-post-form-target"><spring:message code="communities.postForm.postingTo"/></p>
                <a class="community-post-form-community" href="${communityDetailUrl}">
                    <span class="community-post-form-community-avatar" aria-hidden="true"></span>
                    <strong><spring:message code="communities.postForm.community"/></strong>
                    <pa:icon name="chevron-down" size="12"/>
                </a>
            </div>

            <div class="community-post-type-tabs" role="tablist" aria-label="${communityPostFormTypesAria}">
                <button type="button" class="community-post-type-tab is-active"><spring:message code="communities.postForm.type.discussion"/></button>
                <button type="button" class="community-post-type-tab"><spring:message code="communities.postForm.type.link"/></button>
                <button type="button" class="community-post-type-tab"><spring:message code="communities.postForm.type.photo"/></button>
                <button type="button" class="community-post-type-tab"><spring:message code="communities.postForm.type.review"/></button>
            </div>

            <div class="community-post-form-card">
                <div class="community-post-form-title-block">
                    <label for="communityPostTitle"><spring:message code="communities.postForm.field.title"/></label>
                    <input id="communityPostTitle" type="text" maxlength="120" value="${communityPostFormTitleValue}">
                    <span>38 / 120</span>
                </div>

                <div class="community-post-form-body-block">
                    <label for="communityPostBody"><spring:message code="communities.postForm.field.body"/></label>
                    <textarea id="communityPostBody" rows="8"><c:out value="${communityPostFormBodyValue}"/></textarea>
                </div>

                <div class="community-post-form-toolbar" aria-label="${communityPostFormToolbarAria}">
                    <button type="button"><spring:message code="communities.postForm.toolbar.bold"/></button>
                    <button type="button"><spring:message code="communities.postForm.toolbar.italic"/></button>
                    <button type="button"><spring:message code="communities.postForm.toolbar.quote"/></button>
                    <button type="button"><spring:message code="communities.postForm.toolbar.link"/></button>
                    <button type="button"><spring:message code="communities.postForm.toolbar.photo"/></button>
                    <button type="button"><spring:message code="communities.postForm.toolbar.review"/></button>
                </div>
            </div>

            <div class="community-post-form-footer">
                <div class="community-post-form-flair">
                    <span><spring:message code="communities.postForm.flair.label"/></span>
                    <span class="community-post-form-flair-chip"><spring:message code="communities.postForm.flair.selected"/></span>
                    <button type="button" class="community-post-form-flair-add"><spring:message code="communities.postForm.flair.add"/></button>
                </div>

                <div class="community-post-form-actions">
                    <p><spring:message code="communities.postForm.draftStatus"/></p>
                    <div>
                        <button type="button" class="btn-secondary"><spring:message code="communities.postForm.saveDraft"/></button>
                        <button type="submit" class="btn-primary"><spring:message code="communities.postForm.submit"/></button>
                    </div>
                </div>
            </div>
        </form>
    </main>
    <pa:footer/>
</body>
</html>
