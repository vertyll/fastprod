package com.vertyll.fastprod.email.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateNameTest {

    @Test
    void getName_ShouldReturnCorrectName() {
        // given
        EmailTemplateName templateName = EmailTemplateName.ACTIVATE_ACCOUNT;

        // when
        String name = templateName.getName();

        // then
        assertEquals("activate_account", name);
    }

    @Test
    void values_ShouldContainAllTemplates() {
        // when
        EmailTemplateName[] values = EmailTemplateName.values();

        // then
        assertEquals(1, values.length);
        assertTrue(containsTemplate(values));
    }

    @Test
    void valueOf_WithValidName_ShouldReturnCorrectEnum() {
        // when
        EmailTemplateName template = EmailTemplateName.valueOf("ACTIVATE_ACCOUNT");

        // then
        assertNotNull(template);
        assertEquals(EmailTemplateName.ACTIVATE_ACCOUNT, template);
    }

    @Test
    void valueOf_WithInvalidName_ShouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> EmailTemplateName.valueOf("INVALID_TEMPLATE"));
    }

    private boolean containsTemplate(EmailTemplateName[] values) {
        for (EmailTemplateName value : values) {
            if (value.getName().equals("activate_account")) {
                return true;
            }
        }
        return false;
    }
}