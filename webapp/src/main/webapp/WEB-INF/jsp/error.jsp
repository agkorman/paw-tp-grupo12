<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="errorTitleSuffix" code="error.titleSuffix"/>
<c:set var="errorPageTitle" value="${statusCode} ${errorTitleSuffix}"/>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${errorPageTitle}" styles="/css/landing.css|/css/error.css"/>
<body>

    <pa:nav activePage=""/>

    <main class="error-page">
        <section class="error-hero">
            <span class="error-status"><c:out value="${statusCode}"/></span>
            <h1 class="error-title"><spring:message code="${titleCode}"/></h1>
            <p class="error-text"><spring:message code="${descriptionCode}"/></p>
            <div class="error-actions">
                <a class="btn-primary" href="<c:url value='/'/>"><spring:message code="error.home"/></a>
                <a class="btn-secondary" href="<c:url value='/cars'/>"><spring:message code="error.catalog"/></a>
            </div>
        </section>
    </main>

    <pa:footer/>

</body>
</html>
