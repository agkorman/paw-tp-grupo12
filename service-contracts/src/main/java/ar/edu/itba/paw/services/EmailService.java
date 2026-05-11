package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.EmailRecipient;

import java.util.List;

public interface EmailService {
    void sendNewCarRequestNotification(CarRequest request, String brandName, String bodyTypeName);
    void sendCarApprovedNotification(String recipientEmail, String brandName, String model, long carId);
    void sendCarRejectedNotification(String recipientEmail, String brandName, String model);
    void sendCatalogRequestApprovedNotification(String recipientEmail, String requestType, String requestedName);
    void sendCatalogRequestRejectedNotification(String recipientEmail, String requestType, String requestedName);
    void sendAdminRequestApprovedNotification(String recipientEmail);
    void sendAdminRequestRejectedNotification(String recipientEmail);
    void sendReviewHiddenNotification(String recipientEmail, String reviewTitle, String carName,
                                      String moderatorReason);
    void sendWeeklyModeratorDigest(List<EmailRecipient> moderatorRecipients, int pendingRequestCount);
    void sendWeeklyUserDigest(String recipientEmail, String username,
                              List<ReviewActivityItem> reviewActivity,
                              List<FavoriteActivityItem> favoriteActivity);

    public static final class ReviewActivityItem {
        public final String reviewTitle;
        public final String carName;
        public final long newLikes;
        public final long newReplies;

        public ReviewActivityItem(final String reviewTitle, final String carName,
                                  final long newLikes, final long newReplies) {
            this.reviewTitle = reviewTitle;
            this.carName = carName;
            this.newLikes = newLikes;
            this.newReplies = newReplies;
        }
    }

    public static final class FavoriteActivityItem {
        public final String carName;
        public final long newReviewCount;

        public FavoriteActivityItem(final String carName, final long newReviewCount) {
            this.carName = carName;
            this.newReviewCount = newReviewCount;
        }
    }
}
