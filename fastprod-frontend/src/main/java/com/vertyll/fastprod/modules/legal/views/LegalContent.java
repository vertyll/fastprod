package com.vertyll.fastprod.modules.legal.views;

import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

final class LegalContent {

    private LegalContent() {
    }

    static void fill(VerticalLayout layout, String document) {
        layout.setMaxWidth("800px");
        layout.setPadding(true);
        layout.getStyle().set("margin", "0 auto");
        H2 title = new H2(title(document));
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        layout.add(title);
        for (String paragraph : I18n.t("legal." + document + ".content").split("\n\n")) {
            layout.add(new Paragraph(paragraph));
        }
    }

    static String title(String document) {
        return I18n.t("legal." + document + ".title");
    }
}
