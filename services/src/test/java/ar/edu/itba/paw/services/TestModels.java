package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

final class TestModels {

    private TestModels() {}

    static User user(final long id, final String username, final String email, final String password,
                     final String role, final LocalDateTime createdAt) {
        return user(id, username, email, password, role, "es", createdAt);
    }

    static User user(final long id, final String username, final String email, final String password,
                     final String role, final String preferredLocale, final LocalDateTime createdAt) {
        final User user = new User(username, email, password, role, preferredLocale);
        user.setId(id);
        user.setCreatedAt(createdAt);
        return user;
    }

    static Brand brand(final long id, final String name, final LocalDateTime createdAt) {
        final Brand brand = new Brand(name);
        brand.setId(id);
        brand.setCreatedAt(createdAt);
        return brand;
    }

    static BodyType bodyType(final long id, final String name, final LocalDateTime createdAt) {
        final BodyType bodyType = new BodyType(name);
        bodyType.setId(id);
        bodyType.setCreatedAt(createdAt);
        return bodyType;
    }

    static Car car(final long id, final long brandId, final String brandName, final String model,
                   final long bodyTypeId, final String bodyType, final String description,
                   final LocalDateTime createdAt) {
        return car(id, brandId, brandName, model, bodyTypeId, null, bodyType, description, createdAt, false,
                null, null, null, null, null, null, null);
    }

    static Car car(final long id, final long brandId, final String brandName, final String model,
                   final long bodyTypeId, final Integer year, final String bodyType, final String description,
                   final LocalDateTime createdAt, final boolean hasImage, final String fuelType,
                   final Integer horsepower, final Integer airbagCount, final String transmission,
                   final BigDecimal fuelConsumption, final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        final Car car = new Car(brand(brandId, brandName, null), model, bodyType(bodyTypeId, bodyType, null));
        car.setId(id);
        car.setYear(year);
        car.setDescription(description);
        car.setCreatedAt(createdAt);
        car.setHasImage(hasImage);
        car.setFuelType(fuelType);
        car.setHorsepower(horsepower);
        car.setAirbagCount(airbagCount);
        car.setTransmission(transmission);
        car.setFuelConsumption(fuelConsumption);
        car.setMaxSpeedKmh(maxSpeedKmh);
        car.setPriceUsd(priceUsd);
        return car;
    }

    static CarRequest carRequest(final long id, final Long submittedByUserId, final String submitterEmail,
                                 final long brandId, final long bodyTypeId, final Integer year,
                                 final String model, final String description, final String imageContentType,
                                 final byte[] imageData, final String status, final LocalDateTime createdAt,
                                 final String fuelType, final Integer horsepower, final Integer airbagCount,
                                 final String transmission, final BigDecimal fuelConsumption,
                                 final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        final CarRequest request = new CarRequest(
                brand(brandId, null, null), bodyType(bodyTypeId, null, null), model, description, status);
        request.setId(id);
        request.setSubmittedByUser(submittedByUserId == null ? null : user(submittedByUserId, null, submitterEmail, null, null, null));
        request.setSubmitterEmail(submitterEmail);
        request.setYear(year);
        request.setImageContentType(imageContentType);
        request.setImageData(imageData);
        request.setCreatedAt(createdAt);
        request.setFuelType(fuelType);
        request.setHorsepower(horsepower);
        request.setAirbagCount(airbagCount);
        request.setTransmission(transmission);
        request.setFuelConsumption(fuelConsumption);
        request.setMaxSpeedKmh(maxSpeedKmh);
        request.setPriceUsd(priceUsd);
        return request;
    }

    static BrandRequest brandRequest(final long id, final Long submittedByUserId, final String submitterEmail,
                                     final String name, final String comments, final String status,
                                     final LocalDateTime createdAt) {
        final BrandRequest request = new BrandRequest(name, status);
        request.setId(id);
        request.setSubmittedByUser(submittedByUserId == null ? null : user(submittedByUserId, null, submitterEmail, null, null, null));
        request.setSubmitterEmail(submitterEmail);
        request.setComments(comments);
        request.setCreatedAt(createdAt);
        return request;
    }

    static BodyTypeRequest bodyTypeRequest(final long id, final Long submittedByUserId, final String submitterEmail,
                                           final String name, final String comments, final String status,
                                           final LocalDateTime createdAt) {
        final BodyTypeRequest request = new BodyTypeRequest(name, status);
        request.setId(id);
        request.setSubmittedByUser(submittedByUserId == null ? null : user(submittedByUserId, null, submitterEmail, null, null, null));
        request.setSubmitterEmail(submitterEmail);
        request.setComments(comments);
        request.setCreatedAt(createdAt);
        return request;
    }

    static AdminRequest adminRequest(final long id, final long submittedByUserId, final String submitterEmail,
                                     final String motivation, final String bio, final String justification,
                                     final String status, final LocalDateTime createdAt) {
        final AdminRequest request = new AdminRequest(
                user(submittedByUserId, null, submitterEmail, null, null, null),
                motivation, bio, justification, status);
        request.setId(id);
        request.setSubmitterEmail(submitterEmail);
        request.setCreatedAt(createdAt);
        return request;
    }

    static Review review(final long id, final Long userId, final String reviewerEmail, final long carId,
                         final BigDecimal rating, final String title, final String body,
                         final String ownershipStatus, final Integer modelYear, final Integer mileageKm,
                         final Boolean wouldRecommend, final LocalDateTime createdAt,
                         final LocalDateTime updatedAt) {
        final Car car = new Car(brand(0, null, null), null, bodyType(0, null, null));
        car.setId(carId);
        final Review review = new Review(car, rating, title, body);
        review.setId(id);
        review.setUser(userId == null ? null : user(userId, null, reviewerEmail, null, null, null));
        review.setReviewerEmail(reviewerEmail);
        review.setOwnershipStatus(ownershipStatus);
        review.setModelYear(modelYear);
        review.setMileageKm(mileageKm);
        review.setWouldRecommend(wouldRecommend);
        review.setCreatedAt(createdAt);
        review.setUpdatedAt(updatedAt);
        return review;
    }

    static ReviewReply reviewReply(final long id, final long reviewId, final long userId,
                                   final String authorUsername, final String body,
                                   final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        final Review review = new Review(
                new Car(brand(0, null, null), null, bodyType(0, null, null)), null, null, null);
        review.setId(reviewId);
        final ReviewReply reply = new ReviewReply(review, user(userId, authorUsername, null, null, null, null), body);
        reply.setId(id);
        reply.setCreatedAt(createdAt);
        reply.setUpdatedAt(updatedAt);
        return reply;
    }

    static CarRequestImage carRequestImage(final long imageId, final long requestId, final int displayOrder,
                                           final String contentType, final byte[] imageData,
                                           final LocalDateTime updatedAt) {
        final CarRequest request = new CarRequest(
                brand(0, null, null), bodyType(0, null, null), null, null, null);
        request.setId(requestId);
        final CarRequestImage image = new CarRequestImage(request, displayOrder, contentType, imageData);
        image.setImageId(imageId);
        image.setUpdatedAt(updatedAt);
        return image;
    }
}
