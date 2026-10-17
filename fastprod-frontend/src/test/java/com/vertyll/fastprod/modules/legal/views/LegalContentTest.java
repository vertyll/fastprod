package com.vertyll.fastprod.modules.legal.views;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;

import static org.assertj.core.api.Assertions.assertThat;

class LegalContentTest {

    @Test
    void anEmailAddressBecomesAMailtoLink() {
        Paragraph paragraph = LegalContent.paragraph("Kontakt: gawrmiko@gmail.com.");

        Optional<Anchor> link =
                paragraph.getChildren().filter(Anchor.class::isInstance).map(Anchor.class::cast).findFirst();

        assertThat(link).isPresent();
        assertThat(link.get().getHref()).isEqualTo("mailto:gawrmiko@gmail.com");
        assertThat(paragraph.getChildren().count()).isEqualTo(3);
    }

    @Test
    void textWithoutAnAddressStaysPlain() {
        assertThat(LegalContent.paragraph("Bez adresu.").getChildren().filter(Anchor.class::isInstance).count())
            .isZero();
    }

    @Test
    void aSectionIsItsHeadingFollowedByItsText() {
        List<Component> section = LegalContent.section("1. Administrator danych", "Administratorem danych jest autor.");

        assertThat(section).hasSize(2);
        assertThat(section.get(0)).isInstanceOfSatisfying(
            H3.class,
            heading -> assertThat(heading.getText()).isEqualTo("1. Administrator danych")
        );
        assertThat(section.get(1)).isInstanceOfSatisfying(
            Paragraph.class,
            paragraph -> assertThat(paragraph.getText()).isEqualTo("Administratorem danych jest autor.")
        );
    }
}
