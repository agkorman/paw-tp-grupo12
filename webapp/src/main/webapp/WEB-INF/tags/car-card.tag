<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="model"      required="true" %>
<%@ attribute name="generation" required="false" %>
<%@ attribute name="bodyType"   required="false" %>
<%@ attribute name="imageUrl"   required="false" %>
<%@ attribute name="linkUrl"    required="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:choose>
    <c:when test="${not empty linkUrl}">
        <a class="car-card-link" href="${fn:escapeXml(linkUrl)}">
            <div class="car-card">
                <div class="card-image-wrap">
                    <c:choose>
                        <c:when test="${not empty imageUrl}">
                            <img src="${fn:escapeXml(imageUrl)}" alt="${fn:escapeXml(model)}" loading="lazy">
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
                        <span class="card-specs-link">
                            Technical Specs
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <path d="M5 12h14M12 5l7 7-7 7"/>
                            </svg>
                        </span>
                    </div>
                </div>
            </div>
        </a>
    </c:when>
    <c:otherwise>
        <div class="car-card">
            <div class="card-image-wrap">
                <c:choose>
                    <c:when test="${not empty imageUrl}">
                        <img src="${fn:escapeXml(imageUrl)}" alt="${fn:escapeXml(model)}" loading="lazy">
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
                    <span class="card-specs-link">
                        Technical Specs
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <path d="M5 12h14M12 5l7 7-7 7"/>
                        </svg>
                    </span>
                </div>
            </div>
        </div>
    </c:otherwise>
</c:choose>
