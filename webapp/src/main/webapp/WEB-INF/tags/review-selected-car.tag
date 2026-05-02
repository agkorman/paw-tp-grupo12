<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="selectedCar" required="true" type="ar.edu.itba.paw.model.Car" %>
<%@ attribute name="carImages" required="false" type="java.util.List" %>
<%@ attribute name="favorited" required="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<spring:message var="favoriteLabel" code="review.selectedCar.favorite"/>

<article class="selected-car-panel">
    <div class="selected-car-header">
        <h2><spring:message code="review.selectedCar.imageTitle"/></h2>
        <pa:car-favorite-button carId="${selectedCar.id}" favorited="${favorited}" label="${favoriteLabel}"/>
    </div>
    <div class="selected-car-image">
        <pa:car-image-carousel carId="${selectedCar.id}" model="${selectedCar.model}" images="${carImages}"/>
    </div>
</article>
