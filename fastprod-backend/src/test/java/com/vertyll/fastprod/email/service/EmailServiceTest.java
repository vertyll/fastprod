package com.vertyll.fastprod.email.service;

import com.vertyll.fastprod.email.enums.EmailTemplateName;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @Captor
    private ArgumentCaptor<Context> contextCaptor;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_CODE = "123456";
    private static final String TEST_SUBJECT = "Test Subject";

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test Template</html>");
    }

    @Test
    void sendEmail_WhenAllParametersValid_ShouldSendEmail() throws MessagingException {
        // given
        EmailTemplateName templateName = EmailTemplateName.ACTIVATE_ACCOUNT;

        // when
        emailService.sendEmail(TEST_EMAIL, TEST_USERNAME, templateName, TEST_CODE, TEST_SUBJECT);

        // then
        verify(mailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq(templateName.name()), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        assertNotNull(capturedContext.getVariable("username"));
        assertNotNull(capturedContext.getVariable("activation_code"));
        assertEquals(TEST_USERNAME, capturedContext.getVariable("username"));
        assertEquals(TEST_CODE, capturedContext.getVariable("activation_code"));
    }

    @Test
    void sendEmail_WhenTemplateNameNull_ShouldUseDefaultTemplate() throws MessagingException {
        // when
        emailService.sendEmail(TEST_EMAIL, TEST_USERNAME, null, TEST_CODE, TEST_SUBJECT);

        // then
        verify(mailSender).send(any(MimeMessage.class));
        verify(templateEngine).process(eq("confirm-email"), any(Context.class));
    }

    @Test
    void sendEmail_WhenSendingFails_ShouldPropagateException() {
        // given
        doThrow(new RuntimeException("Failed to send email"))
                .when(mailSender).send(any(MimeMessage.class));

        // when & then
        assertThrows(RuntimeException.class, () ->
                emailService.sendEmail(TEST_EMAIL, TEST_USERNAME, EmailTemplateName.ACTIVATE_ACCOUNT, TEST_CODE, TEST_SUBJECT)
        );
    }
}