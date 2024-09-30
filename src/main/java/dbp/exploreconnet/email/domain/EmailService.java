package dbp.exploreconnet.email.domain;

import dbp.exploreconnet.reservation.domain.Reservation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    public void correoSingIn(String to, String name) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);

        String process = templateEngine.process("sing-in-template.html", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        helper.setTo(to);
        helper.setText(process, true);
        helper.setSubject("Te damos la Bienvenida en Explore Connect!");

        mailSender.send(message);
    }
    public void sendReservationQRCode(String to, String name, Reservation reservation, String qrCodeUrl) throws MessagingException {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("reservationId", reservation.getId());
        context.setVariable("reservationDate", reservation.getDate());
        context.setVariable("numberOfPeople", reservation.getNumberOfPeople());
        context.setVariable("placeName", reservation.getPlace().getName());
        context.setVariable("qrCodeUrl", qrCodeUrl);  // Incluir la URL del código QR

        String process = templateEngine.process("reservation-qr-template.html", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        helper.setTo(to);
        helper.setText(process, true);
        helper.setSubject("Tu Reserva en Explore Connect - Código QR");

        mailSender.send(message);
    }
}