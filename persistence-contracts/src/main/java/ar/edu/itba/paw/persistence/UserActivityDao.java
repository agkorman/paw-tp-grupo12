package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ProfileActivityItem;

public interface UserActivityDao {

    Page<ProfileActivityItem> findAuthoredActivity(long userId, int page);

    long countAuthoredActivity(long userId);

    Page<ProfileActivityItem> findLikedActivity(long userId, int page);

    long countLikedActivity(long userId);
}
