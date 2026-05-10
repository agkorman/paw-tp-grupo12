package ar.edu.itba.paw.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    private static final String APP_BASE_URL = "http://localhost:8080/webapp";

    @Mock
    private UserService userService;

    @Test
    public void shouldSendApprovedNotificationWithRecipientSubjectAndCarLink() throws Exception {
        // Arrange
        final RecordingMailSender mailSender = new RecordingMailSender();
        final EmailServiceImpl emailService = new EmailServiceImpl(mailSender, userService, messageSource(), APP_BASE_URL);

        // Exercise
        emailService.sendCarApprovedNotification("owner@example.com", "Toyota", "Corolla", 42L);

        // Assertions
        assertEquals(1, mailSender.sentMessages.size());
        final MimeMessage message = mailSender.sentMessages.get(0);
        final Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        assertEquals(1, recipients.length);
        assertEquals("owner@example.com", recipients[0].toString());
        assertEquals("[La Posta Autos] Tu auto fue aprobado: Toyota Corolla", message.getSubject());
        assertTrue(extractText(message).contains(APP_BASE_URL + "/reviews?carId=42"));
    }

    @Test
    public void shouldSkipApprovedNotificationWhenRecipientIsBlank() {
        // Arrange
        final RecordingMailSender mailSender = new RecordingMailSender();
        final EmailServiceImpl emailService = new EmailServiceImpl(mailSender, userService, messageSource(), APP_BASE_URL);

        // Exercise
        emailService.sendCarApprovedNotification("   ", "Toyota", "Corolla", 42L);

        // Assertions
        assertTrue(mailSender.sentMessages.isEmpty());
    }

    @Test
    public void shouldSkipModeratorDigestWhenRecipientsAreEmpty() {
        // Arrange
        final RecordingMailSender mailSender = new RecordingMailSender();
        final EmailServiceImpl emailService = new EmailServiceImpl(mailSender, userService, messageSource(), APP_BASE_URL);

        // Exercise
        emailService.sendWeeklyModeratorDigest(List.of(), 3);

        // Assertions
        assertTrue(mailSender.sentMessages.isEmpty());
    }

    @Test
    public void shouldSendApprovedNotificationWithDefaultLocaleWhenRecipientLookupFails() throws Exception {
        // Arrange
        final String recipientEmail = "owner@example.com";
        final RecordingMailSender mailSender = new RecordingMailSender();
        final EmailServiceImpl emailService = new EmailServiceImpl(mailSender, userService, messageSource(), APP_BASE_URL);
        when(userService.findByEmail(recipientEmail))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        // Exercise
        emailService.sendCarApprovedNotification(recipientEmail, "Toyota", "Corolla", 42L);

        // Assertions
        assertEquals(1, mailSender.sentMessages.size());
        final MimeMessage message = mailSender.sentMessages.get(0);
        final Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        assertEquals(1, recipients.length);
        assertEquals(recipientEmail, recipients[0].toString());
        assertEquals("[La Posta Autos] Tu auto fue aprobado: Toyota Corolla", message.getSubject());
    }

    private static String extractText(final Part part) throws Exception {
        final Object content = part.getContent();
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof Multipart multipart) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                builder.append(extractText(multipart.getBodyPart(i)));
            }
            return builder.toString();
        }
        return "";
    }

    private static ResourceBundleMessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    private static final class RecordingMailSender implements JavaMailSender {

        private final List<MimeMessage> sentMessages = new ArrayList<>();

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance(new Properties()));
        }

        @Override
        public MimeMessage createMimeMessage(final InputStream contentStream) {
            try {
                return new MimeMessage(Session.getInstance(new Properties()), contentStream);
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to create test message.", e);
            }
        }

        @Override
        public void send(final MimeMessage mimeMessage) throws MailException {
            sentMessages.add(mimeMessage);
        }

        @Override
        public void send(final MimeMessage... mimeMessages) throws MailException {
            sentMessages.addAll(List.of(mimeMessages));
        }

        @Override
        public void send(final MimeMessagePreparator mimeMessagePreparator) throws MailException {
            final MimeMessage message = createMimeMessage();
            try {
                mimeMessagePreparator.prepare(message);
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to prepare test message.", e);
            }
            sentMessages.add(message);
        }

        @Override
        public void send(final MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            for (final MimeMessagePreparator preparator : mimeMessagePreparators) {
                send(preparator);
            }
        }

        @Override
        public void send(final org.springframework.mail.SimpleMailMessage simpleMessage) throws MailException {
            throw new UnsupportedOperationException("Simple mail is not used by EmailServiceImpl.");
        }

        @Override
        public void send(final org.springframework.mail.SimpleMailMessage... simpleMessages) throws MailException {
            throw new UnsupportedOperationException("Simple mail is not used by EmailServiceImpl.");
        }
    }
}
