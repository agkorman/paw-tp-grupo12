<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="users.search.title" styles="/css/users-search.css"/>
<body>
    <pa:nav activePage="users"/>
    <spring:message var="searchPlaceholder" code="users.search.placeholder"/>
    <spring:message var="searchAria" code="users.search.aria"/>
    <spring:message var="searchAction" code="common.action.search"/>
    <spring:message var="paginationAria" code="users.search.pagination.aria"/>

    <main class="users-search-page">
        <header class="users-search-header">
            <h1 class="users-search-title"><spring:message code="users.search.heading"/></h1>
            <form class="users-search-form" method="get" action="<c:url value='/users/search'/>" role="search">
                <label class="users-search-field" for="users-search-input">
                    <span class="users-search-icon" aria-hidden="true">
                        <pa:icon name="search" size="22"/>
                    </span>
                    <input id="users-search-input"
                           class="users-search-input"
                           type="search"
                           name="q"
                           value="<c:out value='${query}'/>"
                           placeholder="${searchPlaceholder}"
                           autocomplete="off"
                           aria-label="${searchAria}">
                </label>
                <button type="submit" class="users-search-submit"><c:out value="${searchAction}"/></button>
            </form>
        </header>

        <section class="users-search-results" aria-live="polite">
            <c:choose>
                <c:when test="${empty query}">
                    <p class="users-search-hint"><spring:message code="users.search.hint"/></p>
                </c:when>
                <c:when test="${empty results}">
                    <p class="users-search-empty">
                        <spring:message code="users.search.empty" arguments="${fn:escapeXml(query)}"/>
                    </p>
                </c:when>
                <c:otherwise>
                    <p class="users-search-count">
                        <spring:message code="users.search.count" arguments="${totalItems}"/>
                    </p>
                    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
                    <spring:message var="followLabel" code="common.label.follow"/>
                    <spring:message var="followingLabel" code="common.label.following"/>
                    <ul class="users-search-list">
                        <c:forEach items="${results}" var="u">
                            <li class="users-search-item">
                                <c:url var="profileUrl" value="/profiles/${u.id}"/>
                                <a class="users-search-card" href="${profileUrl}">
                                    <span class="users-search-avatar" aria-hidden="true">
                                        <c:out value="${fn:toUpperCase(fn:substring(u.username, 0, 1))}"/>
                                    </span>
                                    <span class="users-search-meta">
                                        <span class="users-search-username"><c:out value="${u.username}"/></span>
                                        <span class="users-search-email"><c:out value="${u.email}"/></span>
                                    </span>
                                </a>
                                <c:if test="${empty currentUserId or currentUserId ne u.id}">
                                    <c:set var="isFollowing" value="${followedIds.contains(u.id)}"/>
                                    <c:choose>
                                        <c:when test="${authenticated}">
                                            <form class="users-search-follow-form"
                                                  method="post"
                                                  action="<c:url value='/users/${u.id}/follow'/>">
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                <c:if test="${not empty query}">
                                                    <input type="hidden" name="q" value="<c:out value='${query}'/>">
                                                </c:if>
                                                <input type="hidden" name="page" value="${currentPage}">
                                                <button type="submit"
                                                        class="users-search-follow-button ${isFollowing ? 'is-following' : ''}"
                                                        aria-pressed="${isFollowing}">
                                                    <c:choose>
                                                        <c:when test="${isFollowing}"><c:out value="${followingLabel}"/></c:when>
                                                        <c:otherwise><c:out value="${followLabel}"/></c:otherwise>
                                                    </c:choose>
                                                </button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <c:url var="followLoginUrl" value="/login">
                                                <c:param name="redirect" value="/users/search"/>
                                            </c:url>
                                            <a href="${followLoginUrl}" class="users-search-follow-button">
                                                <c:out value="${followLabel}"/>
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
        </section>

        <c:if test="${totalPages > 1}">
            <c:url var="searchBaseUrl" value="/users/search"/>
            <c:set var="paginationParams" value="${null}"/>
            <jsp:useBean id="paginationParams" class="java.util.LinkedHashMap" scope="page"/>
            <c:set target="${paginationParams}" property="q" value="${query}"/>
            <pa:pagination currentPage="${currentPage}"
                           totalPages="${totalPages}"
                           baseUrl="${searchBaseUrl}"
                           extraParams="${paginationParams}"
                           ariaLabel="${paginationAria}"/>
        </c:if>
    </main>

    <pa:footer/>
</body>
</html>
