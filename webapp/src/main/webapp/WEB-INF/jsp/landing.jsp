<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><spring:message code="landing.title"/></title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <pa:font-head/>
    <link rel="stylesheet" href="<c:url value='/css/design-system.css?v=3'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/css/landing.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/cars.css'/>">
</head>
<body>

    <pa:nav activePage="explore"/>

    <main class="landing-page">
        <spring:message var="ctaStart" code="landing.recommend.cta.start"/>
        <c:url var="recommendUrl" value="/cars/recommend"/>
        <section class="wizard-cta wizard-cta--hero">
            <div class="wizard-cta-shell">
                <div class="wizard-cta-copy">
                    <span class="section-kicker"><spring:message code="landing.recommend.title"/></span>
                    <h1 class="wizard-cta-title"><spring:message code="landing.recommend.cta.heading"/></h1>
                    <p><spring:message code="landing.recommend.cta.detail"/></p>
                    <div class="wizard-cta-actions">
                        <pa:button text="${ctaStart}" variant="primary" icon="arrow-right" href="${recommendUrl}"/>
                    </div>
                </div>
                <spring:message code="review.tag.emoji.fallback" var="landingTagFallback" text="🏷️"/>
                <spring:message code="review.tag.emoji.comfortable" var="landingEmojiComfort" text="${landingTagFallback}"/>
                <spring:message code="review.tag.emoji.low_fuel_consumption" var="landingEmojiLowFuel" text="${landingTagFallback}"/>
                <spring:message code="review.tag.emoji.noisy_cabin" var="landingEmojiNoisy" text="${landingTagFallback}"/>
                <ul class="wizard-cta-preview" aria-hidden="true">
                    <li class="review-tag-chip review-tag-chip--display recommendation-highlight review-tag-chip--furor">
                        <span class="recommendation-highlight-emoji recommendation-highlight-emoji--tier" aria-hidden="true">🔥</span>
                        <span class="recommendation-highlight-emoji recommendation-highlight-emoji--topic" aria-hidden="true"><c:out value="${landingEmojiComfort}"/></span>
                        <span class="recommendation-highlight-tag"><spring:message code="landing.badge.comfortable"/></span>
                    </li>
                    <li class="review-tag-chip review-tag-chip--display recommendation-highlight review-tag-chip--destacado">
                        <span class="recommendation-highlight-emoji recommendation-highlight-emoji--tier" aria-hidden="true">👍</span>
                        <span class="recommendation-highlight-emoji recommendation-highlight-emoji--topic" aria-hidden="true"><c:out value="${landingEmojiLowFuel}"/></span>
                        <span class="recommendation-highlight-tag"><spring:message code="landing.badge.lowConsumption"/></span>
                    </li>
                    <li class="review-tag-chip review-tag-chip--display recommendation-highlight review-tag-chip--alerta">
                        <span class="recommendation-highlight-emoji recommendation-highlight-emoji--tier" aria-hidden="true">⚠️</span>
                        <span class="recommendation-highlight-emoji recommendation-highlight-emoji--topic" aria-hidden="true"><c:out value="${landingEmojiNoisy}"/></span>
                        <span class="recommendation-highlight-tag"><spring:message code="landing.badge.noisyCabin"/></span>
                    </li>
                </ul>
            </div>
        </section>

        <section class="landing-hero">
            <spring:message var="landingSearchLabel" code="landing.search.label"/>
            <spring:message var="landingSearchPlaceholder" code="landing.search.placeholder"/>
            <spring:message var="heroReviewView" code="review.hero.view"/>
            <div class="hero-copy">
                <h2 class="hero-title">
                    <span class="hero-title-accent"><spring:message code="landing.hero.explore.accent"/></span>
                    <span><spring:message code="landing.hero.explore.subject"/></span>
                </h2>
                <p class="hero-text">
                    <spring:message code="landing.hero.text"/>
                </p>

                <form class="hero-search" method="get" action="<c:url value='/cars'/>">
                    <label class="sr-only" for="hero-search-input"><c:out value="${landingSearchLabel}"/></label>
                    <div class="hero-search-field">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" aria-hidden="true">
                            <circle cx="11" cy="11" r="7"></circle>
                            <path d="m20 20-3.5-3.5"></path>
                        </svg>
                        <input
                                id="hero-search-input"
                                type="search"
                                name="q"
                                maxlength="120"
                                placeholder="${fn:escapeXml(landingSearchPlaceholder)}"
                                autocomplete="off">
                    </div>
                    <button type="submit" class="btn-primary"><spring:message code="common.action.search"/></button>
                </form>

                <c:if test="${not empty heroCar}">
                    <div class="hero-spotlight">
                        <div class="hero-spotlight-header">
                            <span class="hero-spotlight-label"><spring:message code="landing.spotlight.label"/></span>
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
                                    <spring:message code="landing.spotlight.fallback"/>
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
            <c:if test="${not empty heroReview}">
                <c:url var="heroReviewUrl" value="/reviews">
                    <c:param name="carId" value="${heroCar.id}"/>
                </c:url>
                <c:set var="heroReviewHref" value="${heroReviewUrl}#review-${heroReview.id}"/>
            </c:if>

            <div class="hero-stage">
                <div class="hero-glow"></div>
                <c:choose>
                    <c:when test="${not empty heroReviewHref}">
                        <a class="hero-media-frame hero-media-link" href="${heroReviewHref}" aria-label="${fn:escapeXml(heroReviewView)}">
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
                        </a>
                    </c:when>
                    <c:otherwise>
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
                    </c:otherwise>
                </c:choose>

                <c:if test="${not empty heroReview}">
                    <pa:hero-review-card
                            heroReview="${heroReview}"
                            heroCarBrandName="${heroCar.brandName}"
                            heroCarImageUrl="${heroCarImageUrl}"
                            href="${heroReviewHref}"/>
                </c:if>
            </div>
        </section>

        <section class="featured-section">
            <div class="section-heading">
                <div>
                    <span class="section-kicker"><spring:message code="landing.featuredReviews.title"/></span>
                    <h2><spring:message code="landing.featuredReviews.subtitle"/></h2>
                    <p><spring:message code="landing.featuredCars.text"/></p>
                </div>
                <c:url var="catalogUrl" value="/cars"/>
                <spring:message var="viewCatalogText" code="common.action.viewCatalog"/>
                <pa:button text="${viewCatalogText}" variant="secondary" icon="arrow-right" href="${catalogUrl}"/>
            </div>

            <c:choose>
                <c:when test="${empty featuredCars}">
                    <div class="landing-empty-state">
                        <p><spring:message code="landing.featuredCars.empty"/></p>
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
                                year="${car.year}"
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
    <pa:auth-required-modal/>
    <script src="<c:url value='/js/reactions.js'/>"></script>
    <script src="<c:url value='/js/auth-required-modal.js'/>"></script>
    <pa:footer/>
</body>
</html>
