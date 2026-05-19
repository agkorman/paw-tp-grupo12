package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewTagServiceImpl.class);

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
    public Map<String, ReviewTag> getAllByCode() {
        final Map<String, ReviewTag> byCode = new LinkedHashMap<>();
        for (final ReviewTag tag : reviewTagDao.findAll()) {
            byCode.putIfAbsent(tag.getCode(), tag);
        }
        return byCode;
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
            LOGGER.warn("review tag selection rejected: too many tags count={}", uniqueIds.size());
            throw new InvalidReviewTagSelectionException(
                    InvalidReviewTagSelectionException.Reason.TOO_MANY,
                    "Podés elegir hasta " + MAX_TAGS_PER_REVIEW + " etiquetas.");
        }
        final List<ReviewTag> resolved = reviewTagDao.findByIds(uniqueIds);
        if (resolved.size() != uniqueIds.size()) {
            LOGGER.warn("review tag selection rejected: unknown tag selectedCount={} resolvedCount={}",
                    uniqueIds.size(), resolved.size());
            throw new InvalidReviewTagSelectionException(
                    InvalidReviewTagSelectionException.Reason.UNKNOWN_TAG,
                    "Una de las etiquetas seleccionadas no es válida.");
        }
        final Set<String> dimensions = resolved.stream()
                .map(ReviewTag::getDimension)
                .collect(Collectors.toSet());
        if (dimensions.size() != resolved.size()) {
            LOGGER.warn("review tag selection rejected: duplicate dimension");
            throw new InvalidReviewTagSelectionException(
                    InvalidReviewTagSelectionException.Reason.DUPLICATE_DIMENSION,
                    "No podés elegir dos etiquetas opuestas para la misma característica.");
        }
        return resolved;
    }
}
