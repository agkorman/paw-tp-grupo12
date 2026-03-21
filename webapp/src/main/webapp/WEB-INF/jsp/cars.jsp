<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>The Kinetic Gallery</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        :root {
            --surface:                #131313;
            --surface-container-lowest: #0d0d0d;
            --surface-container-low:  #1a1a1a;
            --surface-container:      #1f1f1f;
            --surface-container-high: #252525;
            --surface-container-highest: #2e2e2e;
            --on-surface:             #e5e2e1;
            --on-surface-variant:     #c4c6cc;
            --secondary:              #c4c6cc;
            --primary:                #ffb59e;
            --primary-container:      #ff5719;
            --tertiary:               #5ed4ff;
            --tertiary-fixed:         #1a3a4a;
            --on-tertiary-fixed:      #5ed4ff;
            --outline-variant:        rgba(229, 226, 225, 0.15);
        }

        html, body {
            background-color: var(--surface);
            color: var(--on-surface);
            font-family: 'Inter', sans-serif;
            min-height: 100vh;
        }

        /* ── NAV ── */
        nav {
            position: sticky;
            top: 0;
            z-index: 100;
            background: rgba(19, 19, 19, 0.70);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 3rem;
            height: 56px;
        }

        .nav-brand {
            font-family: 'Space Grotesk', sans-serif;
            font-size: 0.75rem;
            font-weight: 700;
            letter-spacing: 0.12em;
            text-transform: uppercase;
            color: var(--on-surface);
            text-decoration: none;
        }

        .nav-links {
            display: flex;
            gap: 2rem;
            list-style: none;
        }

        .nav-links a {
            font-family: 'Inter', sans-serif;
            font-size: 0.875rem;
            font-weight: 500;
            color: var(--secondary);
            text-decoration: none;
            padding-bottom: 2px;
            transition: color 0.2s;
        }

        .nav-links a.active,
        .nav-links a:hover {
            color: var(--on-surface);
        }

        .nav-links a.active {
            border-bottom: 2px solid var(--primary-container);
        }

        .nav-right {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .search-box {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            background: var(--surface-container-low);
            border-radius: 0.375rem;
            padding: 0.375rem 0.75rem;
            width: 180px;
        }

        .search-box svg {
            color: var(--secondary);
            flex-shrink: 0;
        }

        .search-box input {
            background: transparent;
            border: none;
            outline: none;
            font-size: 0.8125rem;
            color: var(--on-surface);
            font-family: 'Inter', sans-serif;
            width: 100%;
        }

        .search-box input::placeholder { color: var(--secondary); }

        .avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background: var(--surface-container-highest);
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
        }

        /* ── FILTER BAR ── */
        .filter-bar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 1.5rem 3rem;
        }

        .filters {
            display: flex;
            gap: 0.75rem;
        }

        .filter-chip {
            display: flex;
            align-items: center;
            gap: 0.375rem;
            background: var(--surface-container-high);
            border-radius: 9999px;
            padding: 0.375rem 0.875rem;
            font-size: 0.75rem;
            font-weight: 500;
            color: var(--secondary);
            cursor: pointer;
            letter-spacing: 0.04em;
            text-transform: uppercase;
            transition: background 0.2s;
        }

        .filter-chip:hover { background: var(--surface-container-highest); }

        .filter-chip svg { opacity: 0.7; }

        .filter-meta {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .count-label {
            font-size: 0.75rem;
            color: var(--secondary);
            letter-spacing: 0.06em;
            text-transform: uppercase;
        }

        .btn-primary {
            background: linear-gradient(135deg, var(--primary-container), var(--primary));
            color: #fff;
            font-family: 'Inter', sans-serif;
            font-size: 0.6875rem;
            font-weight: 700;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            border: none;
            border-radius: 0.375rem;
            padding: 0.5rem 1.125rem;
            cursor: pointer;
            transition: opacity 0.2s;
        }

        .btn-primary:hover { opacity: 0.85; }

        /* ── GRID ── */
        .catalog-section {
            padding: 0 3rem 4rem;
        }

        .cars-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1.5rem;
        }

        /* ── CAR CARD ── */
        .car-card {
            background: var(--surface-container-high);
            border-radius: 0.5rem;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            transition: background 0.2s, transform 0.2s;
            cursor: pointer;
        }

        .car-card:hover {
            background: var(--surface-container-highest);
            transform: translateY(-2px);
        }

        .card-image-wrap {
            position: relative;
            width: 100%;
            padding-top: 60%;
            overflow: hidden;
        }

        .card-image-wrap img {
            position: absolute;
            inset: 0;
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.4s ease;
        }

        .car-card:hover .card-image-wrap img {
            transform: scale(1.04);
        }

        .card-image-wrap .img-placeholder {
            position: absolute;
            inset: 0;
            background: var(--surface-container);
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .card-image-wrap .img-placeholder svg {
            opacity: 0.2;
        }

        /* gradient so text can sit on image if needed */
        .card-image-wrap::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 40%;
            background: linear-gradient(to top, rgba(19,19,19,0.55), transparent);
            pointer-events: none;
        }

        .card-body {
            padding: 1rem 1.125rem 1.125rem;
            display: flex;
            flex-direction: column;
            gap: 0.625rem;
        }

        .card-category {
            font-size: 0.6875rem;
            font-weight: 600;
            letter-spacing: 0.1em;
            text-transform: uppercase;
            color: var(--secondary);
        }

        .card-title-row {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            gap: 0.5rem;
        }

        .card-title {
            font-family: 'Space Grotesk', sans-serif;
            font-size: 1.0625rem;
            font-weight: 600;
            color: var(--on-surface);
            line-height: 1.3;
        }

        .card-footer {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-top: 0.125rem;
        }

        .card-meta {
            font-size: 0.75rem;
            color: var(--secondary);
        }

        .card-specs-link {
            font-size: 0.75rem;
            font-weight: 600;
            color: var(--primary-container);
            text-decoration: none;
            letter-spacing: 0.02em;
            display: flex;
            align-items: center;
            gap: 0.25rem;
            transition: color 0.2s;
            white-space: nowrap;
        }

        .card-specs-link:hover { color: var(--primary); }

        /* ── EMPTY STATE ── */
        .empty-state {
            text-align: center;
            padding: 6rem 0;
            color: var(--secondary);
        }

        .empty-state p {
            font-size: 1rem;
            margin-top: 0.5rem;
        }

        /* ── DISCOVER MORE ── */
        .discover-wrap {
            display: flex;
            justify-content: center;
            padding: 1rem 0 3rem;
        }

        .btn-secondary {
            background: var(--surface-container-high);
            color: var(--on-surface);
            font-family: 'Inter', sans-serif;
            font-size: 0.75rem;
            font-weight: 700;
            letter-spacing: 0.12em;
            text-transform: uppercase;
            border: none;
            border-radius: 0.375rem;
            padding: 0.75rem 2rem;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            transition: background 0.2s;
        }

        .btn-secondary:hover { background: var(--surface-container-highest); }

        /* ── FOOTER ── */
        footer {
            background: var(--surface-container-lowest);
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 1.25rem 3rem;
        }

        .footer-copy {
            font-size: 0.6875rem;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            color: var(--secondary);
        }

        .footer-links {
            display: flex;
            gap: 1.5rem;
            list-style: none;
        }

        .footer-links a {
            font-size: 0.6875rem;
            font-weight: 600;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            color: var(--secondary);
            text-decoration: none;
            transition: color 0.2s;
        }

        .footer-links a:hover { color: var(--on-surface); }

        @media (max-width: 900px) {
            .cars-grid { grid-template-columns: repeat(2, 1fr); }
            nav, .filter-bar, .catalog-section, footer { padding-left: 1.5rem; padding-right: 1.5rem; }
        }

        @media (max-width: 560px) {
            .cars-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>

    <%-- ── NAV ── --%>
    <nav>
        <a href="<c:url value='/'/>" class="nav-brand">The Kinetic Gallery</a>
        <ul class="nav-links">
            <li><a href="<c:url value='/cars'/>" class="active">Explore</a></li>
            <li><a href="#">Reviews</a></li>
        </ul>
        <div class="nav-right">
            <div class="search-box">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
                </svg>
                <input type="text" placeholder="Quick search…">
            </div>
            <div class="avatar">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                </svg>
            </div>
        </div>
    </nav>

    <%-- ── FILTER BAR ── --%>
    <div class="filter-bar">
        <div class="filters">
            <div class="filter-chip">
                Brand: All
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="6 9 12 15 18 9"/>
                </svg>
            </div>
            <div class="filter-chip">
                Body: All
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="6 9 12 15 18 9"/>
                </svg>
            </div>
            <div class="filter-chip">
                Generation: All
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="6 9 12 15 18 9"/>
                </svg>
            </div>
        </div>
        <div class="filter-meta">
            <c:if test="${not empty cars}">
                <span class="count-label">${fn:length(cars)} vehicles found</span>
            </c:if>
            <button class="btn-primary">Apply Focus</button>
        </div>
    </div>

    <%-- ── CATALOG GRID ── --%>
    <section class="catalog-section">
        <c:choose>
            <c:when test="${empty cars}">
                <div class="empty-state">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                        <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                    </svg>
                    <p>No vehicles found in the gallery.</p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="cars-grid">
                    <c:forEach var="car" items="${cars}">
                        <div class="car-card">
                            <div class="card-image-wrap">
                                <c:choose>
                                    <c:when test="${not empty car.imageUrl}">
                                        <img src="${car.imageUrl}" alt="${car.model}" loading="lazy">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="img-placeholder">
                                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c4c6cc" stroke-width="1.5">
                                                <rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>
                                            </svg>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="card-body">
                                <span class="card-category">
                                    <c:choose>
                                        <c:when test="${not empty car.bodyType}"><c:out value="${car.bodyType}"/></c:when>
                                        <c:otherwise>Vehicle</c:otherwise>
                                    </c:choose>
                                </span>
                                <div class="card-title-row">
                                    <span class="card-title"><c:out value="${car.model}"/></span>
                                </div>
                                <div class="card-footer">
                                    <span class="card-meta">
                                        <c:if test="${not empty car.generation}"><c:out value="${car.generation}"/></c:if>
                                    </span>
                                    <a href="#" class="card-specs-link">
                                        Technical Specs
                                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                            <path d="M5 12h14M12 5l7 7-7 7"/>
                                        </svg>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- ── DISCOVER MORE ── --%>
    <c:if test="${not empty cars}">
        <div class="discover-wrap">
            <button class="btn-secondary">
                Discover More
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="6 9 12 15 18 9"/>
                </svg>
            </button>
        </div>
    </c:if>

    <%-- ── FOOTER ── --%>
    <footer>
        <span class="footer-copy">&copy; 2024 The Kinetic Gallery. Engineered for enthusiasts.</span>
        <ul class="footer-links">
            <li><a href="#">Terms</a></li>
            <li><a href="#">Privacy</a></li>
            <li><a href="#">About</a></li>
        </ul>
    </footer>

</body>
</html>
