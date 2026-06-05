package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.ActivityFeedPermissions;
import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Page;
import java.util.Collection;
import java.util.Map;

public interface ActivityService {
    Page<ActivityFeedItem> getActivityFeed(ActivityFeedCriteria criteria);
    Map<ActivityFeedReference, ActivityFeedPermissions> getActivityFeedPermissions(
            Collection<ActivityFeedItem> items,
            Long viewerUserId,
            boolean viewerAdmin
    );
}
