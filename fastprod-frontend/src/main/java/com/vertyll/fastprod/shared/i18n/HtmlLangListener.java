package com.vertyll.fastprod.shared.i18n;

import java.io.Serial;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
public class HtmlLangListener implements VaadinServiceInitListener {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();
            ui.getPage().executeJs("document.documentElement.lang = $0", ui.getLocale().getLanguage());
        });
    }
}
