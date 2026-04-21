<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<pa:cars-content
        cars="${cars}"
        reviewStatsByCarId="${reviewStatsByCarId}"
        showHp="${showHp}"
        showSpeed="${showSpeed}"
        showConsumption="${showConsumption}"
        showAirbags="${showAirbags}"
        showTransmission="${showTransmission}"
        showFuelType="${showFuelType}"/>
