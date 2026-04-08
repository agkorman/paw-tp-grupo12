package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());
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
    private final CarService carService;

    @Autowired
    public EmailServiceImpl(final JavaMailSender mailSender, final UserService userService,
                            final CarService carService) {
        this.mailSender = mailSender;
        this.userService = userService;
        this.carService = carService;
    }

    @Override
    @Async("mailTaskExecutor")
    public void sendCarCreatedNotification(final Car car) {
        try {
            final List<String> moderators = userService.getModeratorsEmails();
            if (moderators.isEmpty()) {
                return;
            }

            final MimeMessage message = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(moderators.toArray(new String[0]));
            helper.setSubject(buildSubject(car));
            final CarImage carImage = getInlineCarImage(car);
            helper.setText(buildPlainTextBody(car), buildHtmlBody(car, carImage != null));
            if (carImage != null) {
                helper.addInline(
                        CAR_IMAGE_CID,
                        new ByteArrayResource(carImage.getImageData()),
                        carImage.getContentType()
                );
            }

            mailSender.send(message);
        } catch (final MessagingException | RuntimeException e) {
            LOGGER.warning("Failed to send car creation notification for car " + car.getId() + ": " + e.getMessage());
        }
    }

    private String buildSubject(final Car car) {
        return "[" + APP_NAME + "] Nuevo auto para revisar: " + sanitizeHeaderValue(car.getBrandName())
                + " " + sanitizeHeaderValue(car.getModel());
    }

    private String sanitizeHeaderValue(final String value) {
        final String sanitized = safeValue(value)
                .chars()
                .filter(ch -> !Character.isISOControl(ch))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString()
                .replaceAll("\\s+", " ")
                .trim();

        return sanitized;
    }
    private String buildPlainTextBody(final Car car) {
        return """
                Hola equipo,

                Se registró un nuevo auto en %s y quedó listo para revisión.

                Marca: %s
                Modelo: %s
                Carrocería: %s
                Imagen cargada: %s

                Descripción:
                %s
                """.formatted(
                APP_NAME,
                safeValue(car.getBrandName()),
                safeValue(car.getModel()),
                safeValue(car.getBodyType()),
                car.getHasImage() ? "Sí" : "No",
                previewDescription(car)
        );
    }

    private String buildHtmlBody(final Car car, final boolean hasInlineImage) {
        final String brand = safeValue(car.getBrandName());
        final String model = safeValue(car.getModel());
        final String bodyType = escapeHtml(safeValue(car.getBodyType()));
        final String description = escapeHtml(previewDescription(car)).replace("\n", "<br>");
        final String imageStatus = hasInlineImage ? "Con imagen" : "Sin imagen";
        final String carName = escapeHtml(brand + " " + model);
        final String preheader = escapeHtml("Nueva alta en el catálogo: " + brand + " " + model);
        final String intro = escapeHtml(
                "Se registró un nuevo auto en La Posta Autos. El equipo ya puede revisarlo desde el catálogo."
        );
        final String imageBlock = hasInlineImage
                ? """
                                    <div style="margin-bottom:24px;">
                                        <div style="font-size:12px;line-height:1;color:%s;text-transform:uppercase;letter-spacing:0.12em;margin-bottom:12px;font-weight:700;font-family:%s;">
                                            Imagen enviada
                                        </div>
                                        <div style="background:%s;border:1px solid %s;border-radius:18px;padding:10px;">
                                            <img src="cid:%s" alt="%s" style="display:block;width:100%%;max-width:596px;height:auto;border-radius:12px;">
                                        </div>
                                    </div>
                                """.formatted(
                        COLOR_ON_SURFACE_VARIANT,
                        BODY_FONT,
                        COLOR_SURFACE_HIGH,
                        COLOR_OUTLINE,
                        CAR_IMAGE_CID,
                        carName
                )
                : "";

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
                                <div style="background:%s;background-image:linear-gradient(135deg,%s 0%%,#ff8c64 56%%,%s 100%%);color:%s;padding:32px;border-radius:22px 22px 0 0;">
                                    <div style="display:inline-block;padding:7px 12px;border-radius:999px;background:rgba(26,8,0,0.14);font-size:11px;line-height:1;font-weight:700;letter-spacing:0.14em;text-transform:uppercase;font-family:%s;">
                                        %s
                                    </div>
                                    <h1 style="margin:16px 0 8px;font-size:30px;line-height:1.1;font-weight:700;font-family:%s;">
                                        Nueva alta en el catálogo
                                    </h1>
                                    <p style="margin:0;font-size:15px;line-height:1.7;color:rgba(26,8,0,0.88);font-family:%s;">
                                        %s
                                    </p>
                                </div>

                                <div style="background:%s;padding:32px;border:1px solid %s;border-top:none;border-radius:0 0 22px 22px;">
                                    <div style="background:%s;border:1px solid %s;border-radius:18px;padding:22px 24px;margin-bottom:24px;">
                                        <div style="font-size:12px;line-height:1;color:%s;text-transform:uppercase;letter-spacing:0.12em;margin-bottom:10px;font-weight:700;font-family:%s;">
                                            Resumen del vehículo
                                        </div>
                                        <div style="font-size:28px;line-height:1.15;font-weight:700;color:%s;font-family:%s;">
                                            %s
                                        </div>
                                        <div style="margin-top:8px;font-size:15px;line-height:1.6;color:%s;font-family:%s;">
                                            %s
                                        </div>
                                        <table role="presentation" cellspacing="0" cellpadding="0" style="margin-top:18px;">
                                            <tr>
                                                <td style="padding:0 10px 10px 0;">
                                                    <span style="display:inline-block;padding:7px 12px;border-radius:999px;background:rgba(255,87,25,0.14);border:1px solid rgba(255,181,158,0.16);font-size:12px;font-weight:700;letter-spacing:0.06em;text-transform:uppercase;color:%s;font-family:%s;">
                                                        Pendiente
                                                    </span>
                                                </td>
                                                <td style="padding:0 0 10px 0;">
                                                    <span style="display:inline-block;padding:7px 12px;border-radius:999px;background:%s;border:1px solid %s;font-size:12px;font-weight:700;letter-spacing:0.06em;text-transform:uppercase;color:%s;font-family:%s;">
                                                        %s
                                                    </span>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>

                                    %s

                                    <div style="font-size:12px;line-height:1;color:%s;text-transform:uppercase;letter-spacing:0.12em;margin-bottom:12px;font-weight:700;font-family:%s;">
                                        Descripción enviada
                                    </div>
                                    <div style="background:%s;border:1px solid %s;border-radius:18px;padding:20px 22px;font-size:15px;line-height:1.7;color:%s;font-family:%s;">
                                        %s
                                    </div>

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
                COLOR_PRIMARY_CONTAINER,
                COLOR_PRIMARY_CONTAINER,
                COLOR_PRIMARY,
                COLOR_ON_PRIMARY,
                BODY_FONT,
                escapeHtml(APP_NAME),
                DISPLAY_FONT,
                BODY_FONT,
                intro,
                COLOR_SURFACE,
                COLOR_OUTLINE,
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                COLOR_ON_SURFACE,
                DISPLAY_FONT,
                carName,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                bodyType,
                COLOR_PRIMARY,
                BODY_FONT,
                COLOR_SURFACE_HIGHEST,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE,
                BODY_FONT,
                escapeHtml(imageStatus),
                imageBlock,
                COLOR_ON_SURFACE_VARIANT,
                BODY_FONT,
                COLOR_SURFACE_HIGH,
                COLOR_OUTLINE,
                COLOR_ON_SURFACE,
                BODY_FONT,
                description
        );
    }

    private CarImage getInlineCarImage(final Car car) {
        return carService.getCarImageByCarId(car.getId())
                .filter(image -> image.getImageData() != null && image.getImageData().length > 0)
                .filter(image -> image.getContentType() != null && !image.getContentType().isBlank())
                .orElse(null);
    }

    private String previewDescription(final Car car) {
        if (car.getDescription() == null || car.getDescription().isBlank()) {
            return "Sin descripción provista.";
        }
        final String description = car.getDescription().trim();
        if (description.length() <= DESCRIPTION_PREVIEW_LENGTH) {
            return description;
        }
        return description.substring(0, DESCRIPTION_PREVIEW_LENGTH - 3).trim() + "...";
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
}
