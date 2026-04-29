<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<pa:cars-content
        cars="${cars}"
        resultCount="${empty totalItems ? fn:length(cars) : totalItems}"
        reviewStatsByCarId="${reviewStatsByCarId}"
        showHp="${showHp}"
        showSpeed="${showSpeed}"
        showConsumption="${showConsumption}"
        showAirbags="${showAirbags}"
        showTransmission="${showTransmission}"
        showFuelType="${showFuelType}"
        showPrice="${showPrice}"
        showYear="${showYear}"
        currentPage="${currentPage}"
        totalPages="${totalPages}"
        criteria="${criteria}"/>
