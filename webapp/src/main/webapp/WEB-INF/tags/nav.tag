<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="activePage" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav>
    <a href="<c:url value='/'/>" class="nav-brand">La Posta Autos</a>
    <ul class="nav-links">
        <li><a href="<c:url value='/'/>" class="${activePage eq 'explore' ? 'active' : ''}">Explore</a></li>
        <li><a href="<c:url value='/cars'/>" class="${activePage eq 'reviews' ? 'active' : ''}">Reviews</a></li>
    </ul>
    <div class="nav-right">

    </div>
</nav>
