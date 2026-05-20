<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.create.title" styles="/css/communities.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="communitiesHubUrl" value="/communities"/>
    <spring:message var="communityCreateNameValue" code="communities.create.sample.name"/>
    <spring:message var="communityCreateDescriptionValue" code="communities.create.sample.description"/>
    <spring:message var="communityCreatePreviewHandle" code="communities.create.preview.handle"/>
    <spring:message var="communityAvatarMark" code="communities.hero.avatarMark"/>
    <main class="community-create-page">
        <section class="community-create-intro">
            <p class="community-create-kicker"><spring:message code="communities.create.kicker"/></p>
            <h1><spring:message code="communities.create.heading"/></h1>
            <p><spring:message code="communities.create.description"/></p>
        </section>

        <form class="community-create-layout" novalidate="novalidate">
            <section class="community-create-panel">
                <div class="community-create-section">
                    <div class="community-create-section-head">
                        <h2><spring:message code="communities.create.topic.title"/></h2>
                        <p><spring:message code="communities.create.topic.description"/></p>
                    </div>
                    <div class="community-topic-grid">
                        <span class="community-topic-chip is-selected"><spring:message code="communities.create.topic.classics"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.brands"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.jdm"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.electric"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.motorsport"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.offroad"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.repairs"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.reviews"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.buying"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.local"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.photography"/></span>
                        <span class="community-topic-chip"><spring:message code="communities.create.topic.daily"/></span>
                    </div>
                </div>

                <div class="community-create-section community-create-details-grid">
                    <div class="community-create-fields">
                        <div class="community-create-field">
                            <label for="communityCreateName"><spring:message code="communities.create.field.name"/></label>
                            <input id="communityCreateName"
                                   type="text"
                                   maxlength="21"
                                   value="${communityCreateNameValue}"
                                   aria-describedby="communityCreateNameHelp">
                            <div class="community-create-field-meta">
                                <span id="communityCreateNameHelp"><spring:message code="communities.create.field.name.help"/></span>
                                <span>14/21</span>
                            </div>
                        </div>

                        <div class="community-create-field">
                            <label for="communityCreateDescription"><spring:message code="communities.create.field.description"/></label>
                            <textarea id="communityCreateDescription"
                                      rows="6"
                                      maxlength="180"><c:out value="${communityCreateDescriptionValue}"/></textarea>
                            <div class="community-create-field-meta">
                                <span><spring:message code="communities.create.field.description.help"/></span>
                                <span>88/180</span>
                            </div>
                        </div>
                    </div>

                    <aside class="community-create-preview">
                        <p class="community-create-preview-label"><spring:message code="communities.create.preview.label"/></p>
                        <article class="community-create-preview-card">
                            <div class="community-create-preview-banner"></div>
                            <div class="community-create-preview-body">
                                <div class="community-create-preview-avatar"><c:out value="${communityAvatarMark}"/></div>
                                <div>
                                    <h3><c:out value="${communityCreatePreviewHandle}"/></h3>
                                    <p><spring:message code="communities.create.preview.meta"/></p>
                                </div>
                            </div>
                            <p class="community-create-preview-description"><spring:message code="communities.create.preview.description"/></p>
                        </article>
                    </aside>
                </div>
            </section>

            <div class="community-create-actions">
                <a class="btn-secondary" href="${communitiesHubUrl}"><spring:message code="common.action.cancel"/></a>
                <button type="submit" class="btn-primary"><spring:message code="communities.create.submit"/></button>
            </div>
        </form>
    </main>
    <pa:footer/>
</body>
</html>
