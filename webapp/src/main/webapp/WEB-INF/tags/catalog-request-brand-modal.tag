<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:url var="requestBrandUrl" value="/brand-requests"/>
<spring:message var="brandNamePlaceholder" code="request.brand.placeholder.name"/>
<spring:message var="brandCommentsPlaceholder" code="request.brand.placeholder.comments"/>

<pa:catalog-request-modal
        id="requestBrandModal"
        formId="requestBrandForm"
        action="${requestBrandUrl}"
        titleId="requestBrandModalTitle"
        kickerCode="request.brand.kicker"
        titleCode="request.brand.title"
        descriptionCode="request.brand.description"
        nameLabelCode="request.brand.name"
        commentsLabelCode="request.brand.comments"
        nameInputId="requestBrandName"
        commentsInputId="requestBrandComments"
        namePlaceholder="${brandNamePlaceholder}"
        commentsPlaceholder="${brandCommentsPlaceholder}"/>
