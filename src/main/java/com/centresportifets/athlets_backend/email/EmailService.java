package com.centresportifets.athlets_backend.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendActivationEmail(String to, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Activation de votre compte AthlETS");
        message.setText(
                "Bonjour,\n\n"
                        + "Votre compte AthlETS a été créé.\n\n"
                        + "Pour l'activer, cliquez sur le lien suivant :\n"
                        + activationLink
                        + "\n\n"
                        + "Ce lien est valide pendant 24 heures.\n\n"
                        + "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer ce message.\n\n"
                        + "L'équipe AthlETS"
        );

        mailSender.send(message);
    }
}