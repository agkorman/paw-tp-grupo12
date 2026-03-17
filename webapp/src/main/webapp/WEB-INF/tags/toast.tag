<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="message" required="true" %>
<%@ attribute name="color" required="false" %>
<%@ attribute name="state" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="toastColor" value="${not empty color ? color : (not empty state ? state : 'success')}" />
<c:set var="stateClass" value="toast-${toastColor}" />
<c:set var="toastInfo" value="${not empty info ? info : 'Info'}" />

<div class="toast ${stateClass}">
    <div class="toast-header">
        <strong class="toast-title">
            <c:out value="${title}" />
        </strong>
        <button type="button" class="toast-close" onclick="this.parentElement.parentElement.style.display='none';">&times;</button>
    </div>
    <div class="toast-body">
        <div class="toast-message">
            <c:out value="${message}" />
        </div>
    </div>
</div>