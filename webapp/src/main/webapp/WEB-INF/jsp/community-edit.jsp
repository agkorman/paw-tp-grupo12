<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.edit.title" styles="/css/community-create.css|/css/communities-responsive.css|/css/review-tags.css|/css/profile-modal.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="communityDetailUrl" value="/communities/${communitySlug}"/>
    <c:url var="communityEditUrl" value="/communities/${communitySlug}/edit"/>
    <c:url var="communityDeleteUrl" value="/communities/${communitySlug}/delete"/>
    <spring:message var="jsMsgRequiredGeneric" code="js.form.required.generic"/>
    <spring:message var="jsMsgRequiredCommunityName" code="js.community.required.name"/>
    <spring:message var="jsMsgRequiredCommunityDescription" code="js.community.required.description"/>
    <spring:message var="jsMsgRequiredCommunityTopics" code="js.community.required.topics"/>
    <main class="community-create-page">
        <section class="community-create-intro">
            <p class="community-create-kicker"><spring:message code="communities.edit.kicker"/></p>
            <h1><spring:message code="communities.edit.heading"/></h1>
            <p><spring:message code="communities.edit.description"/></p>
        </section>

        <form:form id="communityCreateForm"
                   cssClass="community-create-layout community-create-form"
                   modelAttribute="communityForm"
                   method="post"
                   action="${fn:escapeXml(communityEditUrl)}"
                   data-submit-lock="true"
                   data-msg-required-generic="${fn:escapeXml(jsMsgRequiredGeneric)}"
                   data-msg-required-community-create-name="${fn:escapeXml(jsMsgRequiredCommunityName)}"
                   data-msg-required-community-create-description="${fn:escapeXml(jsMsgRequiredCommunityDescription)}"
                   data-msg-topics-required="${fn:escapeXml(jsMsgRequiredCommunityTopics)}"
                   novalidate="novalidate">
            <form:errors cssClass="alert alert-error" element="div"/>
            <section class="community-create-panel">
                <div class="community-create-section">
                    <div class="community-create-section-head">
                        <h2><spring:message code="communities.create.topic.title"/></h2>
                        <p><spring:message code="communities.create.topic.description"/></p>
                    </div>
                    <pa:community-topic-chips topics="${communityTopics}" selectedTopicIds="${communityForm.selectedTopicIds}"/>
                    <form:errors path="selectedTopicIds" cssClass="form-error" element="span"/>
                </div>

                <div class="community-create-section">
                    <div class="community-create-fields">
                        <div class="community-create-field">
                            <label for="communityCreateName"><spring:message code="communities.create.field.name"/></label>
                            <form:input id="communityCreateName"
                                        path="name"
                                        type="text"
                                        maxlength="21"
                                        required="required"
                                        aria-describedby="communityCreateNameHelp"/>
                            <div class="community-create-field-meta">
                                <span id="communityCreateNameHelp"><spring:message code="communities.create.field.name.help"/></span>
                            </div>
                            <form:errors path="name" cssClass="form-error" element="span"/>
                        </div>

                        <div class="community-create-field">
                            <label for="communityCreateDescription"><spring:message code="communities.create.field.description"/></label>
                            <form:textarea id="communityCreateDescription"
                                           path="description"
                                           rows="6"
                                           maxlength="180"
                                           required="required"/>
                            <div class="community-create-field-meta">
                                <span><spring:message code="communities.create.field.description.help"/></span>
                            </div>
                            <form:errors path="description" cssClass="form-error" element="span"/>
                        </div>
                    </div>
                </div>
            </section>

            <div class="community-create-actions">
                <a class="btn-secondary" href="${fn:escapeXml(communityDetailUrl)}"><spring:message code="common.action.cancel"/></a>
                <button type="submit" class="btn-primary"><spring:message code="communities.edit.submit"/></button>
            </div>
        </form:form>

        <c:if test="${viewerIsCreator}">
            <section class="community-edit-danger">
                <div class="community-edit-danger-copy">
                    <h2><spring:message code="communities.edit.danger.title"/></h2>
                    <p><spring:message code="communities.edit.danger.description"/></p>
                </div>
                <form method="post" action="${fn:escapeXml(communityDeleteUrl)}" data-confirm-modal="deleteCommunityConfirmModal">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <button type="submit" class="btn-danger"><spring:message code="communities.edit.danger.delete"/></button>
                </form>
            </section>
        </c:if>
    </main>

    <c:if test="${viewerIsCreator}">
        <pa:confirmation-modal id="deleteCommunityConfirmModal"
                               titleCode="communities.edit.danger.confirm.title"
                               bodyCode="communities.edit.danger.confirm.body"
                               confirmCode="communities.edit.danger.delete"
                               confirmCssClass="btn-primary"/>
    </c:if>

    <pa:script src="/js/communities/community-topic-chips.js" defer="true"/>
    <pa:script src="/js/communities/community-create-form.js" defer="true"/>
    <pa:script src="/js/shared/confirmation-modal.js"/>
    <pa:footer/>
</body>
</html>
