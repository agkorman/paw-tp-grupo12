package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String APP_NAME = "La Posta Autos";
    private static final String DEFAULT_LOCALE_TAG = "es";
    private static final int DESCRIPTION_PREVIEW_LENGTH = 220;
    private static final String COLOR_SURFACE = "#131313";
    private static final String COLOR_SURFACE_LOW = "#1a1a1a";
    private static final String COLOR_SURFACE_HIGH = "#252525";
    private static final String COLOR_SURFACE_HIGHEST = "#2e2e2e";
    private static final String COLOR_ON_SURFACE = "#e5e2e1";
    private static final String COLOR_ON_SURFACE_VARIANT = "#c4c6cc";
    private static final String COLOR_PRIMARY = "#ffb59e";
    private static final String COLOR_PRIMARY_CONTAINER = "#ff5719";
    private static final String COLOR_ON_PRIMARY = "#1a0800";
    private static final String COLOR_OUTLINE = "#3a3a3a";
    private static final String DISPLAY_FONT = "'Space Grotesk', 'Trebuchet MS', 'Segoe UI', Arial, sans-serif";
    private static final String BODY_FONT = "-apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif";

    private final JavaMailSender mailSender;
    private final UserService userService;
    private final MessageSource messageSource;
    private final String appBaseUrl;

    @Autowired
    public EmailServiceImpl(final JavaMailSender mailSender, final UserService userService,
                            final MessageSource messageSource,
                            @Qualifier("appBaseUrl") final String appBaseUrl) {
        this.mailSender = mailSender;
        this.userService = userService;
        this.messageSource = messageSource;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendNewCarRequestNotification(final CarRequest request, final String brandName,
                                              final String bodyTypeName) {
        final List<EmailRecipient> moderators = getModeratorEmailRecipientsSafely();
        if (moderators.isEmpty()) {
            return;
        }

        final Map<Locale, List<String>> moderatorsByLocale = groupRecipientEmailsByLocale(moderators);
        for (final Map.Entry<Locale, List<String>> entry : moderatorsByLocale.entrySet()) {
            final Locale locale = entry.getKey();
            sendEmail(
                    buildRequestSubject(brandName, request.getModel(), locale),
                    buildRequestPlainTextBody(request, brandName, bodyTypeName, locale),
                    buildRequestHtmlBody(request, brandName, bodyTypeName, locale),
                    "Failed to send car request notification for request " + request.getId(),
                    helper -> helper.setTo(entry.getValue().toArray(new String[0]))
            );
        }
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCarApprovedNotification(final String recipientEmail, final String brandName,
                                            final String model, final long carId) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        final String carUrl = appBaseUrl + "/reviews?carId=" + carId;
        final Locale locale = resolveRecipientLocale(recipientEmail);
        sendEmail(
                buildApprovedSubject(brandName, model, locale),
                buildApprovedPlainTextBody(brandName, model, carUrl, locale),
                buildApprovedHtmlBody(brandName, model, carUrl, locale),
                "Failed to send car approved notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCarRejectedNotification(final String recipientEmail, final String brandName,
                                            final String model) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        final Locale locale = resolveRecipientLocale(recipientEmail);
        sendEmail(
                buildRejectedSubject(brandName, model, locale),
                buildRejectedPlainTextBody(brandName, model, locale),
                buildRejectedHtmlBody(brandName, model, locale),
                "Failed to send car rejected notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCatalogRequestApprovedNotification(final String recipientEmail, final String requestType,
                                                       final String requestedName) {
        sendRequestDecisionNotification(recipientEmail, requestType, requestedName, true);
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCatalogRequestRejectedNotification(final String recipientEmail, final String requestType,
                                                       final String requestedName) {
        sendRequestDecisionNotification(recipientEmail, requestType, requestedName, false);
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendAdminRequestApprovedNotification(final String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        final Locale locale = resolveRecipientLocale(recipientEmail);
        sendRequestDecisionNotification(
                recipientEmail,
                msg("email.requestDecision.type.moderator", locale),
                msg("email.requestDecision.moderatorPermissions", locale),
                true,
                appBaseUrl + "/admin",
                msg("email.action.adminPanel", locale),
                locale
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendAdminRequestRejectedNotification(final String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        final Locale locale = resolveRecipientLocale(recipientEmail);
        sendRequestDecisionNotification(
                recipientEmail,
                msg("email.requestDecision.type.moderator", locale),
                msg("email.requestDecision.moderatorPermissions", locale),
                false,
                appBaseUrl + "/cars",
                msg("common.action.viewCatalog", locale),
                locale
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendReviewHiddenNotification(final String recipientEmail, final String reviewTitle,
                                             final String carName, final String moderatorReason) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        final Locale locale = resolveRecipientLocale(recipientEmail);
        final String subject = msg("review.hide.email.subject", locale);
        final String heading = msg("review.hide.email.heading", locale);
        final String intro = msg("review.hide.email.intro", locale);
        final String reviewLabel = msg("review.hide.email.review", locale);
        final String carLabel = msg("review.hide.email.car", locale);
        final String reasonLabel = msg("review.hide.email.reason", locale);
        sendEmail(
                sanitizeHeaderValue(subject),
                buildReviewHiddenPlainText(heading, intro, reviewLabel, carLabel, reasonLabel,
                        reviewTitle, carName, moderatorReason),
                buildReviewHiddenHtml(heading, intro, reviewLabel, carLabel, reasonLabel,
                        reviewTitle, carName, moderatorReason, locale),
                "Failed to send hidden review notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCommunityPostHiddenNotification(final String recipientEmail, final String communityName,
                                                    final String postTitle, final String moderatorReason,
                                                    final String postUrl) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        final Locale locale = resolveRecipientLocale(recipientEmail);
        sendEmail(
                msg("email.community.postHidden.subject", locale, APP_NAME, sanitizeHeaderValue(communityName)),
                buildCommunityModerationPlainText(
                        msg("email.community.postHidden.heading", locale),
                        msg("email.community.postHidden.intro", locale, APP_NAME),
                        msg("email.community.label.community", locale),
                        communityName,
                        msg("email.community.label.post", locale),
                        postTitle,
                        moderatorReason,
                        absoluteUrl(postUrl),
                        locale
                ),
                buildCommunityModerationHtml(
                        msg("email.community.postHidden.preheader", locale, safeValue(communityName)),
                        msg("email.community.postHidden.heading", locale),
                        msg("email.community.postHidden.intro", locale, APP_NAME),
                        msg("email.community.label.post", locale),
                        safeValue(postTitle),
                        communityName,
                        moderatorReason,
                        absoluteUrl(postUrl),
                        locale
                ),
                "Failed to send hidden community post notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCommunityCommentHiddenNotification(final String recipientEmail, final String communityName,
                                                       final String postTitle, final String commentBody,
                                                       final String moderatorReason, final String postUrl) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        final Locale locale = resolveRecipientLocale(recipientEmail);
        sendEmail(
                msg("email.community.commentHidden.subject", locale, APP_NAME, sanitizeHeaderValue(communityName)),
                buildCommunityModerationPlainText(
                        msg("email.community.commentHidden.heading", locale),
                        msg("email.community.commentHidden.intro", locale, APP_NAME),
                        msg("email.community.label.community", locale),
                        communityName,
                        msg("email.community.label.comment", locale),
                        previewDescription(commentBody, locale),
                        moderatorReason,
                        absoluteUrl(postUrl),
                        locale
                ),
                buildCommunityModerationHtml(
                        msg("email.community.commentHidden.preheader", locale, safeValue(communityName)),
                        msg("email.community.commentHidden.heading", locale),
                        msg("email.community.commentHidden.intro", locale, APP_NAME),
                        msg("email.community.label.comment", locale),
                        previewDescription(commentBody, locale),
                        communityName + " / " + safeValue(postTitle),
                        moderatorReason,
                        absoluteUrl(postUrl),
                        locale
                ),
                "Failed to send hidden community comment notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCommunityMemberKickedNotification(final String recipientEmail, final String communityName,
                                                      final String communityUrl) {
        sendCommunityRoleNotification(
                recipientEmail,
                communityName,
                communityUrl,
                "email.community.memberKicked.subject",
                "email.community.memberKicked.preheader",
                "email.community.memberKicked.heading",
                "email.community.memberKicked.intro",
                "email.action.viewCommunity"
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCommunityModeratorPromotedNotification(final String recipientEmail, final String communityName,
                                                          final String communityMembersUrl) {
        sendCommunityRoleNotification(
                recipientEmail,
                communityName,
                communityMembersUrl,
                "email.community.moderatorPromoted.subject",
                "email.community.moderatorPromoted.preheader",
                "email.community.moderatorPromoted.heading",
                "email.community.moderatorPromoted.intro",
                "email.action.viewMembers"
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCommunityOwnershipTransferredNotification(final String recipientEmail, final String communityName,
                                                             final String communityMembersUrl) {
        sendCommunityRoleNotification(
                recipientEmail,
                communityName,
                communityMembersUrl,
                "email.community.ownershipTransferred.subject",
                "email.community.ownershipTransferred.preheader",
                "email.community.ownershipTransferred.heading",
                "email.community.ownershipTransferred.intro",
                "email.action.viewMembers"
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendWeeklyModeratorDigest(final List<EmailRecipient> moderatorRecipients,
                                          final long pendingRequestCount) {
        if (moderatorRecipients == null || moderatorRecipients.isEmpty()) {
            return;
        }

        final Map<Locale, List<String>> moderatorsByLocale = groupRecipientEmailsByLocale(moderatorRecipients);
        for (final Map.Entry<Locale, List<String>> entry : moderatorsByLocale.entrySet()) {
            final Locale locale = entry.getKey();
            sendEmail(
                    buildModeratorDigestSubject(pendingRequestCount, locale),
                    buildModeratorDigestPlainText(pendingRequestCount, locale),
                    buildModeratorDigestHtml(pendingRequestCount, locale),
                    "Failed to send weekly moderator digest",
                    helper -> helper.setTo(entry.getValue().toArray(new String[0]))
            );
        }
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendWeeklyUserDigest(final String recipientEmail, final String username,
                                     final List<EmailService.ReviewActivityItem> reviewActivity,
                                     final List<EmailService.FavoriteActivityItem> favoriteActivity) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        final Locale locale = resolveRecipientLocale(recipientEmail);
        sendEmail(
                msg("email.userDigest.subject", locale, APP_NAME, sanitizeHeaderValue(safeValue(username))),
                buildUserDigestPlainText(username, reviewActivity, favoriteActivity, locale),
                buildUserDigestHtml(username, reviewActivity, favoriteActivity, locale),
                "Failed to send weekly user digest to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    private void sendEmail(final String subject, final String plainTextBody, final String htmlBody,
                           final String failurePrefix, final EmailCustomization customization) {
        try {
            final MimeMessageHelper helper = newMimeMessageHelper();
            customization.apply(helper);
            helper.setSubject(subject);
            helper.setText(plainTextBody, htmlBody);
            mailSender.send(helper.getMimeMessage());
        } catch (final MessagingException | RuntimeException e) {
            LOGGER.warn("email send failed: {}", failurePrefix, e);
        }
    }

    private MimeMessageHelper newMimeMessageHelper() throws MessagingException {
        final MimeMessage message = mailSender.createMimeMessage();
        return new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
    }

    // ── New request notification helpers ──────────────────────────────────────

    private String buildRequestSubject(final String brandName, final String model, final Locale locale) {
        return msg("email.request.subject", locale, APP_NAME,
                sanitizeHeaderValue(brandName), sanitizeHeaderValue(model));
    }

    private String buildRequestPlainTextBody(final CarRequest request, final String brandName,
                                             final String bodyTypeName, final Locale locale) {
        return msg(
                "email.request.plain",
                locale,
                APP_NAME,
                safeValue(brandName),
                safeValue(request.getModel()),
                request.getYear() == null ? msg("common.empty.na", locale) : request.getYear().toString(),
                safeValue(bodyTypeName),
                request.getImageData() != null && request.getImageData().length > 0
                        ? msg("common.boolean.yes", locale)
                        : msg("common.boolean.no", locale),
                previewDescription(request.getDescription(), locale),
                appBaseUrl + "/admin"
        );
    }

    private String buildRequestHtmlBody(final CarRequest request, final String brandName,
                                        final String bodyTypeName, final Locale locale) {
        final String brand = safeValue(brandName);
        final String model = safeValue(request.getModel());
        final String year = request.getYear() == null ? "" : " " + request.getYear();
        final String carName = escapeHtml(brand + " " + model + year);
        final String bodyType = escapeHtml(safeValue(bodyTypeName));
        final String description = escapeHtml(previewDescription(request.getDescription(), locale)).replace("\n", "<br>");
        final String imageStatus = hasRequestImage(request)
                ? msg("email.request.image.with", locale)
                : msg("email.request.image.without", locale);
        final String dashboardUrl = escapeHtml(appBaseUrl + "/admin");

        final String bodyHtml = buildRequestSummaryCard(carName, bodyType, imageStatus, locale)
                + buildRequestDescriptionBlock(description, locale)
                + buildCenteredAction(dashboardUrl, msg("email.action.dashboard", locale));

        return buildEmailShell(
                escapeHtml(msg("email.request.preheader", locale, brand, model)),
                msg("email.request.title", locale),
                msg("email.request.intro", locale, APP_NAME),
                bodyHtml,
                locale
        );
    }

    private String buildRequestSummaryCard(final String carName, final String bodyType, final String imageStatus,
                                           final Locale locale) {
        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:24px;">
                    %s
                    <div style="font-size:28px;line-height:1.15;font-weight:700;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:8px;font-size:15px;line-height:1.6;color:%s;font-family:%s;">
                        %s
                    </div>
                    <table role="presentation" cellspacing="0" cellpadding="0" style="margin-top:18px;">
                        <tr>
                            <td style="padding:0 10px 10px 0;">
                                %s
                            </td>
                            <td style="padding:0 0 10px 0;">
                                %s
                            </td>
                        </tr>
                    </table>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                buildSectionLabel(msg("email.request.summary", locale)),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                carName,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                bodyType,
                buildPill(msg("email.status.pending", locale), "rgba(255,87,25,0.14)", "rgba(255,181,158,0.16)", COLOR_PRIMARY),
                buildPill(imageStatus, COLOR_SURFACE_HIGHEST, COLOR_OUTLINE, COLOR_ON_SURFACE)
        );
    }

    private String buildRequestDescriptionBlock(final String description, final Locale locale) {
        return """
                %s
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:20px 22px;font-size:15px;line-height:1.7;color:%s;font-family:%s;margin-bottom:28px;">
                    %s
                </div>
                """.formatted(
                buildSectionLabel(msg("email.request.descriptionSubmitted", locale)),
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE,
                BODY_FONT,
                description
        );
    }

    // ── Weekly moderator digest ───────────────────────────────────────────────

    private String buildModeratorDigestSubject(final long pendingCount, final Locale locale) {
        return pendingCount == 0
                ? msg("email.moderatorDigest.subject.empty", locale, APP_NAME)
                : msg("email.moderatorDigest.subject.pending", locale, APP_NAME, pendingCount);
    }

    private String buildModeratorDigestPlainText(final long pendingCount, final Locale locale) {
        if (pendingCount == 0) {
            return msg("email.moderatorDigest.plain.empty", locale, APP_NAME, appBaseUrl + "/admin");
        }
        return msg("email.moderatorDigest.plain.pending", locale, APP_NAME, pendingCount, appBaseUrl + "/admin");
    }

    private String buildModeratorDigestHtml(final long pendingCount, final Locale locale) {
        final boolean hasPending = pendingCount > 0;
        final String preheader = hasPending
                ? escapeHtml(msg("email.moderatorDigest.preheader.pending", locale, pendingCount))
                : escapeHtml(msg("email.moderatorDigest.preheader.empty", locale));
        final String dashboardUrl = escapeHtml(appBaseUrl + "/admin");

        final String bodyHtml = buildModeratorDigestStatusCard(pendingCount, locale)
                + buildCenteredAction(dashboardUrl, msg("email.action.dashboard", locale));

        return buildEmailShell(
                preheader,
                msg("email.moderatorDigest.title", locale),
                msg("email.moderatorDigest.intro", locale),
                bodyHtml,
                locale
        );
    }

    private String buildModeratorDigestStatusCard(final long pendingCount, final Locale locale) {
        if (pendingCount > 0) {
            return """
                    <div style="background:%s;border:1px solid %s;border-radius:18px;padding:28px 24px;margin-bottom:28px;text-align:center;">
                        <div style="font-size:56px;line-height:1;font-weight:700;color:%s;font-family:%s;">
                            %d
                        </div>
                        <div style="margin-top:8px;font-size:16px;line-height:1.5;color:%s;font-family:%s;">
                            %s
                        </div>
                    </div>
                    """.formatted(
                    COLOR_SURFACE_HIGH,
                    COLOR_OUTLINE,
                    COLOR_PRIMARY,
                    DISPLAY_FONT,
                    pendingCount,
                    COLOR_ON_SURFACE_VARIANT,
                    BODY_FONT,
                    escapeHtml(msg("email.moderatorDigest.status.pending", locale, pendingCount))
            );
        }

        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:28px 24px;margin-bottom:28px;text-align:center;">
                    <div style="font-size:40px;line-height:1;margin-bottom:12px;">✓</div>
                    <div style="font-size:16px;line-height:1.5;color:%s;font-family:%s;">
                        %s
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                escapeHtml(msg("email.moderatorDigest.status.empty", locale))
        );
    }

    // ── Weekly user digest ────────────────────────────────────────────────────

    private String buildUserDigestPlainText(final String username,
                                            final List<EmailService.ReviewActivityItem> reviewActivity,
                                            final List<EmailService.FavoriteActivityItem> favoriteActivity,
                                            final Locale locale) {
        final StringBuilder sb = new StringBuilder();
        sb.append(msg("email.userDigest.plain.greeting", locale, safeValue(username))).append("\n\n");
        sb.append(msg("email.userDigest.plain.summary", locale, APP_NAME)).append("\n\n");

        sb.append(msg("email.userDigest.reviewSection", locale)).append("\n");
        if (reviewActivity.isEmpty()) {
            sb.append(msg("email.userDigest.review.empty", locale)).append("\n");
        } else {
            for (final EmailService.ReviewActivityItem item : reviewActivity) {
                sb.append("• ").append(safeValue(item.reviewTitle))
                        .append(" (").append(safeValue(item.carName)).append(")");
                if (item.newLikes > 0) {
                    sb.append(" — ").append(msg("email.userDigest.likes", locale, item.newLikes));
                }
                if (item.newReplies > 0) {
                    sb.append(" — ").append(msg("email.userDigest.replies", locale, item.newReplies));
                }
                sb.append("\n");
            }
        }

        sb.append("\n").append(msg("email.userDigest.favoriteSection", locale)).append("\n");
        if (favoriteActivity.isEmpty()) {
            sb.append(msg("email.userDigest.favorite.empty", locale)).append("\n");
        } else {
            for (final EmailService.FavoriteActivityItem item : favoriteActivity) {
                sb.append("• ").append(safeValue(item.carName))
                        .append(" — ").append(msg("email.userDigest.newReviews", locale, item.newReviewCount))
                        .append("\n");
            }
        }

        sb.append("\n").append(msg("email.userDigest.catalogLink", locale, appBaseUrl + "/cars")).append("\n");
        return sb.toString();
    }

    private String buildUserDigestHtml(final String username,
                                       final List<EmailService.ReviewActivityItem> reviewActivity,
                                       final List<EmailService.FavoriteActivityItem> favoriteActivity,
                                       final Locale locale) {
        final String displayName = safeValue(username);
        final String bodyHtml = buildUserDigestReviewSection(reviewActivity, locale)
                + buildUserDigestFavoriteSection(favoriteActivity, locale)
                + buildCenteredAction(escapeHtml(appBaseUrl + "/cars"), msg("common.action.viewCatalog", locale));

        return buildEmailShell(
                escapeHtml(msg("email.userDigest.preheader", locale, APP_NAME)),
                msg("email.userDigest.heading", locale, displayName),
                msg("email.userDigest.intro", locale),
                bodyHtml,
                locale
        );
    }

    // ── Review moderation notification ────────────────────────────────────────

    private String buildReviewHiddenPlainText(final String heading, final String intro,
                                              final String reviewLabel, final String carLabel,
                                              final String reasonLabel, final String reviewTitle,
                                              final String carName, final String moderatorReason) {
        return """
                %s

                %s

                %s: %s
                %s: %s

                %s:
                %s
                """.formatted(
                safeValue(heading),
                safeValue(intro),
                safeValue(reviewLabel),
                safeValue(reviewTitle),
                safeValue(carLabel),
                safeValue(carName),
                safeValue(reasonLabel),
                safeValue(moderatorReason)
        );
    }

    private String buildReviewHiddenHtml(final String heading, final String intro,
                                         final String reviewLabel, final String carLabel,
                                         final String reasonLabel, final String reviewTitle,
                                         final String carName, final String moderatorReason,
                                         final Locale locale) {
        final String bodyHtml = buildModeratedReviewSummary(reviewLabel, carLabel, reviewTitle, carName)
                + buildModerationReason(reasonLabel, moderatorReason);

        return buildEmailShell(
                escapeHtml(safeValue(intro)),
                safeValue(heading),
                safeValue(intro),
                bodyHtml,
                locale
        );
    }

    private String buildModeratedReviewSummary(final String reviewLabel, final String carLabel,
                                               final String reviewTitle, final String carName) {
        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:24px;">
                    %s
                    <div style="font-size:24px;line-height:1.2;font-weight:700;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:10px;font-size:14px;line-height:1.6;color:%s;font-family:%s;">
                        <strong style="color:%s;">%s:</strong> %s
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                buildSectionLabel(reviewLabel),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                escapeHtml(safeValue(reviewTitle)),
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                COLOR_ON_SURFACE,
                escapeHtml(safeValue(carLabel)),
                escapeHtml(safeValue(carName))
        );
    }

    private String buildModerationReason(final String reasonLabel, final String moderatorReason) {
        return """
                %s
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:20px 22px;font-size:15px;line-height:1.7;color:%s;font-family:%s;white-space:pre-line;">
                    %s
                </div>
                """.formatted(
                buildSectionLabel(reasonLabel),
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE,
                BODY_FONT,
                escapeHtml(safeValue(moderatorReason))
        );
    }

    private String buildCommunityModerationPlainText(final String heading, final String intro,
                                                     final String communityLabel, final String communityName,
                                                     final String contentLabel, final String contentTitle,
                                                     final String moderatorReason, final String actionUrl,
                                                     final Locale locale) {
        return """
                %s

                %s

                %s: %s
                %s: %s

                %s:
                %s

                %s: %s
                """.formatted(
                safeValue(heading),
                safeValue(intro),
                safeValue(communityLabel),
                safeValue(communityName),
                safeValue(contentLabel),
                safeValue(contentTitle),
                msg("email.community.label.reason", locale),
                safeValue(moderatorReason),
                msg("email.community.label.link", locale),
                safeValue(actionUrl)
        );
    }

    private String buildCommunityModerationHtml(final String preheader, final String heading,
                                                final String intro, final String contentLabel,
                                                final String contentTitle, final String communityName,
                                                final String moderatorReason, final String actionUrl,
                                                final Locale locale) {
        final String bodyHtml = buildCommunitySummaryCard(contentLabel, contentTitle, communityName, locale)
                + buildModerationReason(msg("email.community.label.reason", locale), moderatorReason)
                + buildCenteredAction(escapeHtml(actionUrl), msg("email.action.viewCommunity", locale));

        return buildEmailShell(
                escapeHtml(preheader),
                heading,
                intro,
                bodyHtml,
                locale
        );
    }

    private String buildCommunitySummaryCard(final String contentLabel, final String contentTitle,
                                             final String communityName, final Locale locale) {
        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:24px;">
                    %s
                    <div style="font-size:24px;line-height:1.2;font-weight:700;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:10px;font-size:14px;line-height:1.6;color:%s;font-family:%s;">
                        <strong style="color:%s;">%s:</strong> %s
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                buildSectionLabel(contentLabel),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                escapeHtml(safeValue(contentTitle)),
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                COLOR_ON_SURFACE,
                escapeHtml(msg("email.community.label.community", locale)),
                escapeHtml(safeValue(communityName))
        );
    }

    private void sendCommunityRoleNotification(final String recipientEmail, final String communityName,
                                               final String actionUrl, final String subjectCode,
                                               final String preheaderCode, final String headingCode,
                                               final String introCode, final String actionLabelCode) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        final Locale locale = resolveRecipientLocale(recipientEmail);
        final String resolvedUrl = absoluteUrl(actionUrl);
        sendEmail(
                msg(subjectCode, locale, APP_NAME, sanitizeHeaderValue(communityName)),
                buildCommunityRolePlainText(headingCode, introCode, communityName, resolvedUrl, locale),
                buildCommunityRoleHtml(preheaderCode, headingCode, introCode, communityName, resolvedUrl,
                        actionLabelCode, locale),
                "Failed to send community role notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    private String buildCommunityRolePlainText(final String headingCode, final String introCode,
                                               final String communityName, final String actionUrl,
                                               final Locale locale) {
        return """
                %s

                %s

                %s: %s
                %s: %s
                """.formatted(
                msg(headingCode, locale),
                msg(introCode, locale, safeValue(communityName), APP_NAME),
                msg("email.community.label.community", locale),
                safeValue(communityName),
                msg("email.community.label.link", locale),
                safeValue(actionUrl)
        );
    }

    private String buildCommunityRoleHtml(final String preheaderCode, final String headingCode,
                                          final String introCode, final String communityName,
                                          final String actionUrl, final String actionLabelCode,
                                          final Locale locale) {
        final String bodyHtml = buildCommunitySummaryCard(
                msg("email.community.label.community", locale),
                communityName,
                communityName,
                locale
        ) + buildCenteredAction(escapeHtml(actionUrl), msg(actionLabelCode, locale));

        return buildEmailShell(
                escapeHtml(msg(preheaderCode, locale, safeValue(communityName))),
                msg(headingCode, locale),
                msg(introCode, locale, safeValue(communityName), APP_NAME),
                bodyHtml,
                locale
        );
    }

    private String buildUserDigestReviewSection(final List<EmailService.ReviewActivityItem> items,
                                                final Locale locale) {
        final String sectionLabel = msg("email.userDigest.reviewSection", locale);
        if (items.isEmpty()) {
            return buildDigestEmptySection(sectionLabel, msg("email.userDigest.review.empty", locale));
        }
        final StringBuilder rows = new StringBuilder();
        for (final EmailService.ReviewActivityItem item : items) {
            final StringBuilder detail = new StringBuilder();
            if (item.newLikes > 0) {
                detail.append(escapeHtml(msg("email.userDigest.likes", locale, item.newLikes)));
            }
            if (item.newReplies > 0) {
                if (detail.length() > 0) {
                    detail.append(" &amp; ");
                }
                detail.append(escapeHtml(msg("email.userDigest.replies", locale, item.newReplies)));
            }
            rows.append(buildDigestRow(
                    escapeHtml(safeValue(item.reviewTitle)),
                    escapeHtml(safeValue(item.carName)),
                    detail.toString()
            ));
        }
        return buildDigestSection(sectionLabel, rows.toString());
    }

    private String buildUserDigestFavoriteSection(final List<EmailService.FavoriteActivityItem> items,
                                                  final Locale locale) {
        final String sectionLabel = msg("email.userDigest.favoriteSection", locale);
        if (items.isEmpty()) {
            return buildDigestEmptySection(sectionLabel, msg("email.userDigest.favorite.empty", locale));
        }
        final StringBuilder rows = new StringBuilder();
        for (final EmailService.FavoriteActivityItem item : items) {
            final String detail = escapeHtml(msg("email.userDigest.newReviews", locale, item.newReviewCount));
            rows.append(buildDigestRow(escapeHtml(safeValue(item.carName)), null, detail));
        }
        return buildDigestSection(sectionLabel, rows.toString());
    }

    private String buildDigestSection(final String label, final String rowsHtml) {
        return """
                <div style="margin-bottom:24px;">
                    %s
                    <div style="background:%s;border:1px solid %s;border-radius:18px;overflow:hidden;">
                        %s
                    </div>
                </div>
                """.formatted(
                buildSectionLabel(label),
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                rowsHtml
        );
    }

    private String buildDigestEmptySection(final String label, final String message) {
        return """
                <div style="margin-bottom:24px;">
                    %s
                    <div style="background:%s;border:1px solid %s;border-radius:18px;padding:20px 22px;font-size:15px;color:%s;font-family:%s;">
                        %s
                    </div>
                </div>
                """.formatted(
                buildSectionLabel(label),
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                escapeHtml(message)
        );
    }

    private String buildDigestRow(final String title, final String subtitle, final String detail) {
        final String subtitleHtml = subtitle != null
                ? "<div style=\"font-size:13px;line-height:1.4;color:" + COLOR_ON_SURFACE_VARIANT
                + ";font-family:" + BODY_FONT + ";margin-top:2px;\">" + subtitle + "</div>"
                : "";
        return """
                <div style="padding:16px 22px;border-bottom:1px solid %s;">
                    <div style="font-size:15px;font-weight:600;color:%s;font-family:%s;">%s</div>
                    %s
                    <div style="margin-top:6px;font-size:13px;color:%s;font-family:%s;">%s</div>
                </div>
                """.formatted(
                COLOR_OUTLINE,
                COLOR_ON_SURFACE,
                BODY_FONT,
                title,
                subtitleHtml,
                COLOR_PRIMARY,
                BODY_FONT,
                detail
        );
    }

    // ── Approved notification helpers ─────────────────────────────────────────

    private String buildApprovedSubject(final String brandName, final String model, final Locale locale) {
        return msg("email.carApproved.subject", locale, APP_NAME,
                sanitizeHeaderValue(brandName), sanitizeHeaderValue(model));
    }

    private String buildApprovedPlainTextBody(final String brandName, final String model, final String carUrl,
                                              final Locale locale) {
        return msg(
                "email.carApproved.plain",
                locale,
                safeValue(brandName),
                safeValue(model),
                APP_NAME,
                carUrl
        );
    }

    private String buildApprovedHtmlBody(final String brandName, final String model, final String carUrl,
                                         final Locale locale) {
        final String carName = escapeHtml(safeValue(brandName) + " " + safeValue(model));
        final String bodyHtml = buildApprovedSummaryCard(carName, locale)
                + buildCenteredAction(escapeHtml(carUrl), msg("email.action.viewCar", locale));

        return buildEmailShell(
                escapeHtml(msg("email.carApproved.preheader", locale, safeValue(brandName), safeValue(model))),
                msg("email.carApproved.title", locale),
                msg("email.carApproved.intro", locale),
                bodyHtml,
                locale
        );
    }

    private String buildApprovedSummaryCard(final String carName, final Locale locale) {
        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:28px;">
                    %s
                    <div style="font-size:28px;line-height:1.15;font-weight:700;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:12px;">
                        %s
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                buildSectionLabel(msg("email.carApproved.summary", locale)),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                carName,
                buildPill(msg("email.status.approved", locale), "rgba(100,200,100,0.14)", "rgba(100,200,100,0.3)", "#88c888")
        );
    }

    // ── Rejected notification helpers ────────────────────────────────────────

    private String buildRejectedSubject(final String brandName, final String model, final Locale locale) {
        return msg("email.carRejected.subject", locale, APP_NAME,
                sanitizeHeaderValue(brandName), sanitizeHeaderValue(model));
    }

    private String buildRejectedPlainTextBody(final String brandName, final String model, final Locale locale) {
        return msg(
                "email.carRejected.plain",
                locale,
                safeValue(brandName),
                safeValue(model),
                APP_NAME,
                appBaseUrl + "/cars"
        );
    }

    private String buildRejectedHtmlBody(final String brandName, final String model, final Locale locale) {
        final String carName = escapeHtml(safeValue(brandName) + " " + safeValue(model));
        final String bodyHtml = buildRejectedSummaryCard(carName, locale)
                + buildCenteredAction(escapeHtml(appBaseUrl + "/cars"), msg("common.action.viewCatalog", locale));

        return buildEmailShell(
                escapeHtml(msg("email.carRejected.preheader", locale, safeValue(brandName), safeValue(model))),
                msg("email.carRejected.title", locale),
                msg("email.carRejected.intro", locale),
                bodyHtml,
                locale
        );
    }

    private String buildRejectedSummaryCard(final String carName, final Locale locale) {
        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:28px;">
                    %s
                    <div style="font-size:28px;line-height:1.15;font-weight:700;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:12px;">
                        %s
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                buildSectionLabel(msg("email.carRejected.summary", locale)),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                carName,
                buildPill(msg("email.status.rejected", locale), "rgba(220,90,90,0.14)", "rgba(220,90,90,0.32)", "#f09494")
        );
    }

    // ── Request decision notification helpers ────────────────────────────────

    private void sendRequestDecisionNotification(final String recipientEmail, final String requestType,
                                                 final String requestedName, final boolean approved) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }
        final Locale locale = resolveRecipientLocale(recipientEmail);
        final String actionUrl = approved ? appBaseUrl + "/cars/new" : appBaseUrl + "/cars";
        final String actionLabel = approved
                ? msg("email.action.requestCar", locale)
                : msg("common.action.viewCatalog", locale);
        sendRequestDecisionNotification(
                recipientEmail,
                localizedRequestType(requestType, locale),
                requestedName,
                approved,
                actionUrl,
                actionLabel,
                locale
        );
    }

    private void sendRequestDecisionNotification(final String recipientEmail, final String requestType,
                                                 final String requestedName, final boolean approved,
                                                 final String actionUrl, final String actionLabel,
                                                 final Locale locale) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        sendEmail(
                buildRequestDecisionSubject(requestType, requestedName, approved, locale),
                buildRequestDecisionPlainTextBody(requestType, requestedName, approved, actionUrl, locale),
                buildRequestDecisionHtmlBody(requestType, requestedName, approved, actionUrl, actionLabel, locale),
                "Failed to send request decision notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    private String buildRequestDecisionSubject(final String requestType, final String requestedName,
                                               final boolean approved, final Locale locale) {
        return msg(
                approved ? "email.requestDecision.subject.approved" : "email.requestDecision.subject.rejected",
                locale,
                APP_NAME,
                sanitizeHeaderValue(requestType),
                sanitizeHeaderValue(requestedName)
        );
    }

    private String buildRequestDecisionPlainTextBody(final String requestType, final String requestedName,
                                                     final boolean approved, final String actionUrl,
                                                     final Locale locale) {
        return msg(
                approved ? "email.requestDecision.plain.approved" : "email.requestDecision.plain.rejected",
                locale,
                safeValue(requestType),
                safeValue(requestedName),
                APP_NAME,
                safeValue(actionUrl)
        );
    }

    private String buildRequestDecisionHtmlBody(final String requestType, final String requestedName,
                                                final boolean approved, final String actionUrl,
                                                final String actionLabel, final Locale locale) {
        final String title = approved
                ? msg("email.requestDecision.title.approved", locale)
                : msg("email.requestDecision.title.rejected", locale);
        final String intro = approved
                ? msg("email.requestDecision.intro.approved", locale)
                : msg("email.requestDecision.intro.rejected", locale);
        final String bodyHtml = buildRequestDecisionSummaryCard(requestType, requestedName, approved, locale)
                + buildCenteredAction(escapeHtml(actionUrl), actionLabel);

        return buildEmailShell(
                escapeHtml(msg(
                        approved ? "email.requestDecision.preheader.approved" : "email.requestDecision.preheader.rejected",
                        locale,
                        safeValue(requestType)
                )),
                title,
                intro,
                bodyHtml,
                locale
        );
    }

    private String buildRequestDecisionSummaryCard(final String requestType, final String requestedName,
                                                   final boolean approved, final Locale locale) {
        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:28px;">
                    %s
                    <div style="font-size:28px;line-height:1.15;font-weight:700;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:8px;font-size:15px;line-height:1.6;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:12px;">
                        %s
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                buildSectionLabel(msg("email.requestDecision.summary", locale)),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                escapeHtml(safeValue(requestedName)),
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                escapeHtml(msg("email.requestDecision.requestOf", locale, safeValue(requestType))),
                approved
                        ? buildPill(msg("email.status.approved.feminine", locale), "rgba(100,200,100,0.14)", "rgba(100,200,100,0.3)", "#88c888")
                        : buildPill(msg("email.status.rejected.feminine", locale), "rgba(220,90,90,0.14)", "rgba(220,90,90,0.32)", "#f09494")
        );
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private boolean hasRequestImage(final CarRequest request) {
        return request.getImageData() != null
                && request.getImageData().length > 0
                && request.getImageContentType() != null
                && !request.getImageContentType().isBlank();
    }

    private String buildEmailShell(final String preheader, final String title,
                                   final String intro, final String bodyHtml,
                                   final Locale locale) {
        return """
                <!DOCTYPE html>
                <html lang="%s">
                <body style="margin:0;padding:24px;background:%s;color:%s;font-family:%s;">
                    <div style="display:none;max-height:0;max-width:0;opacity:0;overflow:hidden;color:transparent;">
                        %s
                    </div>
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:680px;margin:0 auto;">
                        <tr>
                            <td style="padding:0;">
                                %s
                                <div style="background:%s;padding:32px;border:1px solid %s;border-top:none;border-radius:0 0 22px 22px;">
                                    %s
                                </div>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                locale.toLanguageTag(),
                COLOR_SURFACE_LOW,
                COLOR_ON_SURFACE,
                BODY_FONT,
                preheader,
                buildEmailHeader(title, intro),
                COLOR_SURFACE,
                COLOR_OUTLINE,
                bodyHtml
        );
    }

    private String buildEmailHeader(final String title, final String intro) {
        return """
                <div style="background:%s;background-image:linear-gradient(135deg,%s 0%%,#ff8c64 56%%,%s 100%%);color:%s;padding:32px;border-radius:22px 22px 0 0;">
                    <div style="display:inline-block;padding:7px 12px;border-radius:999px;background:rgba(26,8,0,0.14);font-size:11px;line-height:1;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;font-family:%s;">
                        %s
                    </div>
                    <h1 style="margin:16px 0 8px;font-size:30px;line-height:1.1;font-weight:700;font-family:%s;">
                        %s
                    </h1>
                    <p style="margin:0;font-size:15px;line-height:1.7;color:rgba(26,8,0,0.88);font-family:%s;">
                        %s
                    </p>
                </div>
                """.formatted(
                COLOR_PRIMARY_CONTAINER,
                COLOR_PRIMARY_CONTAINER,
                COLOR_PRIMARY,
                COLOR_ON_PRIMARY,
                BODY_FONT,
                escapeHtml(APP_NAME),
                DISPLAY_FONT,
                escapeHtml(title),
                BODY_FONT,
                escapeHtml(intro)
        );
    }

    private String buildSectionLabel(final String label) {
        return """
                <div style="font-size:12px;line-height:1;color:%s;text-transform:uppercase;letter-spacing:0.12em;margin-bottom:12px;font-weight:700;font-family:%s;">
                    %s
                </div>
                """.formatted(
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                escapeHtml(label)
        );
    }

    private String buildPill(final String label, final String background, final String borderColor,
                             final String textColor) {
        return """
                <span style="display:inline-block;padding:7px 12px;border-radius:999px;background:%s;border:1px solid %s;font-size:12px;font-weight:700;letter-spacing:0.06em;text-transform:uppercase;color:%s;font-family:%s;">
                    %s
                </span>
                """.formatted(
                background,
                borderColor,
                textColor,
                BODY_FONT,
                escapeHtml(label)
        );
    }

    private String buildCenteredAction(final String url, final String label) {
        return """
                <div style="text-align:center;">
                    <a href="%s" style="display:inline-block;padding:14px 32px;background:%s;color:%s;font-size:15px;font-weight:700;text-decoration:none;border-radius:999px;font-family:%s;letter-spacing:0.04em;">
                        %s
                    </a>
                </div>
                """.formatted(
                url,
                COLOR_PRIMARY_CONTAINER,
                COLOR_ON_PRIMARY,
                DISPLAY_FONT,
                escapeHtml(label)
        );
    }

    private String sanitizeHeaderValue(final String value) {
        return safeValue(value)
                .chars()
                .filter(ch -> !Character.isISOControl(ch))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String previewDescription(final String description, final Locale locale) {
        if (description == null || description.isBlank()) {
            return msg("email.request.description.empty", locale);
        }
        final String trimmed = description.trim();
        if (trimmed.length() <= DESCRIPTION_PREVIEW_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, DESCRIPTION_PREVIEW_LENGTH - 3).trim() + "...";
    }

    private String safeValue(final String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String absoluteUrl(final String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isBlank()) {
            return appBaseUrl;
        }
        final String trimmed = pathOrUrl.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return appBaseUrl + trimmed;
        }
        return appBaseUrl + "/" + trimmed;
    }

    private Locale resolveRecipientLocale(final String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return defaultLocale();
        }
        try {
            return userService.findByEmail(recipientEmail)
                    .map(User::getPreferredLocale)
                    .map(this::toSupportedLocale)
                    .orElseGet(this::defaultLocale);
        } catch (final RuntimeException e) {
            LOGGER.warn("failed to resolve recipient locale for email={}", recipientEmail, e);
            return defaultLocale();
        }
    }

    private Map<Locale, List<String>> groupRecipientEmailsByLocale(final List<EmailRecipient> recipients) {
        return recipients.stream()
                .filter(recipient -> recipient != null
                        && recipient.getEmail() != null
                        && !recipient.getEmail().isBlank())
                .collect(Collectors.groupingBy(
                        recipient -> toSupportedLocale(recipient.getPreferredLocale()),
                        Collectors.mapping(EmailRecipient::getEmail, Collectors.toList())
                ));
    }

    private List<EmailRecipient> getModeratorEmailRecipientsSafely() {
        try {
            return userService.getModeratorEmailRecipients();
        } catch (final RuntimeException e) {
            LOGGER.warn("failed to load moderator email recipients", e);
            return List.of();
        }
    }

    private Locale toSupportedLocale(final String localeTag) {
        final Locale locale = Locale.forLanguageTag(localeTag == null ? "" : localeTag);
        final String language = locale.getLanguage();
        if ("en".equals(language) || "es".equals(language)) {
            return Locale.forLanguageTag(language);
        }
        return defaultLocale();
    }

    private Locale defaultLocale() {
        return Locale.forLanguageTag(DEFAULT_LOCALE_TAG);
    }

    private String localizedRequestType(final String requestType, final Locale locale) {
        final String normalized = requestType == null ? "" : requestType.trim().toLowerCase(Locale.ROOT);
        if ("marca".equals(normalized) || "brand".equals(normalized)) {
            return msg("email.requestDecision.type.brand", locale);
        }
        if ("tipo de carrocería".equals(normalized) || "tipo de carroceria".equals(normalized)
                || "body type".equals(normalized)) {
            return msg("email.requestDecision.type.bodyType", locale);
        }
        if ("moderador".equals(normalized) || "moderator".equals(normalized)) {
            return msg("email.requestDecision.type.moderator", locale);
        }
        return safeValue(requestType);
    }

    private String msg(final String code, final Locale locale, final Object... args) {
        return messageSource.getMessage(code, args, locale == null ? defaultLocale() : locale);
    }

    private String escapeHtml(final String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @FunctionalInterface
    private interface EmailCustomization {
        void apply(MimeMessageHelper helper) throws MessagingException;
    }
}
