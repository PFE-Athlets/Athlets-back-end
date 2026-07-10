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

    /**
     * Sends an account activation email to the specified recipient.
     *
     * @param to             the recipient's email address
     * @param activationLink the activation link to be included in the email
     */
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

    /**
     * Sends a password reset email to the specified recipient.
     *
     * @param to        the recipient's email address
     * @param resetLink the password reset link to be included in the email
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe AthlETS");
        message.setText(
                "Bonjour,\n\n"
                        + "Une demande de réinitialisation de mot de passe a été effectuée pour votre compte AthlETS.\n\n"
                        + "Pour choisir un nouveau mot de passe, cliquez sur le lien suivant :\n"
                        + resetLink
                        + "\n\n"
                        + "Ce lien est valide pendant 1 heure.\n\n"
                        + "Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer ce message.\n\n"
                        + "L'équipe AthlETS"
        );

        mailSender.send(message);
    }
}