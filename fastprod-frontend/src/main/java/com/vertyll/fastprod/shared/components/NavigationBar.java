package com.vertyll.fastprod.shared.components;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vertyll.fastprod.modules.auth.views.LoginView;
import com.vertyll.fastprod.modules.auth.views.RegisterView;

public class NavigationBar extends HorizontalLayout {

    public NavigationBar() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);

        H3 logo = new H3("FastProd");

        HorizontalLayout navLinks = new HorizontalLayout();
        navLinks.setSpacing(true);

        RouterLink loginLink = new RouterLink("Login", LoginView.class);
        RouterLink registerLink = new RouterLink("Register", RegisterView.class);

        navLinks.add(loginLink, registerLink);

        add(logo, navLinks);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        setAlignItems(Alignment.CENTER);
    }
}
