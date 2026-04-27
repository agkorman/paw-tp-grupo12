package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewTagServiceImpl implements ReviewTagService {

    private final ReviewTagDao reviewTagDao;

    @Autowired
    public ReviewTagServiceImpl(final ReviewTagDao reviewTagDao) {
        this.reviewTagDao = reviewTagDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewTag> getAll() {
        return reviewTagDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<ReviewTag>> getAllGroupedBySentiment() {
        final Map<String, List<ReviewTag>> grouped = new LinkedHashMap<>();
        grouped.put(ReviewTag.SENTIMENT_POSITIVE, new java.util.ArrayList<>());
        grouped.put(ReviewTag.SENTIMENT_NEGATIVE, new java.util.ArrayList<>());
        for (final ReviewTag tag : reviewTagDao.findAll()) {
            grouped.computeIfAbsent(tag.getSentiment(), k -> new java.util.ArrayList<>()).add(tag);
        }
        return grouped;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewTag> validateSelection(final Collection<Short> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        final Set<Short> uniqueIds = new HashSet<>(tagIds);
        if (uniqueIds.size() > MAX_TAGS_PER_REVIEW) {
            throw new InvalidReviewTagSelectionException(
                    InvalidReviewTagSelectionException.Reason.TOO_MANY,
                    "Podés elegir hasta " + MAX_TAGS_PER_REVIEW + " etiquetas.");
        }
        final List<ReviewTag> resolved = reviewTagDao.findByIds(uniqueIds);
        if (resolved.size() != uniqueIds.size()) {
            throw new InvalidReviewTagSelectionException(
                    InvalidReviewTagSelectionException.Reason.UNKNOWN_TAG,
                    "Una de las etiquetas seleccionadas no es válida.");
        }
        final Set<String> dimensions = resolved.stream()
                .map(ReviewTag::getDimension)
                .collect(Collectors.toSet());
        if (dimensions.size() != resolved.size()) {
            throw new InvalidReviewTagSelectionException(
                    InvalidReviewTagSelectionException.Reason.DUPLICATE_DIMENSION,
                    "No podés elegir dos etiquetas opuestas para la misma característica.");
        }
        return resolved;
    }
}
