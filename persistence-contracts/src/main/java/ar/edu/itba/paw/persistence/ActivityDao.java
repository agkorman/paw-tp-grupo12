package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Page;

public interface ActivityDao {
    Page<ActivityFeedReference> findLatest(int page);
}
