<%@ tag language="java" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="value" required="true" type="java.time.LocalDateTime" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="paf" uri="http://lapostaautos.com/jsp/functions" %>
<c:if test="${not empty value}"><c:set var="relativeTimeBucket" value="${paf:timeAgo(value)}"/><spring:message code="${relativeTimeBucket.code}" arguments="${relativeTimeBucket.quantity}"/></c:if>
