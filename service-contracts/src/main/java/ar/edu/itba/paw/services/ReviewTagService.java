package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ReviewTag;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ReviewTagService {
    int MAX_TAGS_PER_REVIEW = 6;

    List<ReviewTag> getAll();

    /**
     * Returns tags grouped by sentiment ("positive", "negative") for the form view,
     * each list ordered by dimension then label.
     */
    Map<String, List<ReviewTag>> getAllGroupedBySentiment();

    /**
     * Throws {@link ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException}
     * if any id is unknown, the count exceeds {@link #MAX_TAGS_PER_REVIEW}, or two ids
     * resolve to the same dimension. Returns the resolved tag list on success.
     */
    List<ReviewTag> validateSelection(Collection<Short> tagIds);
}
