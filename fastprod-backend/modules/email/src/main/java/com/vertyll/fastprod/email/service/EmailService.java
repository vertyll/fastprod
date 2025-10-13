package com.vertyll.fastprod.email.service;

import com.vertyll.fastprod.email.enums.EmailTemplateName;
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
