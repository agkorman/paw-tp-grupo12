<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Encontrá tu auto | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/cars.css'/>">
</head>
<body>
    <pa:nav activePage="recommend"/>

    <main class="recommend-page">
        <section class="recommend-header">
            <p class="recommend-kicker">Find me a car</p>
            <h1>Encontrá autos según lo que otros conductores destacan</h1>
            <p>Respondé unas preguntas rápidas y priorizamos los modelos con reseñas que coinciden con tu uso.</p>
        </section>

        <c:url var="recommendResultsUrl" value="/cars/recommend/results"/>
        <form:form method="get" action="${recommendResultsUrl}" modelAttribute="recommendationForm" cssClass="recommend-form">
            <form:errors path="*" cssClass="recommend-form-errors" element="div"/>

            <div class="recommend-question-grid">
                <c:forEach var="question" items="${questions}">
                    <fieldset class="recommend-question">
                        <legend><spring:message code="${question.labelKey}"/></legend>
                        <div class="recommend-answer-row">
                            <c:forEach var="answer" items="${question.answers}">
                                <label class="recommend-answer">
                                    <form:radiobutton path="${question.id}" value="${answer.id}"/>
                                    <span><spring:message code="${answer.labelKey}"/></span>
                                </label>
                            </c:forEach>
                        </div>
                        <form:errors path="${question.id}" cssClass="recommend-field-error" element="p"/>
                    </fieldset>
                </c:forEach>
            </div>

            <section class="recommend-prefilters" aria-label="Filtros">
                <label class="recommend-select-field">
                    <span>Carrocería</span>
                    <form:select path="bodyType">
                        <form:option value="">Cualquiera</form:option>
                        <c:forEach var="bodyType" items="${bodyTypes}">
                            <form:option value="${bodyType.name}"><c:out value="${bodyType.name}"/></form:option>
                        </c:forEach>
                    </form:select>
                    <form:errors path="bodyType" cssClass="recommend-field-error" element="p"/>
                </label>

                <label class="recommend-select-field">
                    <span>Motorización</span>
                    <form:select path="fuelType">
                        <form:option value="">Cualquiera</form:option>
                        <form:option value="combustion">Combustión</form:option>
                        <form:option value="hybrid">Híbrido</form:option>
                        <form:option value="electric">Eléctrico</form:option>
                    </form:select>
                    <form:errors path="fuelType" cssClass="recommend-field-error" element="p"/>
                </label>
            </section>

            <div class="recommend-actions">
                <button type="submit" class="btn-primary">Ver recomendaciones</button>
                <a href="<c:url value='/cars'/>" class="btn-secondary">Volver al catálogo</a>
            </div>
        </form:form>
    </main>
</body>
</html>
