package com.vertyll.fastprod.base.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vertyll.fastprod.shared.security.SecurityService;
import lombok.extern.slf4j.Slf4j;

@Layout
@Slf4j
public final class MainLayout extends AppLayout {

    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;

        if (securityService.isAuthenticated()) {
            createAuthenticatedLayout();
        } else {
            createPublicLayout();
        }
    }

    /**
     * Create layout for authenticated users (with side navigation)
     */
    private void createAuthenticatedLayout() {
        setPrimarySection(Section.DRAWER);

        // Create navbar
        HorizontalLayout navbar = createAuthenticatedNavbar();
        addToNavbar(navbar);

        // Create drawer
        addToDrawer(createDrawerHeader(), new Scroller(createSideNav()));
    }

    /**
     * Create layout for public users (with top navigation only)
     */
    private void createPublicLayout() {
        HorizontalLayout navbar = createPublicNavbar();
        addToNavbar(navbar);
    }

    /**
     * Create navbar for authenticated users
     */
    private HorizontalLayout createAuthenticatedNavbar() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        H1 viewTitle = new H1("FastProd");
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, LumoUtility.Margin.Left.MEDIUM);

        Avatar avatar = new Avatar("User");
        avatar.setColorIndex(1);

        Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> handleLogout());

        HorizontalLayout userMenu = new HorizontalLayout(avatar, logoutButton);
        userMenu.setAlignItems(FlexComponent.Alignment.CENTER);
        userMenu.setSpacing(true);

        HorizontalLayout navbar = new HorizontalLayout(toggle, viewTitle, userMenu);
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.setWidthFull();
        navbar.expand(viewTitle);
        navbar.setPadding(true);
        navbar.setSpacing(true);
        navbar.addClassNames(LumoUtility.BoxShadow.SMALL, LumoUtility.Background.BASE);

        return navbar;
    }

    /**
     * Create navbar for public users
     */
    private HorizontalLayout createPublicNavbar() {
        H1 logo = new H1("FastProd");
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Button loginButton = new Button("Login", VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> UI.getCurrent().navigate("login"));

        Button registerButton = new Button("Register");
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.addClickListener(e -> UI.getCurrent().navigate("register"));

        HorizontalLayout authButtons = new HorizontalLayout(loginButton, registerButton);
        authButtons.setSpacing(true);

        HorizontalLayout navbar = new HorizontalLayout(logo, authButtons);
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.setWidthFull();
        navbar.expand(logo);
        navbar.setPadding(true);
        navbar.setSpacing(true);
        navbar.addClassNames(LumoUtility.BoxShadow.SMALL, LumoUtility.Background.BASE);

        return navbar;
    }

    /**
     * Create drawer header
     */
    private Div createDrawerHeader() {
        Icon appIcon = VaadinIcon.FACTORY.create();
        appIcon.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.IconSize.LARGE);

        Span appName = new Span("FastProd");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);

        Div header = new Div(appIcon, appName);
        header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM, LumoUtility.AlignItems.CENTER);

        return header;
    }

    /**
     * Create side navigation menu
     */
    private SideNav createSideNav() {
        SideNav nav = new SideNav();
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);

        // Dashboard
        SideNavItem dashboard = new SideNavItem("Dashboard", "/", VaadinIcon.DASHBOARD.create());

        nav.addItem(dashboard);

        return nav;
    }

    /**
     * Handle logout
     */
    private void handleLogout() {
        securityService.logout();
        UI.getCurrent().getPage().reload();
    }
}
