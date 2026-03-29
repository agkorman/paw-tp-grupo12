<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reviews | La Posta Autos</title>
    <link rel="icon" href="<c:url value='/favicon.ico'/>">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/css/design-system.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/layout.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/components.css'/>">
    <link rel="stylesheet" href="<c:url value='/css/reviews.css'/>">
</head>
<body>
    <pa:nav activePage="explore"/>

    <main class="reviews-page">
        <section class="review-hero">
            <div>
                <p class="eyebrow">Community Garage</p>
                <h1>Write a new review</h1>
                <p class="subtitle">Pick a vehicle, preview it as a card, and submit your ownership experience.</p>
            </div>
        </section>

        <section class="review-layout">
            <c:choose>
                <c:when test="${empty cars}">
                    <div class="empty-state">
                        <p>No cars available to review yet.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="review-form-panel">
                        <h2>Select a car</h2>
                        <div class="car-picker">
                            <c:forEach var="car" items="${cars}" varStatus="status">
                                <button type="button"
                                        class="car-option ${status.first ? 'selected' : ''}"
                                        data-car-id="${car.id}">
                                    <span class="car-option-model"><c:out value="${car.model}"/></span>
                                    <span class="car-option-meta"><c:out value="${car.generation}"/></span>
                                </button>
                            </c:forEach>
                        </div>

                        <form action="<c:url value='/reviews'/>" method="post" class="review-form">
                            <input id="carId" name="carId" type="hidden" value="${cars[0].id}">

                            <div class="form-grid">
                                <div class="field">
                                    <label for="userId">User ID</label>
                                    <input id="userId" name="userId" type="number" value="1" required>
                                </div>
                                <div class="field">
                                    <label for="rating">Rating (0.0 - 5.0)</label>
                                    <input id="rating" name="rating" type="number" min="0" max="5" step="0.1" required>
                                </div>
                                <div class="field field-wide">
                                    <label for="title">Title</label>
                                    <input id="title" name="title" type="text" maxlength="200" required>
                                </div>
                                <div class="field field-wide">
                                    <label for="body">Body</label>
                                    <textarea id="body" name="body" rows="5" required></textarea>
                                </div>
                                <div class="field">
                                    <label for="ownershipStatus">Ownership Status</label>
                                    <input id="ownershipStatus" name="ownershipStatus" type="text">
                                </div>
                                <div class="field">
                                    <label for="modelYear">Model Year</label>
                                    <input id="modelYear" name="modelYear" type="number">
                                </div>
                                <div class="field">
                                    <label for="mileageKm">Mileage (km)</label>
                                    <input id="mileageKm" name="mileageKm" type="number">
                                </div>
                                <div class="field">
                                    <label for="wouldRecommend">Would Recommend</label>
                                    <select id="wouldRecommend" name="wouldRecommend">
                                        <option value="">Not specified</option>
                                        <option value="true">Yes</option>
                                        <option value="false">No</option>
                                    </select>
                                </div>
                            </div>
                            <button type="submit" class="btn-primary">Publish Review</button>
                        </form>
                    </div>

                    <aside class="selected-car-panel">
                        <h2>Selected car card</h2>
                        <div class="selected-card-stack">
                            <c:forEach var="car" items="${cars}" varStatus="status">
                                <div class="selected-card ${status.first ? 'active' : ''}" data-preview-id="${car.id}">
                                    <pa:car-card
                                            model="${car.model}"
                                            generation="${car.generation}"
                                            bodyType="${car.bodyType}"
                                            imageUrl="${car.imageUrl}"/>
                                </div>
                            </c:forEach>
                        </div>
                    </aside>
                </c:otherwise>
            </c:choose>
        </section>

        <section class="reviews-feed">
            <div class="feed-header">
                <h2>All reviews</h2>
                <span class="count-label">${fn:length(reviews)} entries</span>
            </div>
            <c:choose>
                <c:when test="${empty reviews}">
                    <div class="empty-state">
                        <p>No reviews yet. Be the first to post one.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="review-list">
                        <c:forEach var="review" items="${reviews}">
                            <article class="review-item">
                                <div class="review-item-top">
                                    <strong><c:out value="${review.title}"/></strong>
                                    <span class="rating-pill">${review.rating}/5.0</span>
                                </div>
                                <p class="review-body"><c:out value="${review.body}"/></p>
                                <div class="review-meta">
                                    <span>Car #${review.carId}</span>
                                    <span>User #${review.userId}</span>
                                    <span>${review.createdAt}</span>
                                </div>
                            </article>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>

    <pa:footer/>

    <script>
        (function () {
            const hiddenCarIdInput = document.getElementById('carId');
            const options = Array.from(document.querySelectorAll('.car-option'));
            const previews = Array.from(document.querySelectorAll('.selected-card'));
            if (!hiddenCarIdInput || options.length === 0 || previews.length === 0) {
                return;
            }

            const setSelected = function (carId) {
                hiddenCarIdInput.value = carId;

                options.forEach(function (option) {
                    option.classList.toggle('selected', option.dataset.carId === carId);
                });

                previews.forEach(function (preview) {
                    preview.classList.toggle('active', preview.dataset.previewId === carId);
                });
            };

            options.forEach(function (option) {
                option.addEventListener('click', function () {
                    setSelected(option.dataset.carId);
                });
            });

            setSelected(hiddenCarIdInput.value);
        })();
    </script>
</body>
</html>
