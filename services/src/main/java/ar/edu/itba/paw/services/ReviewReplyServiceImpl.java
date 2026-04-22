package ar.edu.itba.paw.services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@Service
public class ReviewReplyServiceImpl implements ReviewReplyService {

    @Override
    public Map<Long, Long> countNewRepliesPerReview(final long userId, final LocalDateTime since) {
        return Collections.emptyMap();
    }
}
