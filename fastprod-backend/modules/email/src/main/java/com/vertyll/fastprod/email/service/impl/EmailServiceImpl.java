package com.vertyll.fastprod.email.service.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

import com.vertyll.fastprod.sharedinfrastructure.config.MailProperties;
import com.vertyll.fastprod.email.enums.EmailTemplateName;
import com.vertyll.fastprod.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@Slf4j
@RequiredArgsConstructor
class EmailServiceImpl implements EmailService {

    private static final String EMAIL_TEMPLATE_CANNOT_BE_NULL = "Email template cannot be null";
    private static final String FAILED_TO_PROCESS_EMAIL_TEMPLATE = "Failed to process email template: ";
    private static final String FAILED_TO_SEND_EMAIL_WITH_TEMPLATE = "Failed to send email with template: ";

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final MailProperties mailProperties;

    @Override
    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String activationCode,
            String subject
    ) throws MessagingException {
        if (emailTemplate == null) {
            throw new IllegalArgumentException(EMAIL_TEMPLATE_CANNOT_BE_NULL);
        }

        String templateName = emailTemplate.getName();
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());

        Map<String, Object> properties = new ConcurrentHashMap<>();
        properties.put("username", username);
        properties.put("activation_code", activationCode);

        Context context = new Context();
        context.setVariables(properties);

        helper.setFrom(mailProperties.from());
        helper.setTo(to);
        helper.setSubject(subject);

        try {
            String template = templateEngine.process(templateName, context);
            helper.setText(template, true);
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {} with template: {}", to, templateName);
        } catch (TemplateEngineException e) {
            log.error("Failed to process email template: {} for recipient: {}", templateName, to, e);
            throw new MessagingException(FAILED_TO_PROCESS_EMAIL_TEMPLATE + templateName, e);
        } catch (MailException e) {
            log.error("Failed to send email to: {} with template: {}", to, templateName, e);
            throw new MessagingException(FAILED_TO_SEND_EMAIL_WITH_TEMPLATE + templateName, e);
        } catch (MessagingException e) {
            log.error("Failed to prepare email message for: {} with template: {}", to, templateName, e);
            throw e; // Re-throw since the method already declares this exception
        }
    }
}
