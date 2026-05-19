package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

final class TestModels {

    private TestModels() {}

    static User user(final long id, final String username, final String email, final String password,
                     final String role, final LocalDateTime createdAt) {
        final User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setPreferredLocale("es");
        user.setCreatedAt(createdAt);
        return user;
    }

    static Brand brand(final long id, final String name, final LocalDateTime createdAt) {
        final Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        brand.setCreatedAt(createdAt);
        return brand;
    }

    static BodyType bodyType(final long id, final String name, final LocalDateTime createdAt) {
        final BodyType bodyType = new BodyType();
        bodyType.setId(id);
        bodyType.setName(name);
        bodyType.setCreatedAt(createdAt);
        return bodyType;
    }

    static Car car(final long id, final long brandId, final String brandName, final String model,
                   final long bodyTypeId, final Integer year, final String bodyType, final String description,
                   final LocalDateTime createdAt, final boolean hasImage, final String fuelType,
                   final Integer horsepower, final Integer airbagCount, final String transmission,
                   final BigDecimal fuelConsumption, final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        final Car car = new Car();
        car.setId(id);
        car.setBrand(brand(brandId, brandName, null));
        car.setModel(model);
        car.setBodyTypeEntity(bodyType(bodyTypeId, bodyType, null));
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
        final CarRequest request = new CarRequest();
        request.setId(id);
        request.setSubmittedByUser(submittedByUserId == null ? null : user(submittedByUserId, null, submitterEmail, null, null, null));
        request.setSubmitterEmail(submitterEmail);
        request.setBrand(brand(brandId, null, null));
        request.setBodyType(bodyType(bodyTypeId, null, null));
        request.setYear(year);
        request.setModel(model);
        request.setDescription(description);
        request.setImageContentType(imageContentType);
        request.setImageData(imageData);
        request.setStatus(status);
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

    static Review review(final long id, final Long userId, final String reviewerEmail, final String reviewerUsername,
                         final long carId, final BigDecimal rating, final String title, final String body,
                         final String ownershipStatus, final Integer modelYear, final Integer mileageKm,
                         final Boolean wouldRecommend, final LocalDateTime createdAt,
                         final LocalDateTime updatedAt) {
        final Review review = new Review();
        review.setId(id);
        review.setUser(userId == null ? null : user(userId, reviewerUsername, reviewerEmail, null, null, null));
        review.setReviewerEmail(reviewerEmail);
        final Car car = new Car();
        car.setId(carId);
        review.setCar(car);
        review.setRating(rating);
        review.setTitle(title);
        review.setBody(body);
        review.setOwnershipStatus(ownershipStatus);
        review.setModelYear(modelYear);
        review.setMileageKm(mileageKm);
        review.setWouldRecommend(wouldRecommend);
        review.setCreatedAt(createdAt);
        review.setUpdatedAt(updatedAt);
        return review;
    }

    static CarImage carImage(final long imageId, final long carId, final int displayOrder,
                             final String contentType, final byte[] imageData, final LocalDateTime updatedAt) {
        final CarImage image = new CarImage();
        image.setImageId(imageId);
        final Car car = new Car();
        car.setId(carId);
        image.setCar(car);
        image.setDisplayOrder(displayOrder);
        image.setContentType(contentType);
        image.setImageData(imageData);
        image.setUpdatedAt(updatedAt);
        return image;
    }

    static CarRequestImage carRequestImage(final long imageId, final long requestId, final int displayOrder,
                                           final String contentType, final byte[] imageData,
                                           final LocalDateTime updatedAt) {
        final CarRequestImage image = new CarRequestImage();
        image.setImageId(imageId);
        final CarRequest request = new CarRequest();
        request.setId(requestId);
        image.setRequest(request);
        image.setDisplayOrder(displayOrder);
        image.setContentType(contentType);
        image.setImageData(imageData);
        image.setUpdatedAt(updatedAt);
        return image;
    }
}
