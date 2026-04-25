<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>

<pa:reviews-feed reviews="${reviews}" reviewThreads="${reviewThreads}" carId="${selectedCar.id}"
                 currentSort="${currentSort}"
                 currentPage="${currentPage}" totalPages="${totalPages}" totalItems="${totalItems}"/>
