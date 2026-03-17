<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="name"        required="true" %>
<%@ attribute name="category"    required="true" %>
<%@ attribute name="imageUrl"    required="true" %>
<%@ attribute name="rating"      required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    .car-card {
        background-color: #1a2332;
        color: #ffffff;
        border-radius: 14px;
        width: 300px;
        overflow: hidden;
        box-shadow: 0 6px 24px rgba(0, 0, 0, 0.5);
        font-family: 'Segoe UI', Arial, sans-serif;
        display: inline-block;
        vertical-align: top;
        margin: 10px;
    }

    .car-card__image-wrapper {
        position: relative;
        height: 165px;
        overflow: hidden;
    }

    .car-card__image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    .car-card__name {
        position: absolute;
        top: 14px;
        left: 16px;
        margin: 0;
        font-size: 1.25rem;
        font-weight: 700;
        color: #ffffff;
        text-shadow: 0 1px 6px rgba(0, 0, 0, 0.7);
        z-index: 1;
    }

    .car-card__details {
        padding: 14px 16px 10px;
        border-bottom: 1px solid #2a3548;
    }

    .car-card__category {
        margin: 0 0 10px;
        font-size: 0.88rem;
        color: #9aaec8;
        display: flex;
        align-items: center;
        gap: 6px;
    }

    .car-card__category svg {
        flex-shrink: 0;
    }

    .car-card__stat {
        margin: 5px 0;
        font-size: 0.9rem;
        color: #c8d4e4;
    }

    .car-card__stat strong {
        color: #5bb8f5;
    }

    .car-card__footer {
        display: flex;
        justify-content: space-around;
        align-items: center;
        padding: 12px 16px;
    }

    .car-card__footer-item {
        display: flex;
        align-items: center;
        gap: 5px;
        font-size: 0.88rem;
        color: #c8d4e4;
        font-weight: 500;
    }

    .car-card__footer-divider {
        width: 1px;
        height: 20px;
        background-color: #2a3548;
    }

    .car-card__footer-item svg {
        flex-shrink: 0;
    }
</style>

<div class="car-card">

    <div class="car-card__image-wrapper">
        <h2 class="car-card__name"><c:out value="${name}" /></h2>
        <img class="car-card__image" src="${imageUrl}" alt="${name}" />
    </div>

    <div class="car-card__footer">
        <span class="car-card__footer-item">
            <!-- Car icon -->
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9aaec8" stroke-width="2"
                 stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 17H3a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1l2-3h10l2 3h1a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2h-2"/>
                <circle cx="7.5" cy="17" r="2.5"/>
                <circle cx="16.5" cy="17" r="2.5"/>
            </svg>
            <c:out value="${category}" />
        </span>

        <span class="car-card__footer-item">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="#f5c518" stroke="#f5c518" stroke-width="1">
            // estrella
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/> 
            </svg>
            <c:out value="${rating}" />
        </span>
    </div>

</div>
