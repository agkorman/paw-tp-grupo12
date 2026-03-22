<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="label" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<button type="button" class="filter-chip">
    <c:out value="${label}"/>
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <polyline points="6 9 12 15 18 9"/>
    </svg>
</button>
