<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="requestBodyTypeUrl" value="/body-type-requests"/>
<spring:message var="bodyTypeNamePlaceholder" code="request.bodyType.placeholder.name"/>
<spring:message var="bodyTypeCommentsPlaceholder" code="request.bodyType.placeholder.comments"/>

<pa:catalog-request-modal
        id="requestBodyTypeModal"
        formId="requestBodyTypeForm"
        action="${requestBodyTypeUrl}"
        titleId="requestBodyTypeModalTitle"
        kickerCode="request.bodyType.kicker"
        titleCode="request.bodyType.title"
        descriptionCode="request.bodyType.description"
        nameLabelCode="request.bodyType.name"
        commentsLabelCode="request.bodyType.comments"
        nameInputId="requestBodyTypeName"
        commentsInputId="requestBodyTypeComments"
        namePlaceholder="${bodyTypeNamePlaceholder}"
        commentsPlaceholder="${bodyTypeCommentsPlaceholder}"/>
