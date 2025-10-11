package com.vertyll.fastprod.email.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateNameTest {

    @Test
    void values_ShouldContainAllTemplates() {
        // when
        EmailTemplateName[] templates = EmailTemplateName.values();

        // then
        assertEquals(4, templates.length);
        assertTrue(containsTemplate(templates, EmailTemplateName.ACTIVATE_ACCOUNT));
        assertTrue(containsTemplate(templates, EmailTemplateName.CHANGE_EMAIL));
        assertTrue(containsTemplate(templates, EmailTemplateName.CHANGE_PASSWORD));
        assertTrue(containsTemplate(templates, EmailTemplateName.RESET_PASSWORD));
    }

    @Test
    void activateAccount_ShouldHaveCorrectName() {
        // when
        String name = EmailTemplateName.ACTIVATE_ACCOUNT.getName();

        // then
        assertEquals("activate_account", name);
    }

    @Test
    void changeEmail_ShouldHaveCorrectName() {
        // when
        String name = EmailTemplateName.CHANGE_EMAIL.getName();

        // then
        assertEquals("change_email", name);
    }

    @Test
    void changePassword_ShouldHaveCorrectName() {
        // when
        String name = EmailTemplateName.CHANGE_PASSWORD.getName();

        // then
        assertEquals("change_password", name);
    }

    @Test
    void resetPassword_ShouldHaveCorrectName() {
        // when
        String name = EmailTemplateName.RESET_PASSWORD.getName();

        // then
        assertEquals("reset_password", name);
    }

    @Test
    void valueOf_WithValidName_ShouldReturnCorrectEnum() {
        // when
        EmailTemplateName template = EmailTemplateName.valueOf("ACTIVATE_ACCOUNT");

        // then
        assertEquals(EmailTemplateName.ACTIVATE_ACCOUNT, template);
        assertEquals("activate_account", template.getName());
    }

    @Test
    void valueOf_WithInvalidName_ShouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> EmailTemplateName.valueOf("INVALID_TEMPLATE"));
    }

    @Test
    void allTemplates_ShouldHaveNonNullNames() {
        // when & then
        for (EmailTemplateName template : EmailTemplateName.values()) {
            assertNotNull(template.getName());
            assertFalse(template.getName().isEmpty());
        }
    }

    @Test
    void allTemplates_ShouldHaveUnderscoreInName() {
        // when & then
        for (EmailTemplateName template : EmailTemplateName.values()) {
            assertTrue(template.getName().contains("_"),
                    "Template name should contain underscore: " + template.getName());
        }
    }

    private boolean containsTemplate(EmailTemplateName[] templates, EmailTemplateName target) {
        for (EmailTemplateName template : templates) {
            if (template == target) {
                return true;
            }
        }
        return false;
    }
}
