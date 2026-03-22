<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="model"      required="true" %>
<%@ attribute name="generation" required="false" %>
<%@ attribute name="bodyType"   required="false" %>
<%@ attribute name="imageUrl"   required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="car-card">
    <div class="card-image-wrap">
        <c:choose>
            <c:when test="${not empty imageUrl}">
                <img src="${imageUrl}" alt="${model}" loading="lazy">
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
                <c:when test="${not empty bodyType}"><c:out value="${bodyType}"/></c:when>
                <c:otherwise>Vehicle</c:otherwise>
            </c:choose>
        </span>
        <div class="card-title-row">
            <span class="card-title"><c:out value="${model}"/></span>
        </div>
        <div class="card-footer">
            <span class="card-meta">
                <c:if test="${not empty generation}"><c:out value="${generation}"/></c:if>
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
