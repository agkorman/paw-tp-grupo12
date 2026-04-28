package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ReviewTag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewTagDao {
    List<ReviewTag> findAll();

    Optional<ReviewTag> findById(short tagId);

    List<ReviewTag> findByIds(Collection<Short> tagIds);

    /**
     * Replace the tag set for a review: deletes existing assignments then inserts the new ones.
     * Caller is responsible for the transactional boundary.
     */
    void replaceAssignments(long reviewId, Collection<Short> tagIds);

    Map<Long, List<ReviewTag>> findByReviewIds(Collection<Long> reviewIds);

    /**
     * For each provided car id, returns a map of {@code tagId -> mention count}
     * across the reviews of that car. Cars without any tagged review are absent
     * from the outer map; tags with zero mentions are absent from the inner map.
     */
    Map<Long, Map<Short, Integer>> getTagCountsForCars(Collection<Long> carIds);
}
