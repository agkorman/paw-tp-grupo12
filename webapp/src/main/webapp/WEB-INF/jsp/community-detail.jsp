<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head title="${pageTitle}" styles="/css/community-detail.css|/css/communities-responsive.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
    <c:url var="communitiesLoginUrl" value="/login">
        <c:param name="redirect" value="/communities/${communityDetail.community.slug}"/>
        <c:param name="intent" value="communities-join"/>
    </c:url>
    <c:url var="communitySubmitUrl" value="/communities/${communityDetail.community.slug}/submit"/>
    <c:url var="communityJoinUrl" value="/communities/${communityDetail.community.slug}/join"/>
    <spring:message var="communityStatsAria" code="communities.sidebar.stats.aria"/>
    <c:set var="communityJoinButtonClass" value="btn-primary community-banner-join-btn"/>
    <c:if test="${communityDetail.joined}">
        <c:set var="communityJoinButtonClass" value="btn-primary community-banner-join-btn is-joined"/>
    </c:if>

    <main class="communities-page">
        <section class="community-hero">
            <div class="community-banner">
                <div class="community-banner-copy">
                    <p class="community-banner-kicker"><spring:message code="communities.hero.kicker"/></p>
                    <div class="community-banner-heading">
                        <h1 class="community-banner-title"><c:out value="${communityDetail.community.name}"/></h1>
                        <div class="community-banner-actions">
                            <c:if test="${authenticated}">
                                <form action="${fn:escapeXml(communityJoinUrl)}" method="post" class="community-banner-join-form">
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                    <button type="submit"
                                            class="${communityJoinButtonClass}"
                                            aria-pressed="${communityDetail.joined}">
                                        <c:choose>
                                            <c:when test="${communityDetail.joined}">
                                                <spring:message code="communities.hero.primaryAction.joined"/>
                                            </c:when>
                                            <c:otherwise>
                                                <spring:message code="communities.hero.primaryAction.authenticated"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </button>
                                </form>
                            </c:if>
                            <c:if test="${not authenticated}">
                                <a class="btn-primary community-banner-join-btn" href="${fn:escapeXml(communitiesLoginUrl)}">
                                    <spring:message code="communities.hero.primaryAction.anonymous"/>
                                </a>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <div class="community-layout">
            <section class="community-feed-panel" aria-labelledby="communityFeedTitle">
                <div class="community-feed-header">
                    <div class="community-feed-heading">
                        <p class="community-section-kicker"><spring:message code="communities.feed.kicker"/></p>
                        <h2 id="communityFeedTitle" class="community-section-title"><spring:message code="communities.feed.title"/></h2>
                    </div>
                    <div class="community-feed-header-actions">
                        <c:choose>
                            <c:when test="${authenticated}">
                                <a class="btn-primary community-create-post-btn" href="${fn:escapeXml(communitySubmitUrl)}">
                                    <spring:message code="communities.detail.createPost"/>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a class="btn-primary community-create-post-btn" href="${fn:escapeXml(communitiesLoginUrl)}">
                                    <spring:message code="communities.hero.primaryAction.anonymous"/>
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="community-post-list">
                    <c:forEach var="postCard" items="${postCards}">
                        <pa:community-post-card
                                href="${postCard.href}"
                                author="${postCard.author}"
                                timeText="${postCard.timeText}"
                                title="${postCard.title}"
                                body="${postCard.body}"
                                helpfulCount="${postCard.helpfulCount}"
                                commentCount="${postCard.commentCount}"/>
                    </c:forEach>
                </div>
            </section>

            <aside class="community-sidebar">
                <section class="community-sidebar-card">
                    <p class="community-sidebar-eyebrow"><spring:message code="communities.sidebar.about.eyebrow"/></p>
                    <p class="community-sidebar-copy"><c:out value="${communityDetail.community.description}"/></p>
                    <c:if test="${not empty communityDetail.topics}">
                        <div class="community-sidebar-topics">
                            <c:forEach var="topic" items="${communityDetail.topics}">
                                <span class="community-sidebar-topic">#<c:out value="${topic.code}"/></span>
                            </c:forEach>
                        </div>
                    </c:if>
                </section>

                <section class="community-sidebar-card community-sidebar-stats" aria-label="${fn:escapeXml(communityStatsAria)}">
                    <div class="community-stat">
                        <strong><c:out value="${communityDetail.memberCount}"/></strong>
                        <span><spring:message code="communities.sidebar.stats.members"/></span>
                    </div>
                    <div class="community-stat">
                        <strong><c:out value="${communityDetail.weeklyPostCount}"/></strong>
                        <span><spring:message code="communities.sidebar.stats.posts"/></span>
                    </div>
                </section>
            </aside>
        </div>
    </main>

    <pa:footer/>
</body>
</html>
