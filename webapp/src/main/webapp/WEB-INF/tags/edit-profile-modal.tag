<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="profile" required="true" type="ar.edu.itba.paw.webapp.controller.ProfileController.ProfileData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="closeModalLabel" code="common.action.close"/>

<div id="editProfileModal" class="profile-modal" hidden>
    <div class="profile-modal-overlay" data-close-profile-modal></div>
    <section class="profile-modal-dialog profile-edit-dialog" role="dialog" aria-modal="true" aria-labelledby="editProfileTitle">
        <header class="profile-modal-header">
            <h2 id="editProfileTitle"><spring:message code="profile.edit.title"/></h2>
            <button type="button" class="profile-modal-close" data-close-profile-modal aria-label="${closeModalLabel}">
                <pa:icon name="close" size="20"/>
            </button>
        </header>

        <form class="profile-edit-form" method="post" action="#">
            <div class="profile-edit-fields">
                <label class="profile-edit-field" for="profileNameInput">
                    <span><spring:message code="profile.edit.username"/></span>
                    <input id="profileNameInput" name="displayName" type="text" value="${fn:escapeXml(profile.name)}" maxlength="80">
                </label>

                <div class="profile-edit-field">
                    <span>Email</span>
                    <div class="profile-readonly-value" aria-label="Email">
                        <c:out value="${profile.email}"/>
                    </div>
                </div>
            </div>

            <div class="profile-modal-actions">
                <button type="button" class="btn-secondary" data-close-profile-modal><spring:message code="common.action.cancel"/></button>
                <button type="button" class="btn-primary" data-close-profile-modal><spring:message code="common.action.save"/></button>
            </div>
        </form>

        <c:url var="logoutUrl" value="/logout"/>
        <form class="profile-logout-form" method="post" action="${logoutUrl}">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <button type="submit" class="btn-secondary profile-logout-button"><spring:message code="profile.edit.logout"/></button>
        </form>
    </section>
</div>
