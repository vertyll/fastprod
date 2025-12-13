package com.vertyll.fastprod.modules.home.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vertyll.fastprod.base.ui.MainLayout;
import com.vertyll.fastprod.shared.security.SecurityService;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | FastProd")
public class HomeView extends VerticalLayout {

    public HomeView(SecurityService securityService) {
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
        H2 title = new H2("Dashboard");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        H3 welcome = new H3("Welcome to FastProd!");
        Paragraph description = new Paragraph("This is your main dashboard. Navigate using the side menu to access different features.");

        add(title, welcome, description);
    }

    private void createPublicView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Welcome to FastProd");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

        Paragraph tagline = new Paragraph("Your production management solution");
        tagline.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.TextColor.SECONDARY);

        Button loginButton = new Button("Sign In", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));

        Button registerButton = new Button("Sign Up");
        registerButton.addThemeVariants(ButtonVariant.LUMO_LARGE);
        registerButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));

        HorizontalLayout buttonLayout = new HorizontalLayout(loginButton, registerButton);
        buttonLayout.setSpacing(true);
        buttonLayout.addClassNames(LumoUtility.Margin.Top.MEDIUM);

        add(title, tagline, buttonLayout);
    }
}
