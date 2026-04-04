<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activePage" required="false" %>
<%@ attribute name="actionText" required="false" %>
<%@ attribute name="actionId" required="false" %>
<%@ attribute name="actionDialogId" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<nav>
    <a href="<c:url value='/'/>" class="nav-brand">La Posta Autos</a>
    <ul class="nav-links">
        <li><a href="<c:url value='/'/>" class="${activePage eq 'explore' ? 'active' : ''}">Explore</a></li>
        <li><a href="<c:url value='/cars'/>" class="${activePage eq 'reviews' ? 'active' : ''}">Reviews</a></li>
    </ul>
    <div class="nav-right">
        <c:if test="${not empty actionText}">
            <button
                    type="button"
                    class="btn-secondary nav-action-btn"
                    id="${fn:escapeXml(actionId)}"
                    aria-controls="${fn:escapeXml(actionDialogId)}"
                    aria-haspopup="dialog">
                <c:out value="${actionText}"/>
            </button>
        </c:if>
    </div>
</nav>
