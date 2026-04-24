<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<pa:cars-content
        cars="${cars}"
        resultCount="${fn:length(cars)}"
        reviewStatsByCarId="${reviewStatsByCarId}"
        favoritedCarIds="${favoritedCarIds}"
        showHp="${showHp}"
        showSpeed="${showSpeed}"
        showConsumption="${showConsumption}"
        showAirbags="${showAirbags}"
        showTransmission="${showTransmission}"
        showFuelType="${showFuelType}"/>
