package com.vertyll.fastprod.modules.legal.views;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Anchor;
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
}
