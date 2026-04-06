package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserService userService;

    @Autowired
    public EmailServiceImpl(final JavaMailSender mailSender, final UserService userService) {
        this.mailSender = mailSender;
        this.userService = userService;
    }

    @Override
    public void sendCarCreatedNotification(final Car car) {
        final List<String> moderators = userService.getModeratorsEmails();
        if (moderators.isEmpty()) {
            return;
        }

        final SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(moderators.toArray(new String[0]));
        message.setSubject("Nuevo auto agregado: " + car.getBrandName() + " " + car.getModel());
        message.setText(
                "Se agregó un nuevo auto al catálogo.\n\n"
                        + "Marca: " + car.getBrandName() + "\n"
                        + "Modelo: " + car.getModel() + "\n"
                        + "Carrocería: " + car.getBodyType() + "\n"
        );

        mailSender.send(message);
    }
}
