package com.hematrace.hematrace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void envoyerMailActivation(String destinataire, String prenom, String lienActivation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(destinataire);
        message.setSubject("Activation de votre compte HemaTrace");
        message.setText(
                "Bonjour " + prenom + ",\n\n" +
                "Un compte HemaTrace a été créé pour vous.\n" +
                "Veuillez cliquer sur le lien suivant pour définir votre mot de passe et activer votre compte :\n\n" +
                lienActivation + "\n\n" +
                "Ce lien expire dans 24 heures.\n\n" +
                "Si vous n'êtes pas concerné, veuillez contacter l'administrateur."
        );
        mailSender.send(message);
    }
}