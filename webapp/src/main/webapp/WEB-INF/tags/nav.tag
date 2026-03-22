<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activePage" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav>
    <a href="<c:url value='/cars'/>" class="nav-brand">La Posta Autos</a>
    <ul class="nav-links">
        <li><a href="<c:url value='/cars'/>" class="${activePage eq 'explore' ? 'active' : ''}">Explore</a></li>
        <li><a href="#" class="${activePage eq 'reviews' ? 'active' : ''}">Reviews</a></li>
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
