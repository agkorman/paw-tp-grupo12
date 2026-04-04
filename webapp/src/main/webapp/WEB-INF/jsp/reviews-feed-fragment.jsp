<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<pa:reviews-feed reviews="${reviews}" carId="${selectedCar.id}" currentSort="${currentSort}"/>
