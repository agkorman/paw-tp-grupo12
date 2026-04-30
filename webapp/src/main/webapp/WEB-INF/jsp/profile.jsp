<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Perfil | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=4'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css?v=4'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile.css?v=6'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-review-card.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-modals.css?v=1'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-connections.css?v=1'/>">
    <link rel="stylesheet" href="<c:url value='/css/review-tags.css'/>">
</head>
<body>
    <pa:nav activePage="profile"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

    <c:set var="profileReviewsPreviewLimit" value="2"/>
    <c:set var="favoriteCarsPreviewLimit" value="4"/>
    <c:set var="likedReviewsPreviewLimit" value="2"/>

    <main class="profile-page">
        <section class="profile-hero" aria-labelledby="profileName">
            <div class="profile-avatar" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.65" aria-hidden="true" focusable="false">
                    <circle cx="12" cy="8" r="4"></circle>
                    <path d="M4 21c1.6-4 4.2-6 8-6s6.4 2 8 6"></path>
                </svg>
            </div>

            <div class="profile-summary">
                <h1 id="profileName"><c:out value="${profile.name}"/></h1>
                <p class="profile-email"><c:out value="${profile.email}"/></p>

                <dl class="profile-stats" aria-label="Estadísticas del perfil">
                    <div>
                        <dt>
                            <button type="button" class="profile-stat-button" data-scroll-to-reviews>
                                <c:out value="${profile.reviewCount}"/>
                            </button>
                        </dt>
                        <dd>Reviews</dd>
                    </div>
                    <div>
                        <dt>
                            <button
                                    type="button"
                                    class="profile-stat-button"
                                    data-open-connections-modal
                                    data-connections-kind="following"
                                    data-connections-title="Seguidos">
                                <c:out value="${profile.followingCount}"/>
                            </button>
                        </dt>
                        <dd>Seguidos</dd>
                    </div>
                    <div>
                        <dt>
                            <button
                                    type="button"
                                    class="profile-stat-button"
                                    data-open-connections-modal
                                    data-connections-kind="followers"
                                    data-connections-title="Seguidores">
                                <c:out value="${profile.followerCount}"/>
                            </button>
                        </dt>
                        <dd>Seguidores</dd>
                    </div>
                </dl>
            </div>

            <c:choose>
                <c:when test="${ownProfile}">
                    <button type="button" class="btn-primary profile-action-button" data-open-edit-profile-modal>Ajustes de perfil</button>
                    <c:if test="${canRequestModerator}">
                        <button type="button" class="profile-moderator-link" data-open-request-admin-modal>
                            ¿Querés ser moderador?
                        </button>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <c:url var="profileFollowUrl" value="/profiles/${profile.id}/follow"/>
                    <c:choose>
                        <c:when test="${authenticated}">
                            <form class="profile-action-form"
                                  method="post"
                                  action="${profileFollowUrl}"
                                  data-auth-resume-intent="follow-profile-${profile.id}">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                <button
                                        type="submit"
                                        class="btn-primary profile-action-button profile-follow-button ${followingProfile ? 'is-following' : ''}"
                                        aria-pressed="${followingProfile}">
                                    <c:choose>
                                        <c:when test="${followingProfile}">Siguiendo</c:when>
                                        <c:otherwise>Seguir</c:otherwise>
                                    </c:choose>
                                </button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <c:url var="profileFollowLoginUrl" value="/login">
                                <c:param name="redirect" value="/profiles/${profile.id}"/>
                                <c:param name="intent" value="follow-profile-${profile.id}"/>
                            </c:url>
                            <a href="${profileFollowLoginUrl}"
                               class="btn-primary profile-action-button profile-follow-button"
                               data-auth-resume-intent="follow-profile-${profile.id}"
                               data-auth-required="true"
                               data-auth-required-action="seguir a este usuario"
                               data-auth-required-intent="follow-profile-${profile.id}">
                                Seguir
                            </a>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="profile-tabs" aria-label="Contenido del perfil" data-profile-tabs>
            <div class="profile-tabs-list ${ownProfile ? '' : 'profile-tabs-list-single'}" role="tablist" aria-label="Secciones del perfil">
                <button type="button"
                        id="profileReviewsTab"
                        class="profile-tab"
                        role="tab"
                        aria-selected="true"
                        aria-controls="profileReviewsPanel"
                        data-profile-tab-target="profileReviewsPanel">
                    <span>
                        <c:choose>
                            <c:when test="${ownProfile}">Mis reseñas</c:when>
                            <c:otherwise>Reviews</c:otherwise>
                        </c:choose>
                    </span>
                    <strong><c:out value="${fn:length(profileReviews)}"/></strong>
                </button>
                <c:if test="${ownProfile}">
                    <button type="button"
                            id="profileFavoritesTab"
                            class="profile-tab"
                            role="tab"
                            aria-selected="false"
                            aria-controls="profileFavoritesPanel"
                            data-profile-tab-target="profileFavoritesPanel">
                        <span>Autos favoritos</span>
                        <strong><c:out value="${fn:length(favoriteCars)}"/></strong>
                    </button>
                    <button type="button"
                            id="profileLikedTab"
                            class="profile-tab"
                            role="tab"
                            aria-selected="false"
                            aria-controls="profileLikedPanel"
                            data-profile-tab-target="profileLikedPanel">
                        <span>Reseñas likeadas</span>
                        <strong><c:out value="${likedActivityCount}"/></strong>
                    </button>
                </c:if>
            </div>

            <section id="profileReviewsPanel"
                     class="profile-tab-panel profile-reviews-section"
                     role="tabpanel"
                     aria-labelledby="profileReviewsTab"
                     data-collapsible-section>
                <div class="profile-section-heading">
                    <h2 id="profileReviewsTitle">
                        <c:choose>
                            <c:when test="${ownProfile}">Mis Reseñas</c:when>
                            <c:otherwise>Reviews</c:otherwise>
                        </c:choose>
                    </h2>
                    <span><c:out value="${fn:length(profileReviews)}"/></span>
                </div>

                <c:choose>
                    <c:when test="${empty profileReviews}">
                        <div class="profile-empty-state">
                            <p>Todavía no tienes reseñas publicadas.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="profile-review-list">
                            <c:forEach var="profileReview" items="${profileReviews}" varStatus="profileReviewStatus">
                                <div class="profile-collapsible-item"
                                     <c:if test="${profileReviewStatus.index ge profileReviewsPreviewLimit}">data-collapsible-extra</c:if>>
                                    <pa:profile-review-card
                                            reviewCard="${profileReview}"
                                            editable="${profileReview.ownedByCurrentUser}"/>
                                </div>
                            </c:forEach>
                        </div>
                        <c:if test="${fn:length(profileReviews) gt profileReviewsPreviewLimit}">
                            <div class="profile-collapsible-actions">
                                <pa:collapsible-toggle/>
                            </div>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </section>

            <c:if test="${ownProfile}">
                <section id="profileFavoritesPanel"
                         class="profile-tab-panel profile-favorites-section"
                         role="tabpanel"
                         aria-labelledby="profileFavoritesTab"
                         data-collapsible-section>
                    <div class="profile-section-heading">
                        <h2 id="profileFavoritesTitle">Autos favoritos</h2>
                        <span><c:out value="${fn:length(favoriteCars)}"/></span>
                    </div>

                    <c:choose>
                        <c:when test="${empty favoriteCars}">
                            <div class="profile-empty-state">
                                <p>Todavía no agregaste autos a favoritos.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="profile-favorites-grid">
                                <c:forEach var="favoriteCar" items="${favoriteCars}" varStatus="favoriteCarStatus">
                                    <c:url var="favoriteReviewUrl" value="/reviews">
                                        <c:param name="carId" value="${favoriteCar.id}"/>
                                    </c:url>
                                    <div class="profile-collapsible-item"
                                         <c:if test="${favoriteCarStatus.index ge favoriteCarsPreviewLimit}">data-collapsible-extra</c:if>>
                                        <pa:car-card
                                                model="${favoriteCar.brandName} ${favoriteCar.model}"
                                                bodyType="${favoriteCar.bodyType}"
                                                carId="${favoriteCar.id}"
                                                hasImage="${favoriteCar.hasImage}"
                                                href="${favoriteReviewUrl}"
                                                averageRating="${reviewStatsByCarId[favoriteCar.id].averageRating}"
                                                reviewCount="${reviewStatsByCarId[favoriteCar.id].reviewCount}"/>
                                    </div>
                                </c:forEach>
                            </div>
                            <c:if test="${fn:length(favoriteCars) gt favoriteCarsPreviewLimit}">
                                <div class="profile-collapsible-actions">
                                    <pa:collapsible-toggle/>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section id="profileLikedPanel"
                         class="profile-tab-panel profile-liked-section"
                         role="tabpanel"
                         aria-labelledby="profileLikedTab"
                         data-collapsible-section>
                    <div class="profile-section-heading">
                        <h2 id="profileLikedTitle">Reseñas likeadas</h2>
                        <span><c:out value="${likedActivityCount}"/></span>
                    </div>

                    <c:choose>
                        <c:when test="${empty likedReviews and empty likedReplies}">
                            <div class="profile-empty-state">
                                <p>Todavía no le diste like a ninguna reseña o respuesta.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${not empty likedReviews}">
                                <div class="profile-liked-group">
                                    <h3>Reseñas</h3>
                                    <div class="profile-review-list">
                                        <c:forEach var="likedReview" items="${likedReviews}" varStatus="likedReviewStatus">
                                            <div class="profile-collapsible-item"
                                                 <c:if test="${likedReviewStatus.index ge likedReviewsPreviewLimit}">data-collapsible-extra</c:if>>
                                                <pa:profile-review-card
                                                        reviewCard="${likedReview}"
                                                        editable="${likedReview.ownedByCurrentUser}"/>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:if>
                            <c:if test="${not empty likedReplies}">
                                <div class="profile-liked-group">
                                    <h3>Respuestas</h3>
                                    <div class="profile-liked-reply-list">
                                        <c:forEach var="likedReply" items="${likedReplies}" varStatus="likedReplyStatus">
                                            <div class="profile-collapsible-item"
                                                 <c:if test="${likedReplyStatus.index ge likedReviewsPreviewLimit}">data-collapsible-extra</c:if>>
                                                <pa:profile-liked-reply-card replyCard="${likedReply}"/>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:if>
                            <c:if test="${fn:length(likedReviews) gt likedReviewsPreviewLimit or fn:length(likedReplies) gt likedReviewsPreviewLimit}">
                                <div class="profile-collapsible-actions">
                                    <pa:collapsible-toggle/>
                                </div>
                            </c:if>
                        </c:otherwise>
                    </c:choose>
                </section>
            </c:if>
        </section>
    </main>

    <pa:review-delete-modal/>
    <sec:authorize access="hasRole('ADMIN')">
        <pa:reply-delete-modal/>
    </sec:authorize>
    <pa:edit-profile-modal profile="${profile}"/>
    <pa:profile-connections-modal followingUsers="${followingUsers}" followerUsers="${followerUsers}"/>
    <pa:auth-required-modal/>
    <c:if test="${ownProfile}">
        <pa:request-admin-modal/>
    </c:if>
    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/action-menu.js'/>"></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
    <script src="<c:url value='/js/profile.js?v=8'/>"></script>
    <sec:authorize access="hasRole('ADMIN')">
        <script src="<c:url value='/js/admin-review-actions.js'/>"></script>
    </sec:authorize>
    <c:if test="${ownProfile}">
        <script src="<c:url value='/js/admin-request-modal.js'/>"></script>
    </c:if>
</body>
</html>
