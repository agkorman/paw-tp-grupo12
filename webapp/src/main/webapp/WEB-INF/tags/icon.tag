<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="name"     required="true" %>
<%@ attribute name="size"     required="false" type="java.lang.Integer" %>
<%@ attribute name="cssClass" required="false" %>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:choose>

    <%-- Navigation & UI --%>
    <c:when test="${name eq 'chevron-down'}">
        <c:set var="s" value="${empty size ? 12 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><polyline points="6 9 12 15 18 9"/></svg>
    </c:when>

    <c:when test="${name eq 'chevron-left'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M15 18l-6-6 6-6"/></svg>
    </c:when>

    <c:when test="${name eq 'chevron-right'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M9 6l6 6-6 6"/></svg>
    </c:when>

    <c:when test="${name eq 'arrow-right'}">
        <c:set var="s" value="${empty size ? 12 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
    </c:when>

    <c:when test="${name eq 'close'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M18 6 6 18M6 6l12 12"/></svg>
    </c:when>

    <c:when test="${name eq 'check-circle'}">
        <c:set var="s" value="${empty size ? 20 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><circle cx="12" cy="12" r="10" fill="currentColor"/><path d="M7.5 12.5l3 3 6-6" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
    </c:when>

    <c:when test="${name eq 'search'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
    </c:when>

    <c:when test="${name eq 'filter'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><line x1="4" y1="6" x2="20" y2="6"/><line x1="8" y1="12" x2="16" y2="12"/><line x1="10" y1="18" x2="14" y2="18"/></svg>
    </c:when>

    <c:when test="${name eq 'options'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><line x1="4" y1="7" x2="10" y2="7"/><line x1="14" y1="7" x2="20" y2="7"/><circle cx="12" cy="7" r="2"/><line x1="4" y1="12" x2="5" y2="12"/><line x1="9" y1="12" x2="20" y2="12"/><circle cx="7" cy="12" r="2"/><line x1="4" y1="17" x2="13" y2="17"/><line x1="17" y1="17" x2="20" y2="17"/><circle cx="15" cy="17" r="2"/></svg>
    </c:when>

    <c:when test="${name eq 'sort'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M3 6h18M7 12h10M11 18h2"/></svg>
    </c:when>

    <c:when test="${name eq 'more-vertical'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 18 18" fill="currentColor" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><circle cx="9" cy="4" r="1.6"/><circle cx="9" cy="9" r="1.6"/><circle cx="9" cy="14" r="1.6"/></svg>
    </c:when>

    <c:when test="${name eq 'visibility-off'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M3 3l18 18"/><path d="M10.6 10.6A2 2 0 0 0 13.4 13.4"/><path d="M9.9 4.2A10.8 10.8 0 0 1 12 4c5.2 0 8.8 4.2 10 8-.5 1.6-1.5 3.2-2.8 4.5"/><path d="M6.6 6.7C4.4 8.1 2.8 10.2 2 12c1.2 3.8 4.8 8 10 8 1.8 0 3.4-.5 4.8-1.3"/></svg>
    </c:when>

    <c:when test="${name eq 'plus'}">
        <c:set var="s" value="${empty size ? 24 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M12 5v14"/><path d="M5 12h14"/></svg>
    </c:when>

    <%-- User & identity --%>
    <c:when test="${name eq 'user-avatar'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><circle cx="12" cy="8" r="4"/><path d="M4 21c1.6-4 4.2-6 8-6s6.4 2 8 6"/></svg>
    </c:when>

    <%-- Cars & catalog --%>
    <c:when test="${name eq 'car-placeholder'}">
        <c:set var="s" value="${empty size ? 48 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><rect x="1" y="3" width="15" height="13" rx="1"/><path d="M16 8h4l3 5v3h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
    </c:when>

    <c:when test="${name eq 'car'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M6.2 11.4A2 2 0 0 1 8.08 10h7.84a2 2 0 0 1 1.88 1.4L19 15H5l1.2-3.6Z"/><path d="M4 15h16v2.2a.8.8 0 0 1-.8.8h-.8a2.2 2.2 0 0 1-4.4 0H10a2.2 2.2 0 0 1-4.4 0h-.8a.8.8 0 0 1-.8-.8V15Z"/><path d="M7.5 12.5h1.5"/><path d="M15 12.5h1.5"/></svg>
    </c:when>

    <c:when test="${name eq 'tag'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M10.5 20H7a2 2 0 0 1-2-2v-3.2a2 2 0 0 1 .59-1.41l6.8-6.8a2 2 0 0 1 2.82 0l2.79 2.79a2 2 0 0 1 0 2.82l-6.8 6.8A2 2 0 0 1 10.5 20Z"/><path d="m13.5 8.5 2 2"/></svg>
    </c:when>

    <c:when test="${name eq 'image-upload'}">
        <c:set var="s" value="${empty size ? 28 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 28 28" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M5.25 19.25L9.8 14.7a2 2 0 0 1 2.8 0l1.05 1.05 2.9-2.9a2 2 0 0 1 2.8 0L22.75 16.25"/><rect x="4.5" y="5.25" width="19" height="17.5" rx="3"/><circle cx="18.75" cy="9.75" r="1.75"/></svg>
    </c:when>

    <%-- Spec icons --%>
    <c:when test="${name eq 'bolt'}">
        <c:set var="s" value="${empty size ? 20 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
    </c:when>

    <c:when test="${name eq 'speedometer'}">
        <c:set var="s" value="${empty size ? 11 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M12 12m-10 0a10 10 0 1 0 20 0a10 10 0 1 0-20 0"/><path d="M12 12l4.5-4.5"/><path d="M12 7v1"/><path d="M17 12h-1"/></svg>
    </c:when>

    <c:when test="${name eq 'droplet'}">
        <c:set var="s" value="${empty size ? 11 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M12 22a7 7 0 0 0 7-7c0-2-1-3.9-3-5.5s-3.5-4-4-6.5c-.5 2.5-2 4.9-4 6.5C6 11.1 5 13 5 15a7 7 0 0 0 7 7z"/></svg>
    </c:when>

    <c:when test="${name eq 'shield'}">
        <c:set var="s" value="${empty size ? 11 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
    </c:when>

    <c:when test="${name eq 'dollar'}">
        <c:set var="s" value="${empty size ? 11 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
    </c:when>

    <c:when test="${name eq 'star-filled'}">
        <c:set var="s" value="${empty size ? 12 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M12 2.75l2.91 5.9 6.51.95-4.71 4.59 1.11 6.48L12 17.62l-5.82 3.05 1.11-6.48-4.71-4.59 6.51-.95L12 2.75z"/></svg>
    </c:when>

    <%-- Fuel types --%>
    <c:when test="${name eq 'eco'}">
        <c:set var="s" value="${empty size ? 20 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10z"/><path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/></svg>
    </c:when>

    <c:when test="${name eq 'gas-pump'}">
        <c:set var="s" value="${empty size ? 20 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><line x1="3" x2="15" y1="22" y2="22"/><line x1="4" x2="14" y1="9" y2="9"/><path d="M14 22V4a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v18"/><path d="M14 13h2a2 2 0 0 1 2 2v2a2 2 0 0 0 2 2 2 2 0 0 0 2-2V9.83a2 2 0 0 0-.59-1.42L18 5"/></svg>
    </c:when>

    <%-- Reactions --%>
    <c:when test="${name eq 'heart'}">
        <c:set var="s" value="${empty size ? 18 : size}"/>
        <svg width="${s}" height="${s}" viewBox="0 0 24 24" aria-hidden="true" focusable="false" class="${fn:escapeXml(cssClass)}"><path d="M12 21s-6.8-4.2-9.5-8.4C0.6 9.5 2.4 5 6.2 5c2 0 3.5 1 4.4 2.3C11.5 6 13 5 15 5c3.8 0 5.6 4.5 3.7 7.6C16.8 16.8 12 21 12 21z"/></svg>
    </c:when>

    <c:otherwise><!-- icon: unknown name "${fn:escapeXml(name)}" --></c:otherwise>

</c:choose>
