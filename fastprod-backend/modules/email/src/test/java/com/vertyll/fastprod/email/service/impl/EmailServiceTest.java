package com.vertyll.fastprod.email.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.vertyll.fastprod.common.config.MailProperties;
import com.vertyll.fastprod.email.enums.EmailTemplateName;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;

    @Mock private SpringTemplateEngine templateEngine;

    @Mock private MailProperties mailProperties;

    @InjectMocks private EmailServiceImpl emailService;

    @Mock private MimeMessage mimeMessage;

    @Captor private ArgumentCaptor<Context> contextCaptor;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_CODE = "123456";
    private static final String TEST_SUBJECT = "Test Subject";

    @BeforeEach
    void setUp() {
        when(mailProperties.from()).thenReturn("test@example.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test Template</html>");
    }

    @Test
    void sendEmail_WhenAllParametersValid_ShouldSendEmail() throws MessagingException {
        // given
        EmailTemplateName templateName = EmailTemplateName.ACTIVATE_ACCOUNT;

        // when
        emailService.sendEmail(TEST_EMAIL, TEST_USERNAME, templateName, TEST_CODE, TEST_SUBJECT);

        // then
        verify(mailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq(templateName.getName()), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertNotNull(capturedContext.getVariable("username"));
        assertNotNull(capturedContext.getVariable("activation_code"));
        assertEquals(TEST_USERNAME, capturedContext.getVariable("username"));
        assertEquals(TEST_CODE, capturedContext.getVariable("activation_code"));
    }

    @Test
    void sendEmail_WhenTemplateNameNull_ShouldThrowException() {
        // when & then
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                emailService.sendEmail(
                                        TEST_EMAIL, TEST_USERNAME, null, TEST_CODE, TEST_SUBJECT));

        assertEquals("Email template cannot be null", exception.getMessage());
        verify(templateEngine, never()).process(anyString(), any(Context.class));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_ShouldSetCorrectEmailProperties() throws MessagingException {
        // given
        EmailTemplateName templateName = EmailTemplateName.CHANGE_EMAIL;

        // when
        emailService.sendEmail(TEST_EMAIL, TEST_USERNAME, templateName, TEST_CODE, TEST_SUBJECT);

        // then
        verify(templateEngine).process(eq(templateName.getName()), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertEquals(TEST_USERNAME, capturedContext.getVariable("username"));
        assertEquals(TEST_CODE, capturedContext.getVariable("activation_code"));
    }

    @Test
    void sendEmail_ShouldProcessCorrectTemplate() throws MessagingException {
        // given
        EmailTemplateName templateName = EmailTemplateName.RESET_PASSWORD;

        // when
        emailService.sendEmail(TEST_EMAIL, TEST_USERNAME, templateName, TEST_CODE, TEST_SUBJECT);

        // then
        verify(templateEngine).process(eq("reset_password"), any(Context.class));
    }

    @Test
    void sendEmail_ShouldHandleAllTemplateTypes() throws MessagingException {
        // Test all template types
        for (EmailTemplateName template : EmailTemplateName.values()) {
            clearInvocations(mailSender, templateEngine);

            // when
            emailService.sendEmail(TEST_EMAIL, TEST_USERNAME, template, TEST_CODE, TEST_SUBJECT);

            // then
            verify(templateEngine).process(eq(template.getName()), any(Context.class));
            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    @Test
    void sendEmail_ShouldIncludeActivationCodeInContext() throws MessagingException {
        // given
        String customCode = "CUSTOM123";

        // when
        emailService.sendEmail(
                TEST_EMAIL,
                TEST_USERNAME,
                EmailTemplateName.ACTIVATE_ACCOUNT,
                customCode,
                TEST_SUBJECT);

        // then
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        assertEquals(customCode, context.getVariable("activation_code"));
    }

    @Test
    void sendEmail_ShouldIncludeUsernameInContext() throws MessagingException {
        // given
        String customUsername = "customUser";

        // when
        emailService.sendEmail(
                TEST_EMAIL,
                customUsername,
                EmailTemplateName.ACTIVATE_ACCOUNT,
                TEST_CODE,
                TEST_SUBJECT);

        // then
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        Context context = contextCaptor.getValue();
        assertEquals(customUsername, context.getVariable("username"));
    }
}
