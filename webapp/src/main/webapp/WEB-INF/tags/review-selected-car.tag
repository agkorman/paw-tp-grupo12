<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="selectedCar" required="true" type="ar.edu.itba.paw.model.Car" %>
<%@ attribute name="carImages" required="false" type="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<article class="selected-car-panel">
    <div class="selected-car-header">
        <h2>Imagen del auto</h2>
        <pa:car-favorite-button carId="${selectedCar.id}" favorited="${selectedCar.id mod 2 eq 0}" label="Favorito"/>
    </div>
    <div class="selected-car-image">
        <pa:car-image-carousel carId="${selectedCar.id}" model="${selectedCar.model}" images="${carImages}"/>
    </div>
</article>
