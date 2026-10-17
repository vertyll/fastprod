package com.vertyll.fastprod.modules.legal.views;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

final class LegalContent {
    /** The sections of each document: `legal.<document>.h<n>` and `p<n>`, the heading and paragraph of section n. */
    private static final Map<String, Integer> SECTIONS = Map.of("terms", 5, "privacy", 8);

    private LegalContent() {
    }

    static void fill(VerticalLayout layout, String document) {
        layout.setMaxWidth("800px");
        layout.setPadding(true);
        layout.getStyle().set("margin", "0 auto");
        Button back = new Button(I18n.t("common.back"), VaadinIcon.ARROW_LEFT.create());
        back.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        back.addClickListener(_ -> UI.getCurrent().getPage().getHistory().back());
        layout.add(back);
        H2 title = new H2(title(document));
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);
        Span modified = new Span(I18n.t("legal.lastModified") + ": " + I18n.t(key(document, "lastModified")));
        modified.addClassNames(
            LumoUtility.FontSize.SMALL,
            LumoUtility.TextColor.SECONDARY,
            LumoUtility.Margin.Bottom.MEDIUM
        );
        layout.add(title, modified);
        int sections = Objects.requireNonNull(SECTIONS.get(document), () -> "no sections for " + document);
        for (int number = 1; number <= sections; number++) {
            layout.add(section(I18n.t(key(document, "h" + number)), I18n.t(key(document, "p" + number))));
        }
    }

    static List<Component> section(String heading, String text) {
        H3 title = new H3(heading);
        title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.Bottom.NONE);
        return List.of(title, paragraph(text));
    }

    static Paragraph paragraph(String text) {
        Paragraph paragraph = new Paragraph();
        int last = 0;
        for (int at = text.indexOf('@'); at != -1; at = text.indexOf('@', Math.max(at + 1, last))) {
            int start = emailStart(text, at, last);
            int end = emailEnd(text, at);
            if (start == at || end == at + 1) {
                continue;
            }
            if (start > last) {
                paragraph.add(new Text(text.substring(last, start)));
            }
            String email = text.substring(start, end);
            paragraph.add(new Anchor("mailto:" + email, email));
            last = end;
        }
        if (last < text.length()) {
            paragraph.add(new Text(text.substring(last)));
        }
        return paragraph;
    }

    private static int emailStart(String text, int at, int from) {
        int start = at;
        while (start > from && isLocalPart(text.charAt(start - 1))) {
            start--;
        }
        return start;
    }

    private static int emailEnd(String text, int at) {
        int end = at + 1;
        while (end < text.length() && isDomain(text.charAt(end))) {
            end++;
        }
        while (end > at + 1 && !isWord(text.charAt(end - 1))) {
            end--;
        }
        return end;
    }

    private static boolean isWord(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static boolean isDomain(char c) {
        return isWord(c) || c == '.' || c == '-';
    }

    private static boolean isLocalPart(char c) {
        return isDomain(c) || c == '+';
    }

    static String title(String document) {
        return I18n.t(key(document, "title"));
    }

    private static String key(String document, String part) {
        return "legal." + document + "." + part;
    }
}
