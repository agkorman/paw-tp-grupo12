<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Perfil | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile.css?v=4'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-review-card.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-modals.css?v=1'/>">
    <link rel="stylesheet" href="<c:url value='/css/profile-connections.css?v=1'/>">
</head>
<body>
    <pa:nav activePage="profile"/>
    <c:set var="authenticated" value="${not empty pageContext.request.userPrincipal}"/>

    <main class="profile-page">
        <section class="profile-hero" aria-labelledby="profileName">
            <div class="profile-avatar" aria-hidden="true">
                <span><c:out value="${profile.initials}"/></span>
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

        <section class="profile-reviews-section" aria-labelledby="profileReviewsTitle">
            <div class="profile-section-heading">
                <h2 id="profileReviewsTitle">
                    <c:choose>
                        <c:when test="${ownProfile}">Mis reviews</c:when>
                        <c:otherwise>Reviews</c:otherwise>
                    </c:choose>
                </h2>
                <span><c:out value="${fn:length(profileReviews)}"/></span>
            </div>

            <c:choose>
                <c:when test="${empty profileReviews}">
                    <div class="profile-empty-state">
                        <p>Todavía no hay reviews publicadas.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="profile-review-list">
                        <c:forEach var="profileReview" items="${profileReviews}">
                            <pa:profile-review-card
                                    reviewCard="${profileReview}"
                                    editable="${profileReview.ownedByCurrentUser}"/>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>

        <c:if test="${ownProfile}">
            <section class="profile-favorites-section" aria-labelledby="profileFavoritesTitle">
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
                            <c:forEach var="favoriteCar" items="${favoriteCars}">
                                <c:url var="favoriteReviewUrl" value="/reviews">
                                    <c:param name="carId" value="${favoriteCar.id}"/>
                                </c:url>
                                <pa:car-card
                                        model="${favoriteCar.brandName} ${favoriteCar.model}"
                                        bodyType="${favoriteCar.bodyType}"
                                        carId="${favoriteCar.id}"
                                        hasImage="${favoriteCar.hasImage}"
                                        href="${favoriteReviewUrl}"
                                        favorited="true"
                                        averageRating="${reviewStatsByCarId[favoriteCar.id].averageRating}"
                                        reviewCount="${reviewStatsByCarId[favoriteCar.id].reviewCount}"/>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="profile-liked-section" aria-labelledby="profileLikedTitle">
                <div class="profile-section-heading">
                    <h2 id="profileLikedTitle">Reviews likeadas</h2>
                    <span><c:out value="${likedActivityCount}"/></span>
                </div>

                <c:choose>
                    <c:when test="${empty likedReviews and empty likedReplies}">
                        <div class="profile-empty-state">
                            <p>Todavía no likeaste reviews ni respuestas.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${not empty likedReviews}">
                            <div class="profile-liked-group">
                                <h3>Reviews</h3>
                                <div class="profile-review-list">
                                    <c:forEach var="likedReview" items="${likedReviews}">
                                        <pa:profile-review-card
                                                reviewCard="${likedReview}"
                                                editable="${likedReview.ownedByCurrentUser}"/>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${not empty likedReplies}">
                            <div class="profile-liked-group">
                                <h3>Respuestas</h3>
                                <div class="profile-liked-reply-list">
                                    <c:forEach var="likedReply" items="${likedReplies}">
                                        <pa:profile-liked-reply-card replyCard="${likedReply}"/>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:if>
    </main>

    <pa:create-review-modal/>
    <pa:review-delete-modal/>
    <pa:edit-profile-modal profile="${profile}"/>
    <pa:profile-connections-modal followingUsers="${followingUsers}" followerUsers="${followerUsers}"/>
    <pa:auth-required-modal/>
    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/review-modal.js?v=3'/>"></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <script src="<c:url value='/js/form-submit-lock.js'/>"></script>
    <script src="<c:url value='/js/profile.js?v=4'/>"></script>
</body>
</html>
