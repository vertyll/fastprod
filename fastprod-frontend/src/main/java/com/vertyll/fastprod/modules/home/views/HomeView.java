package com.vertyll.fastprod.modules.home.views;

import java.io.Serial;

import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.security.SecurityService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route(value = "", layout = MainLayout.class)
public final class HomeView extends VerticalLayout implements HasDynamicTitle {
    @Serial
    private static final long serialVersionUID = 1L;

    public HomeView(SecurityService securityService) {
        super();
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        if (securityService.isAuthenticated()) {
            createAuthenticatedView();
        } else {
            createPublicView();
        }
    }

    private void createAuthenticatedView() {
        H2 title = new H2(I18n.t("home.dashboard"));
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        H3 welcome = new H3(I18n.t("home.welcomeUser"));
        Paragraph description = new Paragraph(I18n.t("home.description"));

        add(title, welcome, description);
    }

    private void createPublicView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2(I18n.t("home.welcomeGuest"));
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        Paragraph tagline = new Paragraph(I18n.t("home.tagline"));
        tagline.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY);

        Button loginButton = new Button(I18n.t("nav.login"), VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.addClickListener(_ -> getUI().ifPresent(ui -> ui.navigate("login")));

        Button registerButton = new Button(I18n.t("nav.register"));
        registerButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
        registerButton.addClickListener(_ -> getUI().ifPresent(ui -> ui.navigate("register")));

        HorizontalLayout buttonLayout = new HorizontalLayout(loginButton, registerButton);
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("flex-wrap", "wrap").set("justify-content", "center");
        buttonLayout.addClassNames(LumoUtility.Margin.Top.MEDIUM);

        add(title, tagline, buttonLayout);
    }

    @Override
    public String getPageTitle() {
        return I18n.t("pages.dashboard");
    }
}
