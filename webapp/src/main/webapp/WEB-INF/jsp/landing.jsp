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
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=2'/>">
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
                    Explorá reseñas técnicas, compará carrocerías y descubrí modelos con personalidad propia
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
                    <c:url var="heroReviewUrl" value="/reviews">
                        <c:param name="carId" value="${heroCar.id}"/>
                    </c:url>
                    <pa:hero-review-card
                            heroReview="${heroReview}"
                            heroCarBrandName="${heroCar.brandName}"
                            heroCarImageUrl="${heroCarImageUrl}"
                            href="${heroReviewUrl}#review-${heroReview.id}"/>
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
                <c:url var="catalogUrl" value="/cars"/>
                <pa:button text="Ver catálogo" variant="secondary" icon="arrow-right" href="${catalogUrl}"/>
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
                                favorited="${favoritedCarIds[car.id] eq true}"
                                averageRating="${reviewStatsByCarId[car.id].averageRating}"
                                reviewCount="${reviewStatsByCarId[car.id].reviewCount}"/>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <script src="<c:url value='/js/reactions.js'/>"></script>

</body>
</html>
