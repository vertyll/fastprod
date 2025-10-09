package com.vertyll.fastprod.email;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String activationCode,
            String subject
    ) throws MessagingException;
}
