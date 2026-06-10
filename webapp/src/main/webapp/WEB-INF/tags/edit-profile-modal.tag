<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="profile" required="true" type="ar.edu.itba.paw.webapp.controller.UserController.ProfileData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="closeModalLabel" code="common.action.close"/>
<spring:message var="emailLabel" code="common.form.email"/>
<spring:message var="usernameRequiredMessage" code="profile.edit.error.username.required"/>
<spring:message var="usernameMaxMessage" code="profile.edit.error.username.max"/>
<spring:message var="usernamePatternMessage" code="profile.edit.error.username.pattern"/>
<c:url var="profileUpdateUrl" value="/user"/>
<c:set var="resolvedProfileName" value="${empty profileEditUsername ? profile.name : profileEditUsername}"/>

<div id="editProfileModal" class="profile-modal" data-open-on-load="${openEditProfileModal}" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-edit-dialog" role="dialog" aria-modal="true" aria-labelledby="editProfileTitle">
        <header class="profile-modal-header">
            <h2 id="editProfileTitle"><spring:message code="profile.edit.title"/></h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="20"/>
            </button>
        </header>

        <form class="profile-edit-form" method="post" action="${profileUpdateUrl}"
              enctype="multipart/form-data" novalidate="novalidate"
              data-profile-edit-form
              data-msg-required-username="${fn:escapeXml(usernameRequiredMessage)}"
              data-msg-username-max="${fn:escapeXml(usernameMaxMessage)}"
              data-msg-username-pattern="${fn:escapeXml(usernamePatternMessage)}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <div class="profile-edit-fields">
                <label class="profile-edit-field" for="profileNameInput">
                    <span><spring:message code="profile.edit.username"/></span>
                    <input id="profileNameInput" name="displayName" type="text" maxlength="50"
                           required
                           value="${fn:escapeXml(resolvedProfileName)}"
                           class="${not empty profileEditErrorCode ? 'is-invalid' : ''}">
                    <c:if test="${not empty profileEditErrorCode}">
                        <span class="form-error" role="alert"><spring:message code="${profileEditErrorCode}"/></span>
                    </c:if>
                </label>

                <div class="profile-edit-field">
                    <span><c:out value="${emailLabel}"/></span>
                    <div class="profile-readonly-value" aria-label="${fn:escapeXml(emailLabel)}">
                        <c:out value="${profile.email}"/>
                    </div>
                </div>
            </div>

            <div class="profile-modal-actions">
                <button type="button" class="btn-secondary" data-close-profile-modal><spring:message code="common.action.cancel"/></button>
                <button type="submit" class="btn-primary"><spring:message code="common.action.save"/></button>
            </div>
        </form>

    </section>
</div>
