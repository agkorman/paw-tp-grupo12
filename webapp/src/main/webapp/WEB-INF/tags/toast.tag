<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="message" required="true" %>
<%@ attribute name="state" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="stateClass" value="toast-${state}" />

<div class="toast ${stateClass}">
    <div class="toast-header">
        <strong><c:out value="${title}" /></strong>
        <button type="button" class="toast-close" onclick="this.parentElement.parentElement.style.display='none';">&times;</button>
    </div>
    <div class="toast-body">
        <c:out value="${message}" />
    </div>
</div>
