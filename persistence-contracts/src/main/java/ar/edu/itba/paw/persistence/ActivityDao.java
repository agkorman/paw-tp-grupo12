package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Page;

public interface ActivityDao {
    Page<ActivityFeedReference> findFeed(ActivityFeedCriteria criteria);
}
