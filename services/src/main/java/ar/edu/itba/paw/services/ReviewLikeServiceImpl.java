package ar.edu.itba.paw.services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
public class ReviewLikeServiceImpl implements ReviewLikeService {

    @Override
    public Map<Long, Long> countNewLikesPerReview(final long userId, final LocalDateTime since) {
        return Collections.emptyMap();
    }
}
