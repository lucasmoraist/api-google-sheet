package com.lucasmoraist.register_telecentro.service;

import com.lucasmoraist.register_telecentro.exceptions.SendMailException;
import com.lucasmoraist.register_telecentro.model.Person;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service class responsible for sending emails to people who registered for a course.
 * It uses the JavaMailSender class to send emails.
 *
 * @author lucasmoraist
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * Sends an email to the person who registered for the course.
     *
     * @param person the person who registered for the course
     * @throws MessagingException if an error occurs while sending the email
     */
    public void sendEmail(Person person) throws MessagingException {
        LocalDateTime now = LocalDateTime.now();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(person.getEmail());
        helper.setSubject("Inscrição no curso de %s feita com sucesso!".formatted(person.getCourse().getNameCourse()));

        String htmlContent = """
                <html>
                <body>
                    <h2>Curso de <strong>%s</strong></h2>
                    <p>Data e hora: <strong>%s</strong></p>
                            
                    <p>Abraços,</p>
                    <p>Agente de Inclusão Digital<br>(011) 5667-6272</p>
                            
                    <i>Data de envio: %s</>
                </body>
                </html>
                """.formatted(
                person.getCourse().getNameCourse(),
                person.getCourse().getDateAndTime(),
                now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );


        helper.setText(htmlContent, true);

        helper.setFrom(from);
        mailSender.send(message);

    }

}
