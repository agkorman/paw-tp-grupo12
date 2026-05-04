<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="src" required="true" type="java.lang.String" %>
<%@ attribute name="defer" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="scriptSrc" value="${src}">
    <c:param name="v" value="1"/>
</c:url>
<script src="${scriptSrc}"<c:if test="${defer}"> defer</c:if>></script>
