<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="recommend.title" styles="/css/cars.css"/>
<body>
    <pa:nav/>

    <main class="recommend-page wizard-page">
        <c:url var="recommendResultsUrl" value="/cars/recommend/results"/>
        <spring:message code="recommend.question.index" var="recommendQuestionIndexTpl" arguments="__PAW_0__,__PAW_1__"/>
        <spring:message var="jsRecommendRequiredAnswer" code="js.recommend.required.answer"/>
        <form:form method="get" action="${recommendResultsUrl}" modelAttribute="recommendationForm" cssClass="wizard-form" id="recommend-wizard" enctype="multipart/form-data" novalidate="novalidate"
                   data-msg-required-answer="${fn:escapeXml(jsRecommendRequiredAnswer)}">
            <div class="wizard-progress" aria-hidden="true" data-recommend-question-index-template="${fn:escapeXml(recommendQuestionIndexTpl)}">
                <div class="wizard-progress-bar"></div>
            </div>
            <p class="wizard-progress-label" aria-live="polite"></p>

            <form:errors path="*" cssClass="recommend-form-errors" element="div"/>

            <spring:message var="prevLabel" code="common.action.back"/>
            <spring:message var="nextLabel" code="common.action.next"/>
            <spring:message var="submitLabel" code="recommend.submit"/>

            <div class="wizard-steps">
                <section class="wizard-step wizard-step--intro" data-step-type="intro">
                    <div class="wizard-step-body">
                        <p class="wizard-kicker"><spring:message code="recommend.intro.kicker"/></p>
                        <h1 class="wizard-question"><spring:message code="recommend.intro.heading"/></h1>
                        <p class="wizard-help"><spring:message code="recommend.intro.help"/></p>
                        <div class="wizard-intro-actions">
                            <button type="button" class="btn-primary wizard-start" data-wizard-action="next"><spring:message code="recommend.intro.start"/></button>
                        </div>
                    </div>
                </section>

                <c:forEach var="question" items="${questions}" varStatus="status">
                    <c:set var="questionLabelId" value="wizard-question-${question.id}"/>
                    <fieldset class="wizard-step recommend-question"
                              data-step-type="question"
                              data-question-id="${question.id}"
                              aria-labelledby="${questionLabelId}">
                        <div class="wizard-step-body">
                            <h2 id="${questionLabelId}" class="wizard-question"><spring:message code="${question.labelKey}"/></h2>
                            <div class="wizard-answer-stack">
                                <c:forEach var="answer" items="${question.answers}">
                                    <label class="wizard-answer">
                                        <form:radiobutton path="${question.id}" value="${answer.id}"/>
                                        <span class="wizard-answer-text"><spring:message code="${answer.labelKey}"/></span>
                                    </label>
                                </c:forEach>
                            </div>
                            <form:errors path="${question.id}" cssClass="recommend-field-error" element="p"/>
                        </div>
                        <div class="wizard-actions">
                            <button type="button" class="btn-secondary wizard-prev" data-wizard-action="prev"><c:out value="${prevLabel}"/></button>
                            <button type="button" class="btn-primary wizard-next" data-wizard-action="next"><c:out value="${nextLabel}"/></button>
                        </div>
                    </fieldset>
                </c:forEach>

                <section class="wizard-step wizard-step--filters" data-step-type="filters">
                    <div class="wizard-step-body">
                        <p class="wizard-kicker"><spring:message code="recommend.filters.step"/></p>
                        <h2 class="wizard-question"><spring:message code="recommend.filters.title"/></h2>
                        <p class="wizard-help"><spring:message code="recommend.filters.help"/></p>
                        <div class="wizard-filters">
                            <div class="recommend-select-field">
                                <span><spring:message code="recommend.filters.bodyType"/></span>
                                <div class="cars-toolbar-field">
                                    <span class="cars-toolbar-field-ui" aria-hidden="true">
                                        <span class="cars-toolbar-field-copy">
                                            <span class="cars-toolbar-value" data-toolbar-select-value="bodyType">
                                                <c:choose>
                                                    <c:when test="${not empty recommendForm.bodyType}"><c:out value="${recommendForm.bodyType}"/></c:when>
                                                    <c:otherwise><spring:message code="recommend.filters.any"/></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </span>
                                        <span class="cars-toolbar-chevron">
                                            <pa:icon name="chevron-down" size="12"/>
                                        </span>
                                    </span>
                                    <form:select path="bodyType" cssClass="cars-toolbar-select cars-toolbar-select-overlay">
                                        <form:option value=""><spring:message code="recommend.filters.any"/></form:option>
                                        <c:forEach var="bodyType" items="${bodyTypes}">
                                            <form:option value="${bodyType.name}"><c:out value="${bodyType.name}"/></form:option>
                                        </c:forEach>
                                    </form:select>
                                </div>
                                <form:errors path="bodyType" cssClass="recommend-field-error" element="p"/>
                            </div>

                            <div class="recommend-select-field">
                                <span><spring:message code="recommend.filters.fuelType"/></span>
                                <div class="cars-toolbar-field">
                                    <span class="cars-toolbar-field-ui" aria-hidden="true">
                                        <span class="cars-toolbar-field-copy">
                                            <span class="cars-toolbar-value" data-toolbar-select-value="fuelType">
                                                <c:choose>
                                                    <c:when test="${recommendForm.fuelType eq 'combustion'}"><spring:message code="recommend.filters.fuel.combustion"/></c:when>
                                                    <c:when test="${recommendForm.fuelType eq 'hybrid'}"><spring:message code="recommend.filters.fuel.hybrid"/></c:when>
                                                    <c:when test="${recommendForm.fuelType eq 'electric'}"><spring:message code="recommend.filters.fuel.electric"/></c:when>
                                                    <c:otherwise><spring:message code="recommend.filters.any"/></c:otherwise>
                                                </c:choose>
                                            </span>
                                        </span>
                                        <span class="cars-toolbar-chevron">
                                            <pa:icon name="chevron-down" size="12"/>
                                        </span>
                                    </span>
                                    <form:select path="fuelType" cssClass="cars-toolbar-select cars-toolbar-select-overlay">
                                        <form:option value=""><spring:message code="recommend.filters.any"/></form:option>
                                        <form:option value="combustion"><spring:message code="recommend.filters.fuel.combustion"/></form:option>
                                        <form:option value="hybrid"><spring:message code="recommend.filters.fuel.hybrid"/></form:option>
                                        <form:option value="electric"><spring:message code="recommend.filters.fuel.electric"/></form:option>
                                    </form:select>
                                </div>
                                <form:errors path="fuelType" cssClass="recommend-field-error" element="p"/>
                            </div>
                        </div>
                    </div>
                    <div class="wizard-actions">
                        <button type="button" class="btn-secondary wizard-prev" data-wizard-action="prev"><c:out value="${prevLabel}"/></button>
                        <button type="submit" class="btn-primary"><c:out value="${submitLabel}"/></button>
                    </div>
                </section>
            </div>

            <div class="wizard-fallback-actions">
                <button type="submit" class="btn-primary"><c:out value="${submitLabel}"/></button>
                <a href="<c:url value='/cars'/>" class="btn-secondary"><spring:message code="recommend.backToCatalog"/></a>
            </div>
        </form:form>
    </main>

    <pa:script src="/js/cars/cars-toolbar.js"/>
    <pa:script src="/js/recommendations/recommend-wizard.js"/>
    <pa:footer/>
</body>
</html>
