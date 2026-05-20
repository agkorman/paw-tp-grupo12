<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.detail.title" styles="/css/communities.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>
    <c:url var="communitiesLoginUrl" value="/login">
        <c:param name="redirect" value="/communities/classics"/>
        <c:param name="intent" value="communities-join"/>
    </c:url>
    <c:url var="profileUrl" value="/profile"/>
    <c:url var="communitySubmitUrl" value="/communities/classics/submit"/>
    <c:url var="communityPostDetailUrl" value="/communities/classics/posts/falcon-60"/>
    <spring:message var="communityRulesUrlLabel" code="communities.hero.secondaryAction"/>
    <spring:message var="communityGuestName" code="communities.sidebar.flair.guest"/>
    <spring:message var="communityHighlightsAria" code="communities.highlights.aria"/>
    <spring:message var="communityStatsAria" code="communities.sidebar.stats.aria"/>

    <main class="communities-page">
        <section class="community-hero">
            <div class="community-banner">
                <div class="community-banner-copy">
                    <p class="community-banner-kicker"><spring:message code="communities.hero.kicker"/></p>
                    <h1 class="community-banner-title"><spring:message code="communities.hero.title"/></h1>
                    <p class="community-banner-description"><spring:message code="communities.hero.description"/></p>
                </div>
            </div>

            <div class="community-identity">
                <div class="community-avatar" aria-hidden="true">
                    <span class="community-avatar-mark"><spring:message code="communities.hero.avatarMark"/></span>
                </div>
                <div class="community-identity-copy">
                    <p class="community-handle"><spring:message code="communities.hero.handle"/></p>
                    <h2 class="community-name"><spring:message code="communities.hero.name"/></h2>
                    <p class="community-summary"><spring:message code="communities.hero.summary"/></p>
                </div>
                <div class="community-actions">
                    <a class="btn-secondary" href="#communityRules">
                        <c:out value="${communityRulesUrlLabel}"/>
                    </a>
                    <c:choose>
                        <c:when test="${authenticated}">
                            <a class="btn-primary" href="${profileUrl}">
                                <spring:message code="communities.hero.primaryAction.authenticated"/>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a class="btn-primary" href="${communitiesLoginUrl}">
                                <spring:message code="communities.hero.primaryAction.anonymous"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>

        <section class="community-highlights" aria-label="${communityHighlightsAria}">
            <article class="community-highlight-card">
                <p class="community-highlight-label"><spring:message code="communities.highlight.one.label"/></p>
                <h3><spring:message code="communities.highlight.one.title"/></h3>
                <p><spring:message code="communities.highlight.one.body"/></p>
            </article>
            <article class="community-highlight-card">
                <p class="community-highlight-label"><spring:message code="communities.highlight.two.label"/></p>
                <h3><spring:message code="communities.highlight.two.title"/></h3>
                <p><spring:message code="communities.highlight.two.body"/></p>
            </article>
            <article class="community-highlight-card">
                <p class="community-highlight-label"><spring:message code="communities.highlight.three.label"/></p>
                <h3><spring:message code="communities.highlight.three.title"/></h3>
                <p><spring:message code="communities.highlight.three.body"/></p>
            </article>
        </section>

        <div class="community-layout">
            <section class="community-feed-panel" aria-labelledby="communityFeedTitle">
                <div class="community-feed-header">
                    <div>
                        <p class="community-section-kicker"><spring:message code="communities.feed.kicker"/></p>
                        <h2 id="communityFeedTitle" class="community-section-title"><spring:message code="communities.feed.title"/></h2>
                    </div>
                    <div class="community-feed-header-actions">
                        <p class="community-feed-description"><spring:message code="communities.feed.description"/></p>
                        <a class="btn-primary community-create-post-btn" href="${communitySubmitUrl}">
                            <spring:message code="communities.detail.createPost"/>
                        </a>
                    </div>
                </div>

                <div class="community-post-list">
                    <pa:community-post-card
                            authorCode="communities.post.one.author"
                            timeCode="communities.post.one.time"
                            typeCode="communities.post.type.review"
                            typeClass="community-post-type--review"
                            titleCode="communities.post.one.title"
                            bodyCode="communities.post.one.body"
                            helpfulCount="52"
                            commentCount="36"
                            href="${communityPostDetailUrl}"/>
                    <pa:community-post-card
                            authorCode="communities.post.two.author"
                            timeCode="communities.post.two.time"
                            typeCode="communities.post.type.photo"
                            typeClass="community-post-type--photo"
                            titleCode="communities.post.two.title"
                            bodyCode="communities.post.two.body"
                            helpfulCount="142"
                            commentCount="28"
                            href="${communityPostDetailUrl}"/>
                    <pa:community-post-card
                            authorCode="communities.post.three.author"
                            timeCode="communities.post.three.time"
                            typeCode="communities.post.type.question"
                            typeClass="community-post-type--question"
                            titleCode="communities.post.three.title"
                            bodyCode="communities.post.three.body"
                            helpfulCount="31"
                            commentCount="19"
                            href="${communityPostDetailUrl}"/>
                </div>
            </section>

            <aside class="community-sidebar">
                <section class="community-sidebar-card">
                    <p class="community-sidebar-eyebrow"><spring:message code="communities.sidebar.about.eyebrow"/></p>
                    <h2 class="community-sidebar-title"><spring:message code="communities.sidebar.about.title"/></h2>
                    <p class="community-sidebar-copy"><spring:message code="communities.sidebar.about.body"/></p>
                </section>

                <section class="community-sidebar-card community-sidebar-stats" aria-label="${communityStatsAria}">
                    <div class="community-stat">
                        <strong>4.2k</strong>
                        <span><spring:message code="communities.sidebar.stats.visitors"/></span>
                    </div>
                    <div class="community-stat">
                        <strong>312</strong>
                        <span><spring:message code="communities.sidebar.stats.posts"/></span>
                    </div>
                </section>

                <section class="community-sidebar-card">
                    <p class="community-sidebar-eyebrow"><spring:message code="communities.sidebar.flair.eyebrow"/></p>
                    <h2 class="community-sidebar-title"><spring:message code="communities.sidebar.flair.title"/></h2>
                    <div class="community-flair-card">
                        <span class="community-flair-avatar" aria-hidden="true"></span>
                        <div class="community-flair-copy">
                            <sec:authorize access="isAuthenticated()">
                                <sec:authentication property="principal.displayName" var="communityDisplayName"/>
                                <strong><c:out value="${communityDisplayName}"/></strong>
                                <span><spring:message code="communities.sidebar.flair.authenticated"/></span>
                            </sec:authorize>
                            <sec:authorize access="isAnonymous()">
                                <strong><c:out value="${communityGuestName}"/></strong>
                                <span><spring:message code="communities.sidebar.flair.anonymous"/></span>
                            </sec:authorize>
                        </div>
                    </div>
                </section>

                <section class="community-sidebar-card" id="communityRules">
                    <p class="community-sidebar-eyebrow"><spring:message code="communities.sidebar.rules.eyebrow"/></p>
                    <h2 class="community-sidebar-title"><spring:message code="communities.sidebar.rules.title"/></h2>
                    <ol class="community-rules-list">
                        <li><spring:message code="communities.sidebar.rules.one"/></li>
                        <li><spring:message code="communities.sidebar.rules.two"/></li>
                        <li><spring:message code="communities.sidebar.rules.three"/></li>
                    </ol>
                </section>
            </aside>
        </div>
    </main>

    <pa:footer/>
</body>
</html>
