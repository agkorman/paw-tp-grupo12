package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.Page;

public interface ActivityService {
    Page<ActivityFeedItem> getActivityFeed(ActivityFeedCriteria criteria);
}
