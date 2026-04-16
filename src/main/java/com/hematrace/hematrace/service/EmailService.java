package com.hematrace.hematrace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    public void envoyerMailActivation(String destinataire, String prenom, String lienActivation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinataire);
        message.setSubject("Activation de votre compte HemaTrace");
        message.setText(
                "Bonjour " + prenom + ",

" +
                "Un compte HemaTrace a été créé pour vous.
" +
                "Veuillez cliquer sur le lien suivant pour définir votre mot de passe et activer votre compte :

" +
                lienActivation + "

" +
                "Ce lien expire dans 24 heures.

" +
                "Si vous n'êtes pas concerné, veuillez contacter l'administrateur."
        );
        mailSender.send(message);
    }
}
