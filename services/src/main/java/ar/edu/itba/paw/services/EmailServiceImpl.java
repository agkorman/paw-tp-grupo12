package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.CarRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String APP_NAME = "La Posta Autos";
    private static final int DESCRIPTION_PREVIEW_LENGTH = 220;
    private static final String CAR_IMAGE_CID = "car-image";
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
    private final String appBaseUrl;

    @Autowired
    public EmailServiceImpl(final JavaMailSender mailSender, final UserService userService,
                            @Qualifier("appBaseUrl") final String appBaseUrl) {
        this.mailSender = mailSender;
        this.userService = userService;
        this.appBaseUrl = appBaseUrl;
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendNewCarRequestNotification(final CarRequest request, final String brandName,
                                              final String bodyTypeName) {
        final List<String> moderators = userService.getModeratorsEmails();
        if (moderators.isEmpty()) {
            return;
        }

        final boolean hasInlineImage = hasInlineRequestImage(request);
        sendEmail(
                buildRequestSubject(brandName, request.getModel()),
                buildRequestPlainTextBody(request, brandName, bodyTypeName),
                buildRequestHtmlBody(request, brandName, bodyTypeName, hasInlineImage),
                "Failed to send car request notification for request " + request.getId(),
                helper -> {
                    helper.setTo(moderators.toArray(new String[0]));
                    if (hasInlineImage) {
                        helper.addInline(
                                CAR_IMAGE_CID,
                                new ByteArrayResource(request.getImageData()),
                                request.getImageContentType()
                        );
                    }
                }
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCarApprovedNotification(final String recipientEmail, final String brandName,
                                            final String model, final long carId) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        final String carUrl = appBaseUrl + "/reviews?carId=" + carId;
        sendEmail(
                buildApprovedSubject(brandName, model),
                buildApprovedPlainTextBody(brandName, model, carUrl),
                buildApprovedHtmlBody(brandName, model, carUrl),
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

        sendEmail(
                buildRejectedSubject(brandName, model),
                buildRejectedPlainTextBody(brandName, model),
                buildRejectedHtmlBody(brandName, model),
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
        sendRequestDecisionNotification(recipientEmail, "moderador", "Permisos de moderador", true,
                appBaseUrl + "/admin", "Panel de administración");
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendAdminRequestRejectedNotification(final String recipientEmail) {
        sendRequestDecisionNotification(recipientEmail, "moderador", "Permisos de moderador", false);
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendReviewHiddenNotification(final String recipientEmail, final String subject,
                                             final String heading, final String intro,
                                             final String reviewLabel, final String carLabel,
                                             final String reasonLabel, final String reviewTitle,
                                             final String carName, final String moderatorReason) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        sendEmail(
                sanitizeHeaderValue(subject),
                buildReviewHiddenPlainText(heading, intro, reviewLabel, carLabel, reasonLabel,
                        reviewTitle, carName, moderatorReason),
                buildReviewHiddenHtml(heading, intro, reviewLabel, carLabel, reasonLabel,
                        reviewTitle, carName, moderatorReason),
                "Failed to send hidden review notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendWeeklyModeratorDigest(final List<String> moderatorEmails, final int pendingRequestCount) {
        if (moderatorEmails == null || moderatorEmails.isEmpty()) {
            return;
        }

        sendEmail(
                buildModeratorDigestSubject(pendingRequestCount),
                buildModeratorDigestPlainText(pendingRequestCount),
                buildModeratorDigestHtml(pendingRequestCount),
                "Failed to send weekly moderator digest",
                helper -> helper.setTo(moderatorEmails.toArray(new String[0]))
        );
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendWeeklyUserDigest(final String recipientEmail, final String username,
                                     final List<EmailService.ReviewActivityItem> reviewActivity,
                                     final List<EmailService.FavoriteActivityItem> favoriteActivity) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        sendEmail(
                "[" + APP_NAME + "] Tu resumen semanal, " + sanitizeHeaderValue(safeValue(username)),
                buildUserDigestPlainText(username, reviewActivity, favoriteActivity),
                buildUserDigestHtml(username, reviewActivity, favoriteActivity),
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

    private String buildRequestSubject(final String brandName, final String model) {
        return "[" + APP_NAME + "] Nuevo auto para revisar: "
                + sanitizeHeaderValue(brandName) + " " + sanitizeHeaderValue(model);
    }

    private String buildRequestPlainTextBody(final CarRequest request, final String brandName,
                                             final String bodyTypeName) {
        return """
                Hola equipo,

                Se registró un nuevo auto en %s y quedó listo para revisión.

                Marca: %s
                Modelo: %s
                Año modelo: %s
                Carrocería: %s
                Imagen cargada: %s

                Descripción:
                %s

                Revisalo en el dashboard: %s
                """.formatted(
                APP_NAME,
                safeValue(brandName),
                safeValue(request.getModel()),
                request.getYear() == null ? "N/A" : request.getYear().toString(),
                safeValue(bodyTypeName),
                request.getImageData() != null && request.getImageData().length > 0 ? "Sí" : "No",
                previewDescription(request.getDescription()),
                appBaseUrl + "/admin"
        );
    }

    private String buildRequestHtmlBody(final CarRequest request, final String brandName,
                                        final String bodyTypeName, final boolean hasInlineImage) {
        final String brand = safeValue(brandName);
        final String model = safeValue(request.getModel());
        final String year = request.getYear() == null ? "" : " " + request.getYear();
        final String carName = escapeHtml(brand + " " + model + year);
        final String bodyType = escapeHtml(safeValue(bodyTypeName));
        final String description = escapeHtml(previewDescription(request.getDescription())).replace("\n", "<br>");
        final String imageStatus = hasInlineImage ? "Con imagen" : "Sin imagen";
        final String dashboardUrl = escapeHtml(appBaseUrl + "/admin");

        final String bodyHtml = buildRequestSummaryCard(carName, bodyType, imageStatus)
                + buildRequestImageBlock(hasInlineImage, carName)
                + buildRequestDescriptionBlock(description)
                + buildCenteredAction(dashboardUrl, "Ir al dashboard");

        return buildEmailShell(
                escapeHtml("Nueva alta en el catálogo: " + brand + " " + model),
                "Nueva alta en el catálogo",
                "Se registró un nuevo auto en La Posta Autos. El equipo ya puede revisarlo desde el catálogo.",
                bodyHtml
        );
    }

    private String buildRequestSummaryCard(final String carName, final String bodyType, final String imageStatus) {
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
                buildSectionLabel("Resumen del vehículo"),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                carName,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                bodyType,
                buildPill("Pendiente", "rgba(255,87,25,0.14)", "rgba(255,181,158,0.16)", COLOR_PRIMARY),
                buildPill(imageStatus, COLOR_SURFACE_HIGHEST, COLOR_OUTLINE, COLOR_ON_SURFACE)
        );
    }

    private String buildRequestImageBlock(final boolean hasInlineImage, final String carName) {
        if (!hasInlineImage) {
            return "";
        }
        return """
                <div style="margin-bottom:24px;">
                    %s
                    <div style="background:%s;border:1px solid %s;border-radius:18px;padding:10px;">
                        <img src="cid:%s" alt="%s" style="display:block;width:100%%;max-width:596px;height:auto;border-radius:12px;">
                    </div>
                </div>
                """.formatted(
                buildSectionLabel("Imagen enviada"),
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                CAR_IMAGE_CID,
                carName
        );
    }

    private String buildRequestDescriptionBlock(final String description) {
        return """
                %s
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:20px 22px;font-size:15px;line-height:1.7;color:%s;font-family:%s;margin-bottom:28px;">
                    %s
                </div>
                """.formatted(
                buildSectionLabel("Descripción enviada"),
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE,
                BODY_FONT,
                description
        );
    }

    // ── Weekly moderator digest ───────────────────────────────────────────────

    private String buildModeratorDigestSubject(final int pendingCount) {
        return "[" + APP_NAME + "] Resumen semanal — "
                + (pendingCount == 0
                ? "sin pedidos pendientes"
                : pendingCount + " pedido" + (pendingCount == 1 ? "" : "s")
                + " pendiente" + (pendingCount == 1 ? "" : "s"));
    }

    private String buildModeratorDigestPlainText(final int pendingCount) {
        if (pendingCount == 0) {
            return """
                    Resumen semanal — %s

                    Todo al día: no hay pedidos de autos pendientes de revisión esta semana.

                    Dashboard: %s
                    """.formatted(APP_NAME, appBaseUrl + "/admin");
        }
        return """
                Resumen semanal — %s

                Esta semana hay %d pedido%s de autos esperando tu revisión.

                Revisalos en el dashboard: %s
                """.formatted(APP_NAME, pendingCount, pendingCount == 1 ? "" : "s", appBaseUrl + "/admin");
    }

    private String buildModeratorDigestHtml(final int pendingCount) {
        final boolean hasPending = pendingCount > 0;
        final String preheader = hasPending
                ? escapeHtml(pendingCount + " pedido" + (pendingCount == 1 ? "" : "s")
                + " de autos esperan tu revisión")
                : escapeHtml("Todo al día — sin pedidos pendientes esta semana");
        final String dashboardUrl = escapeHtml(appBaseUrl + "/admin");

        final String bodyHtml = buildModeratorDigestStatusCard(pendingCount)
                + buildCenteredAction(dashboardUrl, "Ir al dashboard");

        return buildEmailShell(
                preheader,
                "Resumen semanal",
                "Pedidos de autos pendientes de revisión en el catálogo.",
                bodyHtml
        );
    }

    private String buildModeratorDigestStatusCard(final int pendingCount) {
        if (pendingCount > 0) {
            return """
                    <div style="background:%s;border:1px solid %s;border-radius:18px;padding:28px 24px;margin-bottom:28px;text-align:center;">
                        <div style="font-size:56px;line-height:1;font-weight:700;color:%s;font-family:%s;">
                            %d
                        </div>
                        <div style="margin-top:8px;font-size:16px;line-height:1.5;color:%s;font-family:%s;">
                            pedido%s pendiente%s de revisión
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
                    pendingCount == 1 ? "" : "s",
                    pendingCount == 1 ? "" : "s"
            );
        }

        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:28px 24px;margin-bottom:28px;text-align:center;">
                    <div style="font-size:40px;line-height:1;margin-bottom:12px;">✓</div>
                    <div style="font-size:16px;line-height:1.5;color:%s;font-family:%s;">
                        Todo al día — sin pedidos pendientes esta semana.
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT
        );
    }

    // ── Weekly user digest ────────────────────────────────────────────────────

    private String buildUserDigestPlainText(final String username,
                                            final List<EmailService.ReviewActivityItem> reviewActivity,
                                            final List<EmailService.FavoriteActivityItem> favoriteActivity) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Hola, ").append(safeValue(username)).append("!\n\n");
        sb.append("Tu resumen semanal en ").append(APP_NAME).append(".\n\n");

        sb.append("── Actividad en tus reseñas ──\n");
        if (reviewActivity.isEmpty()) {
            sb.append("Sin actividad nueva esta semana.\n");
        } else {
            for (final EmailService.ReviewActivityItem item : reviewActivity) {
                sb.append("• ").append(safeValue(item.reviewTitle))
                        .append(" (").append(safeValue(item.carName)).append(")");
                if (item.newLikes > 0) {
                    sb.append(" — ").append(item.newLikes).append(" me gusta nuevo")
                            .append(item.newLikes == 1 ? "" : "s");
                }
                if (item.newReplies > 0) {
                    sb.append(" — ").append(item.newReplies).append(" respuesta")
                            .append(item.newReplies == 1 ? "" : "s")
                            .append(" nueva").append(item.newReplies == 1 ? "" : "s");
                }
                sb.append("\n");
            }
        }

        sb.append("\n── Novedades en tus favoritos ──\n");
        if (favoriteActivity.isEmpty()) {
            sb.append("Sin novedades esta semana.\n");
        } else {
            for (final EmailService.FavoriteActivityItem item : favoriteActivity) {
                sb.append("• ").append(safeValue(item.carName))
                        .append(" — ").append(item.newReviewCount)
                        .append(" reseña").append(item.newReviewCount == 1 ? "" : "s")
                        .append(" nueva").append(item.newReviewCount == 1 ? "" : "s")
                        .append("\n");
            }
        }

        sb.append("\nVer catálogo: ").append(appBaseUrl).append("/cars\n");
        return sb.toString();
    }

    private String buildUserDigestHtml(final String username,
                                       final List<EmailService.ReviewActivityItem> reviewActivity,
                                       final List<EmailService.FavoriteActivityItem> favoriteActivity) {
        final String displayName = escapeHtml(safeValue(username));
        final String bodyHtml = buildUserDigestReviewSection(reviewActivity)
                + buildUserDigestFavoriteSection(favoriteActivity)
                + buildCenteredAction(escapeHtml(appBaseUrl + "/cars"), "Ver catálogo");

        return buildEmailShell(
                escapeHtml("Tu actividad semanal en " + APP_NAME),
                "Hola, " + displayName + "!",
                "Esto es lo que pasó esta semana en tu cuenta.",
                bodyHtml
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
                                         final String carName, final String moderatorReason) {
        final String bodyHtml = buildModeratedReviewSummary(reviewLabel, carLabel, reviewTitle, carName)
                + buildModerationReason(reasonLabel, moderatorReason);

        return buildEmailShell(
                escapeHtml(safeValue(intro)),
                safeValue(heading),
                safeValue(intro),
                bodyHtml
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

    private String buildUserDigestReviewSection(final List<EmailService.ReviewActivityItem> items) {
        final String sectionLabel = "Actividad en tus reseñas";
        if (items.isEmpty()) {
            return buildDigestEmptySection(sectionLabel, "Sin actividad nueva esta semana.");
        }
        final StringBuilder rows = new StringBuilder();
        for (final EmailService.ReviewActivityItem item : items) {
            final StringBuilder detail = new StringBuilder();
            if (item.newLikes > 0) {
                detail.append(item.newLikes).append(" me gusta nuevo").append(item.newLikes == 1 ? "" : "s");
            }
            if (item.newReplies > 0) {
                if (detail.length() > 0) {
                    detail.append(" &amp; ");
                }
                detail.append(item.newReplies).append(" respuesta")
                        .append(item.newReplies == 1 ? "" : "s")
                        .append(" nueva").append(item.newReplies == 1 ? "" : "s");
            }
            rows.append(buildDigestRow(
                    escapeHtml(safeValue(item.reviewTitle)),
                    escapeHtml(safeValue(item.carName)),
                    detail.toString()
            ));
        }
        return buildDigestSection(sectionLabel, rows.toString());
    }

    private String buildUserDigestFavoriteSection(final List<EmailService.FavoriteActivityItem> items) {
        final String sectionLabel = "Novedades en tus favoritos";
        if (items.isEmpty()) {
            return buildDigestEmptySection(sectionLabel, "Sin novedades esta semana.");
        }
        final StringBuilder rows = new StringBuilder();
        for (final EmailService.FavoriteActivityItem item : items) {
            final String detail = item.newReviewCount + " reseña" + (item.newReviewCount == 1 ? "" : "s")
                    + " nueva" + (item.newReviewCount == 1 ? "" : "s");
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

    private String buildApprovedSubject(final String brandName, final String model) {
        return "[" + APP_NAME + "] Tu auto fue aprobado: "
                + sanitizeHeaderValue(brandName) + " " + sanitizeHeaderValue(model);
    }

    private String buildApprovedPlainTextBody(final String brandName, final String model, final String carUrl) {
        return """
                ¡Buenas noticias!

                Tu auto %s %s fue aprobado y ya está visible en el catálogo de %s.

                Podés verlo en: %s
                """.formatted(
                safeValue(brandName),
                safeValue(model),
                APP_NAME,
                carUrl
        );
    }

    private String buildApprovedHtmlBody(final String brandName, final String model, final String carUrl) {
        final String carName = escapeHtml(safeValue(brandName) + " " + safeValue(model));
        final String bodyHtml = buildApprovedSummaryCard(carName)
                + buildCenteredAction(escapeHtml(carUrl), "Ver auto");

        return buildEmailShell(
                escapeHtml("Tu auto " + safeValue(brandName) + " " + safeValue(model) + " ya está en el catálogo"),
                "¡Tu auto fue aprobado!",
                "Ya está disponible para que todos lo vean en el catálogo.",
                bodyHtml
        );
    }

    private String buildApprovedSummaryCard(final String carName) {
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
                buildSectionLabel("Vehículo aprobado"),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                carName,
                buildPill("Aprobado", "rgba(100,200,100,0.14)", "rgba(100,200,100,0.3)", "#88c888")
        );
    }

    // ── Rejected notification helpers ────────────────────────────────────────

    private String buildRejectedSubject(final String brandName, final String model) {
        return "[" + APP_NAME + "] Tu auto no fue aprobado: "
                + sanitizeHeaderValue(brandName) + " " + sanitizeHeaderValue(model);
    }

    private String buildRejectedPlainTextBody(final String brandName, final String model) {
        return """
                Gracias por enviar tu auto.

                Tu solicitud para publicar %s %s fue revisada, pero no fue aprobada para el catálogo de %s.

                Podés revisar el catálogo en: %s
                """.formatted(
                safeValue(brandName),
                safeValue(model),
                APP_NAME,
                appBaseUrl + "/cars"
        );
    }

    private String buildRejectedHtmlBody(final String brandName, final String model) {
        final String carName = escapeHtml(safeValue(brandName) + " " + safeValue(model));
        final String bodyHtml = buildRejectedSummaryCard(carName)
                + buildCenteredAction(escapeHtml(appBaseUrl + "/cars"), "Ver catálogo");

        return buildEmailShell(
                escapeHtml("Tu solicitud para publicar " + safeValue(brandName) + " " + safeValue(model)
                        + " no fue aprobada"),
                "Tu auto no fue aprobado",
                "El equipo revisó la solicitud y no la incorporó al catálogo.",
                bodyHtml
        );
    }

    private String buildRejectedSummaryCard(final String carName) {
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
                buildSectionLabel("Vehículo revisado"),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                carName,
                buildPill("Rechazado", "rgba(220,90,90,0.14)", "rgba(220,90,90,0.32)", "#f09494")
        );
    }

    // ── Request decision notification helpers ────────────────────────────────

    private void sendRequestDecisionNotification(final String recipientEmail, final String requestType,
                                                 final String requestedName, final boolean approved) {
        final String actionUrl = approved ? appBaseUrl + "/cars/new" : appBaseUrl + "/cars";
        final String actionLabel = approved ? "Solicitar auto" : "Ver catálogo";
        sendRequestDecisionNotification(recipientEmail, requestType, requestedName, approved, actionUrl, actionLabel);
    }

    private void sendRequestDecisionNotification(final String recipientEmail, final String requestType,
                                                 final String requestedName, final boolean approved,
                                                 final String actionUrl, final String actionLabel) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            return;
        }

        sendEmail(
                buildRequestDecisionSubject(requestType, requestedName, approved),
                buildRequestDecisionPlainTextBody(requestType, requestedName, approved, actionUrl),
                buildRequestDecisionHtmlBody(requestType, requestedName, approved, actionUrl, actionLabel),
                "Failed to send request decision notification to " + recipientEmail,
                helper -> helper.setTo(recipientEmail)
        );
    }

    private String buildRequestDecisionSubject(final String requestType, final String requestedName,
                                               final boolean approved) {
        return "[" + APP_NAME + "] Tu solicitud de " + sanitizeHeaderValue(requestType)
                + " fue " + (approved ? "aprobada" : "rechazada")
                + ": " + sanitizeHeaderValue(requestedName);
    }

    private String buildRequestDecisionPlainTextBody(final String requestType, final String requestedName,
                                                     final boolean approved, final String actionUrl) {
        return """
                Tu solicitud de %s fue %s.

                Solicitud: %s

                %s

                Link: %s
                """.formatted(
                safeValue(requestType),
                approved ? "aprobada" : "rechazada",
                safeValue(requestedName),
                approved
                        ? "El cambio ya fue aplicado en " + APP_NAME + "."
                        : "Gracias por enviarla. Por ahora no fue incorporada.",
                safeValue(actionUrl)
        );
    }

    private String buildRequestDecisionHtmlBody(final String requestType, final String requestedName,
                                                final boolean approved, final String actionUrl,
                                                final String actionLabel) {
        final String title = approved ? "Solicitud aprobada" : "Solicitud rechazada";
        final String intro = approved
                ? "El equipo revisó la solicitud y ya aplicó el cambio."
                : "El equipo revisó la solicitud y decidió no incorporarla por ahora.";
        final String bodyHtml = buildRequestDecisionSummaryCard(requestType, requestedName, approved)
                + buildCenteredAction(escapeHtml(actionUrl), actionLabel);

        return buildEmailShell(
                escapeHtml("Tu solicitud de " + safeValue(requestType) + " fue "
                        + (approved ? "aprobada" : "rechazada")),
                title,
                intro,
                bodyHtml
        );
    }

    private String buildRequestDecisionSummaryCard(final String requestType, final String requestedName,
                                                   final boolean approved) {
        return """
                <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:28px;">
                    %s
                    <div style="font-size:28px;line-height:1.15;font-weight:700;color:%s;font-family:%s;">
                        %s
                    </div>
                    <div style="margin-top:8px;font-size:15px;line-height:1.6;color:%s;font-family:%s;">
                        Solicitud de %s
                    </div>
                    <div style="margin-top:12px;">
                        %s
                    </div>
                </div>
                """.formatted(
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                buildSectionLabel("Resultado de revisión"),
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                escapeHtml(safeValue(requestedName)),
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                escapeHtml(safeValue(requestType)),
                approved
                        ? buildPill("Aprobada", "rgba(100,200,100,0.14)", "rgba(100,200,100,0.3)", "#88c888")
                        : buildPill("Rechazada", "rgba(220,90,90,0.14)", "rgba(220,90,90,0.32)", "#f09494")
        );
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private boolean hasInlineRequestImage(final CarRequest request) {
        return request.getImageData() != null
                && request.getImageData().length > 0
                && request.getImageContentType() != null
                && !request.getImageContentType().isBlank();
    }

    private String buildEmailShell(final String preheader, final String title,
                                   final String intro, final String bodyHtml) {
        return """
                <!DOCTYPE html>
                <html lang="es">
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

    private String previewDescription(final String description) {
        if (description == null || description.isBlank()) {
            return "Sin descripción provista.";
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
