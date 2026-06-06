<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="closeLabel" code="users.panel.close"/>
<spring:message var="searchPlaceholder" code="users.search.placeholder"/>
<c:url var="searchUrl" value="/users/search"/>

<div id="usersPanelOverlay" class="users-panel-overlay" data-close-users-panel></div>

<aside id="usersSearchPanel"
       class="users-panel"
       hidden
       role="dialog"
       aria-modal="true"
       aria-labelledby="usersPanelTitle"
       data-auto-open="${not empty userSearchQuery}">

    <div class="users-panel-inner">

        <div class="users-panel-header">
            <h2 id="usersPanelTitle" class="users-panel-title"><spring:message code="users.panel.title"/></h2>
            <button type="button" class="users-panel-close" data-close-users-panel
                    aria-label="${fn:escapeXml(closeLabel)}">
                <pa:icon name="close" size="18"/>
            </button>
        </div>

        <form class="users-panel-search-wrap" method="get"
              action="${fn:escapeXml(searchUrl)}" novalidate="novalidate">
            <label class="users-panel-search-field" for="usersPanelInput">
                <span class="users-panel-search-icon" aria-hidden="true">
                    <pa:icon name="search" size="16"/>
                </span>
                <input id="usersPanelInput"
                       class="users-panel-search-input"
                       type="search"
                       name="q"
                       value="${fn:escapeXml(userSearchQuery)}"
                       autocomplete="off"
                       placeholder="${fn:escapeXml(searchPlaceholder)}"
                       aria-label="${fn:escapeXml(searchPlaceholder)}">
            </label>
        </form>

        <div class="users-panel-results" aria-live="polite">
            <c:choose>
                <c:when test="${empty userSearchQuery}">
                    <p class="users-panel-hint"><spring:message code="users.search.hint"/></p>
                </c:when>
                <c:when test="${empty userSearchResults}">
                    <p class="users-panel-empty">
                        <spring:message code="users.search.empty" arguments="${fn:escapeXml(userSearchQuery)}"/>
                    </p>
                </c:when>
                <c:otherwise>
                    <ul class="users-search-list">
                        <c:forEach items="${userSearchResults}" var="u">
                            <li class="users-search-item">
                                <c:url var="profileUrl" value="/users/${u.id}"/>
                                <a class="users-search-card" href="${profileUrl}">
                                    <span class="users-search-avatar" aria-hidden="true">
                                        <c:out value="${fn:toUpperCase(fn:substring(u.username, 0, 1))}"/>
                                    </span>
                                    <span class="users-search-meta">
                                        <span class="users-search-username"><c:out value="${u.username}"/></span>
                                        <span class="users-search-email"><c:out value="${u.email}"/></span>
                                    </span>
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>

    </div>
</aside>
