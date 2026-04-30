package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface ReviewService {
    Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
                        String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                        Collection<Short> tagIds);
    List<Review> getAllReviews();
    Optional<Review> getReviewById(long id);
    List<Review> getReviewsByIds(Collection<Long> ids);
    Optional<Review> updateReview(long id, long carId, BigDecimal rating, String title, String body,
                                  String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                                  Collection<Short> tagIds);
    boolean deleteReview(long id);
    List<Review> getReviewsByCar(long carId);
    Page<Review> getReviewsByCar(long carId, int page);
    default Optional<Integer> getDefaultPageForReview(final long carId, final long reviewId) {
        return Optional.ofNullable(getDefaultPagesForReviews(getReviewsByCar(carId)).get(reviewId));
    }

    default Map<Long, Integer> getDefaultPagesForReviews(final Collection<Review> reviews) {
        final Map<Long, Integer> pagesByReviewId = new HashMap<>();
        if (reviews == null || reviews.isEmpty()) {
            return pagesByReviewId;
        }

        final Map<Long, Integer> positionsByCarId = new HashMap<>();
        reviews.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(
                                Review::getCreatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                        .thenComparing(Review::getId, Comparator.reverseOrder()))
                .forEach(review -> {
                    final int position = positionsByCarId.getOrDefault(review.getCarId(), 0);
                    pagesByReviewId.put(review.getId(), (position / Pagination.REVIEWS_PAGE_SIZE)
                            + Pagination.DEFAULT_PAGE);
                    positionsByCarId.put(review.getCarId(), position + 1);
                });
        return pagesByReviewId;
    }
    Optional<Review> getLatestReviewByCar(long carId);
    Optional<Review> getTopRatedLatestReviewByCar(long carId);
    List<Review> getReviewsByCarOrderByRatingAsc(long carId);
    Page<Review> getReviewsByCarOrderByRatingAsc(long carId, int page);
    List<Review> getReviewsByCarOrderByRatingDesc(long carId);
    Page<Review> getReviewsByCarOrderByRatingDesc(long carId, int page);
    List<Review> getReviewsByUser(long userId);
    default Page<Review> getReviewsByUser(final long userId, final int page) {
        final List<Review> reviews = getReviewsByUser(userId);
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        if (reviews.isEmpty()) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), reviews.size(), pageSize);
        final int fromIndex = (int) Pagination.offsetFor(effectivePage, pageSize);
        final int toIndex = Math.min(fromIndex + pageSize, reviews.size());
        return new Page<>(reviews.subList(fromIndex, toIndex), effectivePage, pageSize, reviews.size());
    }

    default long countReviewsByUser(final long userId) {
        return getReviewsByUser(userId).size();
    }
    Optional<ReviewStats> getReviewStatsByCar(long carId);
    List<ReviewStats> getReviewStatsByCarIds(Collection<Long> carIds);
}
