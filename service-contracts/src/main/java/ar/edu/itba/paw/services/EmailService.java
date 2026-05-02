package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;

import java.util.List;

public interface EmailService {
    void sendNewCarRequestNotification(CarRequest request, String brandName, String bodyTypeName);
    void sendCarApprovedNotification(String recipientEmail, String brandName, String model);
    void sendReviewHiddenNotification(String recipientEmail, String subject, String heading, String intro,
                                      String reviewLabel, String carLabel, String reasonLabel,
                                      String reviewTitle, String carName, String moderatorReason);
    void sendWeeklyModeratorDigest(List<String> moderatorEmails, int pendingRequestCount);
    void sendWeeklyUserDigest(String recipientEmail, String username,
                              List<ReviewActivityItem> reviewActivity,
                              List<FavoriteActivityItem> favoriteActivity);

    final class ReviewActivityItem {
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

    final class FavoriteActivityItem {
        public final String carName;
        public final long newReviewCount;

        public FavoriteActivityItem(final String carName, final long newReviewCount) {
            this.carName = carName;
            this.newReviewCount = newReviewCount;
        }
    }
}
