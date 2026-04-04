<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activePage" required="false" %>
<%@ attribute name="actionText" required="false" %>
<%@ attribute name="actionId" required="false" %>
<%@ attribute name="actionDialogId" required="false" %>
<%@ attribute name="searchAction" required="false" %>
<%@ attribute name="searchValue" required="false" %>
<%@ attribute name="searchPlaceholder" required="false" %>
<%@ attribute name="searchBrand" required="false" %>
<%@ attribute name="searchBodyType" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<nav>
    <a href="<c:url value='/'/>" class="nav-brand">La Posta Autos</a>
    <ul class="nav-links">
        <li><a href="<c:url value='/'/>" class="${activePage eq 'explore' ? 'active' : ''}">Explorar</a></li>
        <li><a href="<c:url value='/cars'/>" class="${activePage eq 'reviews' ? 'active' : ''}">Reseñas</a></li>
    </ul>
    <div class="nav-right">
        <c:if test="${not empty searchAction}">
            <form class="nav-search-form search-box" method="get" action="${fn:escapeXml(searchAction)}">
                <c:if test="${not empty searchBrand}">
                    <input type="hidden" name="brand" value="${fn:escapeXml(searchBrand)}">
                </c:if>
                <c:if test="${not empty searchBodyType}">
                    <input type="hidden" name="bodyType" value="${fn:escapeXml(searchBodyType)}">
                </c:if>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" aria-hidden="true">
                    <circle cx="11" cy="11" r="7"></circle>
                    <path d="m20 20-3.5-3.5"></path>
                </svg>
                <input
                        type="search"
                        name="q"
                        value="${fn:escapeXml(searchValue)}"
                        placeholder="${fn:escapeXml(searchPlaceholder)}"
                        aria-label="${fn:escapeXml(searchPlaceholder)}">
            </form>
        </c:if>
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
