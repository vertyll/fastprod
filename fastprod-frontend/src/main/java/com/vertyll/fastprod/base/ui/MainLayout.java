package com.vertyll.fastprod.base.ui;

import java.io.Serial;

import com.vertyll.fastprod.base.ui.component.LanguageSwitcher;
import com.vertyll.fastprod.base.ui.component.UserMenu;
import com.vertyll.fastprod.modules.user.service.UserService;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.security.RoleType;
import com.vertyll.fastprod.shared.security.SecurityService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
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

import lombok.extern.slf4j.Slf4j;

@Layout
@Slf4j
public final class MainLayout extends AppLayout {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String APP_NAME = "FastProd";

    private final transient SecurityService securityService;
    private final transient UserService userService;

    public MainLayout(SecurityService securityService, UserService userService) {
        super();
        this.securityService = securityService;
        this.userService = userService;

        if (securityService.isAuthenticated()) {
            createAuthenticatedLayout();
        } else {
            createPublicLayout();
        }
    }

    private void createAuthenticatedLayout() {
        setPrimarySection(Section.DRAWER);

        HorizontalLayout navbar = createAuthenticatedNavbar();
        addToNavbar(navbar);

        addToDrawer(createDrawerHeader(), new Scroller(createSideNav()));
    }

    private void createPublicLayout() {
        HorizontalLayout navbar = createPublicNavbar();
        addToNavbar(navbar);
    }

    private HorizontalLayout createAuthenticatedNavbar() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel(I18n.t("nav.menuToggle"));

        H1 viewTitle = new H1(APP_NAME);
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, LumoUtility.Margin.Left.MEDIUM);

        UserMenu userMenu = new UserMenu(userService, securityService);

        HorizontalLayout navbar = new HorizontalLayout(toggle, viewTitle, new LanguageSwitcher(), userMenu);
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.setWidthFull();
        navbar.expand(viewTitle);
        navbar.setPadding(true);
        navbar.setSpacing(true);
        navbar.addClassNames(LumoUtility.BoxShadow.SMALL, LumoUtility.Background.BASE);

        return navbar;
    }

    private HorizontalLayout createPublicNavbar() {
        H1 logo = new H1(APP_NAME);
        logo.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Button loginButton = new Button(I18n.t("nav.login"), VaadinIcon.SIGN_IN.create());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(_ -> UI.getCurrent().navigate("login"));

        Button registerButton = new Button(I18n.t("nav.register"));
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.addClickListener(_ -> UI.getCurrent().navigate("register"));

        HorizontalLayout authButtons = new HorizontalLayout(new LanguageSwitcher(), loginButton, registerButton);
        authButtons.setSpacing(true);
        authButtons.getStyle().set("flex-shrink", "0");

        HorizontalLayout navbar = new HorizontalLayout(logo, authButtons);
        navbar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        navbar.setWidthFull();
        navbar.expand(logo);
        navbar.setPadding(true);
        navbar.setSpacing(true);
        navbar.addClassNames(LumoUtility.BoxShadow.SMALL, LumoUtility.Background.BASE);
        navbar.addClassName("public-navbar");
        navbar.getStyle().set("flex-wrap", "wrap");

        return navbar;
    }

    private Div createDrawerHeader() {
        Icon appIcon = VaadinIcon.FACTORY.create();
        appIcon.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.IconSize.LARGE);

        Span appName = new Span(APP_NAME);
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);

        Div header = new Div(appIcon, appName);
        header.addClassNames(
            LumoUtility.Display.FLEX,
            LumoUtility.Padding.MEDIUM,
            LumoUtility.Gap.MEDIUM,
            LumoUtility.AlignItems.CENTER
        );

        return header;
    }

    private SideNav createSideNav() {
        SideNav nav = new SideNav();
        nav.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM);

        SideNavItem dashboard = new SideNavItem(I18n.t("nav.dashboard"), "/", VaadinIcon.DASHBOARD.create());
        nav.addItem(dashboard);

        if (securityService.hasAnyRole(RoleType.ADMIN, RoleType.MANAGER)) {
            SideNavItem adminSection = new SideNavItem(I18n.t("nav.administration"));
            adminSection.setPrefixComponent(VaadinIcon.COG.create());

            SideNavItem employeesLink =
                    new SideNavItem(I18n.t("nav.employees"), "employees", VaadinIcon.USERS.create());
            employeesLink.addItem(new SideNavItem(I18n.t("nav.employeeList"), "employees", VaadinIcon.LIST.create()));
            employeesLink
                .addItem(new SideNavItem(I18n.t("nav.employeeAdd"), "employees/form", VaadinIcon.PLUS.create()));

            adminSection.addItem(employeesLink);

            if (securityService.hasRole(RoleType.ADMIN)) {
                adminSection.addItem(
                    new SideNavItem(I18n.t("nav.translations"), "admin/translations", VaadinIcon.GLOBE.create())
                );
            }

            nav.addItem(adminSection);
        }

        return nav;
    }
}
