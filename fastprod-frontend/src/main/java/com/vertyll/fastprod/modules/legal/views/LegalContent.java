package com.vertyll.fastprod.modules.legal.views;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

final class LegalContent {
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w.-]+\\w");
    private static final Pattern PARAGRAPHS = Pattern.compile("\n\n");

    private LegalContent() {
    }

    static void fill(VerticalLayout layout, String document) {
        layout.setMaxWidth("800px");
        layout.setPadding(true);
        layout.getStyle().set("margin", "0 auto");
        H2 title = new H2(title(document));
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        layout.add(title);
        PARAGRAPHS.splitAsStream(I18n.t("legal." + document + ".content"))
            .map(LegalContent::paragraph)
            .forEach(layout::add);
    }

    static Paragraph paragraph(String text) {
        Paragraph paragraph = new Paragraph();
        Matcher email = EMAIL.matcher(text);
        int last = 0;
        while (email.find()) {
            if (email.start() > last) {
                paragraph.add(new Text(text.substring(last, email.start())));
            }
            paragraph.add(new Anchor("mailto:" + email.group(), email.group()));
            last = email.end();
        }
        if (last < text.length()) {
            paragraph.add(new Text(text.substring(last)));
        }
        return paragraph;
    }

    static String title(String document) {
        return I18n.t("legal." + document + ".title");
    }
}
