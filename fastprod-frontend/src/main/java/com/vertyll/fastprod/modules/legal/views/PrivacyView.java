package com.vertyll.fastprod.modules.legal.views;

import java.io.Serial;

import com.vertyll.fastprod.base.ui.MainLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "privacy", layout = MainLayout.class)
@AnonymousAllowed
public final class PrivacyView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    public PrivacyView() {
        super();
        LegalContent.fill(this, "privacy");
    }

    @Override
    public String getPageTitle() {
        return LegalContent.title("privacy");
    }
}
