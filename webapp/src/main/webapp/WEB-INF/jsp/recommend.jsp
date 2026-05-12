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
        <form:form method="get" action="${recommendResultsUrl}" modelAttribute="recommendationForm" cssClass="wizard-form" id="recommend-wizard" novalidate="novalidate">
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
                    </div>
                    <div class="wizard-actions">
                        <button type="button" class="btn-primary wizard-start" data-wizard-action="next"><spring:message code="recommend.intro.start"/></button>
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
                            <label class="recommend-select-field">
                                <span><spring:message code="recommend.filters.bodyType"/></span>
                                <form:select path="bodyType">
                                    <form:option value=""><spring:message code="recommend.filters.any"/></form:option>
                                    <c:forEach var="bodyType" items="${bodyTypes}">
                                        <form:option value="${bodyType.name}"><c:out value="${bodyType.name}"/></form:option>
                                    </c:forEach>
                                </form:select>
                                <form:errors path="bodyType" cssClass="recommend-field-error" element="p"/>
                            </label>

                            <label class="recommend-select-field">
                                <span><spring:message code="recommend.filters.fuelType"/></span>
                                <form:select path="fuelType">
                                    <form:option value=""><spring:message code="recommend.filters.any"/></form:option>
                                    <form:option value="combustion"><spring:message code="recommend.filters.fuel.combustion"/></form:option>
                                    <form:option value="hybrid"><spring:message code="recommend.filters.fuel.hybrid"/></form:option>
                                    <form:option value="electric"><spring:message code="recommend.filters.fuel.electric"/></form:option>
                                </form:select>
                                <form:errors path="fuelType" cssClass="recommend-field-error" element="p"/>
                            </label>
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

    <pa:script src="/js/recommendations/recommend-wizard.js"/>
    <pa:footer/>
</body>
</html>
