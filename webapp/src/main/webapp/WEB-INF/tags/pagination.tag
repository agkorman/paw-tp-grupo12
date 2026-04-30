<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="currentPage" required="true" type="java.lang.Integer" %>
<%@ attribute name="totalPages" required="true" type="java.lang.Integer" %>
<%@ attribute name="baseUrl" required="true" type="java.lang.String" %>
<%@ attribute name="extraParams" required="false" type="java.util.Map" %>
<%@ attribute name="pageParam" required="false" type="java.lang.String" %>
<%@ attribute name="fragmentUrl" required="false" type="java.lang.String" %>
<%@ attribute name="target" required="false" type="java.lang.String" %>
<%@ attribute name="ariaLabel" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${totalPages > 1}">
    <c:set var="resolvedPageParam" value="${empty pageParam ? 'page' : pageParam}"/>
    <c:set var="safeCurrentPage" value="${currentPage < 1 ? 1 : currentPage}"/>
    <c:if test="${safeCurrentPage > totalPages}"><c:set var="safeCurrentPage" value="${totalPages}"/></c:if>
    <c:set var="windowSize" value="2"/>
    <c:set var="windowStart" value="${safeCurrentPage - windowSize}"/>
    <c:set var="windowEnd" value="${safeCurrentPage + windowSize}"/>
    <c:if test="${windowStart < 1}"><c:set var="windowStart" value="1"/></c:if>
    <c:if test="${windowEnd > totalPages}"><c:set var="windowEnd" value="${totalPages}"/></c:if>

    <nav class="pagination" aria-label="${empty ariaLabel ? 'Paginación' : ariaLabel}">
        <ul class="pagination-list">
            <c:set var="prevPage" value="${safeCurrentPage - 1}"/>
            <li class="pagination-item ${safeCurrentPage <= 1 ? 'is-disabled' : ''}">
                <c:choose>
                    <c:when test="${safeCurrentPage <= 1}">
                        <span class="pagination-link is-disabled" aria-disabled="true">«</span>
                    </c:when>
                    <c:otherwise>
                        <c:url var="prevHref" value="${baseUrl}">
                            <c:if test="${not empty extraParams}">
                                <c:forEach var="entry" items="${extraParams}">
                                    <c:if test="${not empty entry.value}">
                                        <c:param name="${entry.key}" value="${entry.value}"/>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                            <c:param name="${resolvedPageParam}" value="${prevPage}"/>
                        </c:url>
                        <a class="pagination-link" href="${prevHref}" rel="prev"
                           <c:if test="${not empty fragmentUrl}">data-fragment-url="${fragmentUrl}"</c:if>
                           <c:if test="${not empty target}">data-target="${target}"</c:if>
                           data-pagination-link="true"
                           aria-label="Anterior">«</a>
                    </c:otherwise>
                </c:choose>
            </li>

            <c:if test="${windowStart > 1}">
                <li class="pagination-item">
                    <c:url var="firstHref" value="${baseUrl}">
                        <c:if test="${not empty extraParams}">
                            <c:forEach var="entry" items="${extraParams}">
                                <c:if test="${not empty entry.value}">
                                    <c:param name="${entry.key}" value="${entry.value}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <c:param name="${resolvedPageParam}" value="1"/>
                    </c:url>
                    <a class="pagination-link" href="${firstHref}"
                       <c:if test="${not empty fragmentUrl}">data-fragment-url="${fragmentUrl}"</c:if>
                       <c:if test="${not empty target}">data-target="${target}"</c:if>
                       data-pagination-link="true">1</a>
                </li>
                <c:if test="${windowStart > 2}">
                    <li class="pagination-item pagination-ellipsis" aria-hidden="true"><span>…</span></li>
                </c:if>
            </c:if>

            <c:forEach var="p" begin="${windowStart}" end="${windowEnd}">
                <li class="pagination-item ${p == safeCurrentPage ? 'is-current' : ''}">
                    <c:choose>
                        <c:when test="${p == safeCurrentPage}">
                            <span class="pagination-link is-current" aria-current="page">${p}</span>
                        </c:when>
                        <c:otherwise>
                            <c:url var="pHref" value="${baseUrl}">
                                <c:if test="${not empty extraParams}">
                                    <c:forEach var="entry" items="${extraParams}">
                                        <c:if test="${not empty entry.value}">
                                            <c:param name="${entry.key}" value="${entry.value}"/>
                                        </c:if>
                                    </c:forEach>
                                </c:if>
                                <c:param name="${resolvedPageParam}" value="${p}"/>
                            </c:url>
                            <a class="pagination-link" href="${pHref}"
                               <c:if test="${not empty fragmentUrl}">data-fragment-url="${fragmentUrl}"</c:if>
                               <c:if test="${not empty target}">data-target="${target}"</c:if>
                               data-pagination-link="true">${p}</a>
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:forEach>

            <c:if test="${windowEnd < totalPages}">
                <c:if test="${windowEnd < totalPages - 1}">
                    <li class="pagination-item pagination-ellipsis" aria-hidden="true"><span>…</span></li>
                </c:if>
                <li class="pagination-item">
                    <c:url var="lastHref" value="${baseUrl}">
                        <c:if test="${not empty extraParams}">
                            <c:forEach var="entry" items="${extraParams}">
                                <c:if test="${not empty entry.value}">
                                    <c:param name="${entry.key}" value="${entry.value}"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <c:param name="${resolvedPageParam}" value="${totalPages}"/>
                    </c:url>
                    <a class="pagination-link" href="${lastHref}"
                       <c:if test="${not empty fragmentUrl}">data-fragment-url="${fragmentUrl}"</c:if>
                       <c:if test="${not empty target}">data-target="${target}"</c:if>
                       data-pagination-link="true">${totalPages}</a>
                </li>
            </c:if>

            <c:set var="nextPage" value="${safeCurrentPage + 1}"/>
            <li class="pagination-item ${safeCurrentPage >= totalPages ? 'is-disabled' : ''}">
                <c:choose>
                    <c:when test="${safeCurrentPage >= totalPages}">
                        <span class="pagination-link is-disabled" aria-disabled="true">»</span>
                    </c:when>
                    <c:otherwise>
                        <c:url var="nextHref" value="${baseUrl}">
                            <c:if test="${not empty extraParams}">
                                <c:forEach var="entry" items="${extraParams}">
                                    <c:if test="${not empty entry.value}">
                                        <c:param name="${entry.key}" value="${entry.value}"/>
                                    </c:if>
                                </c:forEach>
                            </c:if>
                            <c:param name="${resolvedPageParam}" value="${nextPage}"/>
                        </c:url>
                        <a class="pagination-link" href="${nextHref}" rel="next"
                           <c:if test="${not empty fragmentUrl}">data-fragment-url="${fragmentUrl}"</c:if>
                           <c:if test="${not empty target}">data-target="${target}"</c:if>
                           data-pagination-link="true"
                           aria-label="Siguiente">»</a>
                    </c:otherwise>
                </c:choose>
            </li>
        </ul>
    </nav>
</c:if>
