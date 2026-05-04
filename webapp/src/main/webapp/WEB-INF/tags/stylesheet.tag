<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="href" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="stylesheetHref" value="${href}">
    <c:param name="v" value="1"/>
</c:url>
<link rel="stylesheet" href="${stylesheetHref}">
