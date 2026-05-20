<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<pa:page-head titleCode="communities.postDetail.title" styles="/css/communities.css"/>
<body>
    <pa:nav activePage="communities"/>
    <c:url var="communityDetailUrl" value="/communities/classics"/>
    <spring:message var="communityAvatarMark" code="communities.hero.avatarMark"/>
    <spring:message var="communityPostBackLabel" code="communities.postDetail.back"/>
    <spring:message var="communityPostCommentPlaceholder" code="communities.postDetail.comment.placeholder"/>
    <spring:message var="communityPostSearchPlaceholder" code="communities.postDetail.search.placeholder"/>
    <main class="community-post-page">
        <section class="community-post-shell">
            <div class="community-post-header">
                <a class="community-back-link" href="${communityDetailUrl}" aria-label="${communityPostBackLabel}">
                    <pa:icon name="chevron-left" size="18"/>
                </a>
                <div class="community-post-origin">
                    <span class="community-post-origin-avatar" aria-hidden="true"><c:out value="${communityAvatarMark}"/></span>
                    <div>
                        <p class="community-post-origin-line">
                            <strong><spring:message code="communities.postDetail.community"/></strong>
                            <span aria-hidden="true">•</span>
                            <span><spring:message code="communities.postDetail.time"/></span>
                        </p>
                        <p class="community-post-origin-author"><spring:message code="communities.postDetail.author"/></p>
                    </div>
                </div>
            </div>

            <article class="community-post-detail-card">
                <h1><spring:message code="communities.postDetail.heading"/></h1>
                <span class="community-post-type community-post-type--photo"><spring:message code="communities.post.type.photo"/></span>
                <p class="community-post-detail-body"><spring:message code="communities.postDetail.body"/></p>

                <div class="community-post-detail-actions">
                    <span class="community-post-detail-pill is-positive"><spring:message code="communities.postDetail.helpful"/></span>
                    <span class="community-post-detail-pill"><spring:message code="communities.postDetail.discussionCount"/></span>
                    <button type="button" class="community-post-detail-pill"><spring:message code="communities.postDetail.save"/></button>
                    <button type="button" class="community-post-detail-pill"><spring:message code="communities.postDetail.share"/></button>
                </div>
            </article>

            <form class="community-comment-composer" novalidate="novalidate">
                <label for="communityPostComment" class="sr-only"><c:out value="${communityPostCommentPlaceholder}"/></label>
                <textarea id="communityPostComment" rows="2" placeholder="${communityPostCommentPlaceholder}"></textarea>
            </form>

            <div class="community-comments-toolbar">
                <div class="community-comments-sort">
                    <span><spring:message code="communities.postDetail.sort.label"/></span>
                    <button type="button"><spring:message code="communities.postDetail.sort.best"/> <pa:icon name="chevron-down" size="12"/></button>
                </div>
                <label class="community-comments-search">
                    <pa:icon name="search" size="16"/>
                    <input type="search" placeholder="${communityPostSearchPlaceholder}">
                </label>
            </div>

            <section class="community-comments-list">
                <pa:community-comment-card
                        authorCode="communities.postDetail.comment.one.author"
                        timeCode="communities.postDetail.comment.one.time"
                        bodyCode="communities.postDetail.comment.one.body"
                        helpfulCount="32"/>
                <div class="community-comment-thread">
                    <pa:community-comment-card
                            authorCode="communities.postDetail.comment.reply.author"
                            timeCode="communities.postDetail.comment.reply.time"
                            bodyCode="communities.postDetail.comment.reply.body"
                            helpfulCount="18"
                            isOp="true"
                            nested="true"/>
                </div>
                <pa:community-comment-card
                        authorCode="communities.postDetail.comment.two.author"
                        timeCode="communities.postDetail.comment.two.time"
                        bodyCode="communities.postDetail.comment.two.body"
                        helpfulCount="11"/>
            </section>
        </section>
    </main>
    <pa:footer/>
</body>
</html>
