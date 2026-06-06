package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ProfileActivityItem;

public interface UserActivityService {

    Page<ProfileActivityItem> getAuthoredActivity(long userId, int page);

    long countAuthoredActivity(long userId);

    Page<ProfileActivityItem> getLikedActivity(long userId, int page);

    long countLikedActivity(long userId);
}
