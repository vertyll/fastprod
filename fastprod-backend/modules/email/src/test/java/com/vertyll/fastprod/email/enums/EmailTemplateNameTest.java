package com.vertyll.fastprod.email.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailTemplateNameTest {
    @Test
    void values_ShouldContainAllTemplates() {
        EmailTemplateName[] templates = EmailTemplateName.values();

        assertEquals(4, templates.length);
        assertTrue(containsTemplate(templates, EmailTemplateName.ACTIVATE_ACCOUNT));
        assertTrue(containsTemplate(templates, EmailTemplateName.CHANGE_EMAIL));
        assertTrue(containsTemplate(templates, EmailTemplateName.CHANGE_PASSWORD));
        assertTrue(containsTemplate(templates, EmailTemplateName.RESET_PASSWORD));
    }

    @Test
    void activateAccount_ShouldHaveCorrectName() {
        String name = EmailTemplateName.ACTIVATE_ACCOUNT.getName();

        assertEquals("activate_account", name);
    }

    @Test
    void changeEmail_ShouldHaveCorrectName() {
        String name = EmailTemplateName.CHANGE_EMAIL.getName();

        assertEquals("change_email", name);
    }

    @Test
    void changePassword_ShouldHaveCorrectName() {
        String name = EmailTemplateName.CHANGE_PASSWORD.getName();

        assertEquals("change_password", name);
    }

    @Test
    void resetPassword_ShouldHaveCorrectName() {
        String name = EmailTemplateName.RESET_PASSWORD.getName();

        assertEquals("reset_password", name);
    }

    @Test
    void valueOf_WithValidName_ShouldReturnCorrectEnum() {
        EmailTemplateName template = EmailTemplateName.valueOf("ACTIVATE_ACCOUNT");

        assertEquals(EmailTemplateName.ACTIVATE_ACCOUNT, template);
        assertEquals("activate_account", template.getName());
    }

    @Test
    void valueOf_WithInvalidName_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> EmailTemplateName.valueOf("INVALID_TEMPLATE"));
    }

    @Test
    void allTemplates_ShouldHaveNonNullNames() {
        for (EmailTemplateName template : EmailTemplateName.values()) {
            assertNotNull(template.getName());
            assertFalse(template.getName().isEmpty());
        }
    }

    @Test
    void allTemplates_ShouldHaveUnderscoreInName() {
        for (EmailTemplateName template : EmailTemplateName.values()) {
            assertTrue(
                template.getName().contains("_"),
                "Template name should contain underscore: " + template.getName()
            );
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
