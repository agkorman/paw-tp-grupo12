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
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<noscript>
    <div class="noscript-overlay">
        <div class="noscript-box">
            <strong><spring:message code="nav.noScript.title"/></strong>
            <p><spring:message code="nav.noScript.body"/></p>
        </div>
    </div>
</noscript>
<nav>
    <a href="<c:url value='/'/>" class="nav-brand"><spring:message code="app.name"/></a>
    <ul class="nav-links">
        <li><a href="<c:url value='/cars'/>" class="${activePage eq 'reviews' ? 'active' : ''}"><spring:message code="nav.catalog"/></a></li>
        <li><a href="<c:url value='/activity'/>" class="${activePage eq 'activity' ? 'active' : ''}"><spring:message code="nav.activity"/></a></li>
        <li><a href="<c:url value='/communities'/>" class="${activePage eq 'communities' ? 'active' : ''}"><spring:message code="nav.communities"/></a></li>
        <sec:authorize access="hasRole('ADMIN')">
            <li><a href="<c:url value='/admin'/>" class="${activePage eq 'admin' ? 'active' : ''}"><spring:message code="nav.admin"/></a></li>
        </sec:authorize>
    </ul>
    <div class="nav-right">
        <c:if test="${not empty searchAction}">
            <form class="nav-search-form search-box" method="get" action="${fn:escapeXml(searchAction)}" novalidate="novalidate">
                <c:if test="${not empty searchBrand}">
                    <input type="hidden" name="brand" value="${fn:escapeXml(searchBrand)}">
                </c:if>
                <c:if test="${not empty searchBodyType}">
                    <input type="hidden" name="bodyType" value="${fn:escapeXml(searchBodyType)}">
                </c:if>
                <pa:icon name="search" size="16"/>
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
        <sec:authorize access="isAnonymous()">
            <span class="nav-profile">
                <a class="nav-profile-text nav-auth-link" href="<c:url value='/login'/>"><spring:message code="common.action.login"/></a>
                <span class="avatar" aria-hidden="true">
                    <pa:icon name="user-avatar" size="18"/>
                </span>
            </span>
        </sec:authorize>
        <sec:authorize access="isAuthenticated()">
            <sec:authentication property="principal.displayName" var="displayName"/>
            <span class="nav-profile">
                <spring:message var="profileLabel" code="nav.profile"/>
                <a class="nav-user nav-profile-text ${activePage eq 'profile' ? 'active' : ''}" href="<c:url value='/profile'/>" aria-label="${fn:escapeXml(profileLabel)}">
                    <c:out value="${displayName}"/>
                </a>
                <a class="avatar nav-avatar-link" href="<c:url value='/profile'/>" aria-label="${fn:escapeXml(profileLabel)}">
                    <pa:icon name="user-avatar" size="18"/>
                </a>
            </span>
        </sec:authorize>
    </div>
</nav>
