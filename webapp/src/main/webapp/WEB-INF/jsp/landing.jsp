<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/landing.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
</head>
<body>

    <pa:nav activePage="explore"/>

    <main class="landing-page">
        <section class="landing-hero">
            <div class="hero-copy">
                <h1 class="hero-title">
                    <span class="hero-title-accent">Encontrá</span>
                    <span>tu próximo</span>
                    <span>auto</span>
                </h1>
                <p class="hero-text">
                    Explorá reviews técnicas, compará carrocerías y descubrí modelos con personalidad propia
                    en una galería pensada para entusiastas.
                </p>

                <form class="hero-search" method="get" action="<c:url value='/cars'/>">
                    <label class="sr-only" for="hero-search-input">Buscar autos</label>
                    <div class="hero-search-field">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" aria-hidden="true">
                            <circle cx="11" cy="11" r="7"></circle>
                            <path d="m20 20-3.5-3.5"></path>
                        </svg>
                        <input
                                id="hero-search-input"
                                type="search"
                                name="q"
                                placeholder="Buscar marcas, modelos o carrocerías..."
                                autocomplete="off">
                    </div>
                    <button type="submit" class="btn-primary">Buscar</button>
                </form>

                <c:if test="${not empty heroCar}">
                    <div class="hero-spotlight">
                        <div class="hero-spotlight-header">
                            <span class="hero-spotlight-label">Modelo destacado</span>
                            <c:if test="${reviewStatsByCarId[heroCar.id].reviewCount gt 0}">
                                <span class="hero-spotlight-rating">
                                    <c:out value="${reviewStatsByCarId[heroCar.id].averageRating}"/> /
                                    <c:out value="${reviewStatsByCarId[heroCar.id].reviewCount}"/> reviews
                                </span>
                            </c:if>
                        </div>
                        <h2><c:out value="${heroCar.brandName}"/> <c:out value="${heroCar.model}"/></h2>
                        <p>
                            <c:choose>
                                <c:when test="${not empty heroCar.description and fn:length(heroCar.description) gt 180}">
                                    <c:out value="${fn:substring(heroCar.description, 0, 180)}"/>...
                                </c:when>
                                <c:when test="${not empty heroCar.description}">
                                    <c:out value="${heroCar.description}"/>
                                </c:when>
                                <c:otherwise>
                                    Rendimiento, diseño y carácter en una selección curada de autos que vale la pena manejar.
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </div>
                </c:if>
            </div>

            <c:if test="${not empty heroCar and heroCar.hasImage}">
                <c:url var="heroCarImageUrl" value="/car-image">
                    <c:param name="carId" value="${heroCar.id}"/>
                </c:url>
            </c:if>
            <c:if test="${not empty heroCar and empty heroCarImageUrl and not empty heroCar.imageUrl}">
                <c:set var="heroCarImageUrl" value="${heroCar.imageUrl}"/>
            </c:if>

            <div class="hero-stage">
                <div class="hero-glow"></div>
                <div class="hero-media-frame" aria-hidden="true">
                    <c:choose>
                        <c:when test="${not empty heroCarImageUrl}">
                            <img
                                src="${heroCarImageUrl}"
                                alt=""
                                class="hero-car-image">
                        </c:when>
                        <c:otherwise>
                            <div class="hero-placeholder"></div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${not empty heroReview}">
                    <article class="hero-review-card">
                        <div class="hero-review-header">
                            <div class="hero-review-thumb" aria-hidden="true">
                                <c:choose>
                                    <c:when test="${not empty heroCarImageUrl}">
                                        <img src="${heroCarImageUrl}" alt="" class="hero-review-thumb-image">
                                    </c:when>
                                    <c:otherwise>
                                        <span><c:out value="${fn:substring(heroCar.brandName, 0, 1)}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="hero-review-rating">
                                <span class="hero-review-stars" aria-hidden="true">
                                    <c:forEach var="i" begin="1" end="5">
                                        <svg viewBox="0 0 24 24" width="18" height="18">
                                            <c:choose>
                                                <c:when test="${heroReview.rating >= i}">
                                                    <path fill="#ff5719" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                                </c:when>
                                                <c:when test="${heroReview.rating >= i - 0.5}">
                                                    <defs>
                                                        <linearGradient id="heroStar${heroReview.id}${i}" x1="0" x2="1" y1="0" y2="0">
                                                            <stop offset="50%" stop-color="#ff5719"/>
                                                            <stop offset="50%" stop-color="#3a3a3a"/>
                                                        </linearGradient>
                                                    </defs>
                                                    <path fill="url(#heroStar${heroReview.id}${i})" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <path fill="#3a3a3a" d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 22 12 18.27 5.82 22 7 14.14 2 9.27l6.91-1.01L12 2z"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </svg>
                                    </c:forEach>
                                </span>
                                <span class="hero-review-score"><c:out value="${heroReview.rating}"/></span>
                            </div>
                        </div>

                        <p class="hero-review-quote">
                            <c:choose>
                                <c:when test="${not empty heroReview.body and fn:length(heroReview.body) gt 150}">
                                    “<c:out value="${fn:substring(heroReview.body, 0, 150)}"/>...”
                                </c:when>
                                <c:when test="${not empty heroReview.body}">
                                    “<c:out value="${heroReview.body}"/>”
                                </c:when>
                                <c:when test="${not empty heroReview.title}">
                                    “<c:out value="${heroReview.title}"/>”
                                </c:when>
                                <c:otherwise>
                                    “Una reseña reciente de la comunidad sobre este modelo destacado.”
                                </c:otherwise>
                            </c:choose>
                        </p>

                        <p class="hero-review-meta">
                            <span>
                                <c:choose>
                                    <c:when test="${not empty heroReview.userId}">
                                        Usuario #<c:out value="${heroReview.userId}"/>
                                    </c:when>
                                    <c:when test="${not empty heroReview.reviewerEmail}">
                                        <c:out value="${fn:substringBefore(heroReview.reviewerEmail, '@')}"/>
                                    </c:when>
                                    <c:otherwise>
                                        Anónimo
                                    </c:otherwise>
                                </c:choose>
                            </span>
                            <span><c:out value="${fn:substring(heroReview.createdAt, 0, 10)}"/></span>
                        </p>
                    </article>
                </c:if>
            </div>
        </section>

        <section class="featured-section">
            <div class="section-heading">
                <div>
                    <span class="section-kicker">Reseñas destacadas</span>
                    <h2>Opiniones sinceras de nuestros usuarios</h2>
                    <p>Una selección de autos con reseñas, puntajes visibles y acceso a información detallada del vehículo.</p>
                </div>
                <c:url var="allReviewsUrl" value="/cars"/>
                <pa:button text="Ver todas las reviews" variant="secondary" icon="arrow-right" href="${allReviewsUrl}"/>
            </div>

            <c:choose>
                <c:when test="${empty featuredCars}">
                    <div class="landing-empty-state">
                        <p>Todavía no hay autos destacados para mostrar.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="featured-grid">
                        <c:forEach var="car" items="${featuredCars}">
                            <c:url var="reviewUrl" value="/reviews">
                                <c:param name="carId" value="${car.id}"/>
                            </c:url>
                            <pa:car-card
                                model="${car.brandName} ${car.model}"
                                bodyType="${car.bodyType}"
                                carId="${car.id}"
                                hasImage="${car.hasImage}"
                                href="${reviewUrl}"
                                averageRating="${reviewStatsByCarId[car.id].averageRating}"
                                reviewCount="${reviewStatsByCarId[car.id].reviewCount}"/>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <pa:footer/>

</body>
</html>
