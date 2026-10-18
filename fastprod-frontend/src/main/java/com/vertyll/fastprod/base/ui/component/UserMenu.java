package com.vertyll.fastprod.base.ui.component;

import java.io.Serial;
import java.util.List;

import com.vertyll.fastprod.shared.exception.ApiException;
import com.vertyll.fastprod.shared.i18n.I18n;
import com.vertyll.fastprod.shared.security.SecurityService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class UserMenu extends HorizontalLayout {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient SignedInPerson signedInPerson;
    private final transient SecurityService securityService;
    private final Avatar avatar;
    private final Span userName;

    public UserMenu(SignedInPerson signedInPerson, SecurityService securityService) {
        super();
        this.signedInPerson = signedInPerson;
        this.securityService = securityService;

        setAlignItems(FlexComponent.Alignment.CENTER);
        setSpacing(true);

        avatar = new Avatar();
        avatar.setColorIndex(1);
        avatar.getStyle().set("cursor", "pointer");

        userName = new Span();
        userName.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);

        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        menuBar.addClassName("user-menu");

        Div menuContainer = new Div();
        menuContainer.add(avatar);
        menuContainer.getStyle().set("cursor", "pointer");

        MenuItem menuItem = menuBar.addItem(menuContainer);
        SubMenu subMenu = menuItem.getSubMenu();

        Div profileHeader = new Div();
        profileHeader.getStyle()
            .set("padding", "var(--lumo-space-xs) 0 var(--lumo-space-s)")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("min-width", "250px")
            .set("flex", "1");

        userName.getStyle()
            .set("display", "block")
            .set("font-weight", "600")
            .set("color", "var(--lumo-primary-text-color)");

        Span userEmailSpan = new Span();
        userEmailSpan.getStyle()
            .set("display", "block")
            .set("font-size", "var(--lumo-font-size-xs)")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("margin-top", "var(--lumo-space-xs)");

        profileHeader.add(userName, userEmailSpan);
        MenuItem headerItem = subMenu.addItem(profileHeader);
        headerItem.setEnabled(false);

        MenuItem profileItem = subMenu.addItem(I18n.t("profile.title"), _ -> UI.getCurrent().navigate("profile"));
        profileItem.addComponentAsFirst(VaadinIcon.USER.create());

        MenuItem logoutItem = subMenu.addItem(I18n.t("nav.logout"), _ -> handleLogout());
        logoutItem.addComponentAsFirst(VaadinIcon.SIGN_OUT.create());

        // Lumo keeps room at an item's end for a submenu arrow; this menu has none, so both sides get the same space.
        for (MenuItem item : List.of(headerItem, profileItem, logoutItem)) {
            item.getElement().getStyle().set("padding-inline", "var(--lumo-space-xs)");
        }

        add(menuBar);

        loadUserData(userEmailSpan);
    }

    private void loadUserData(Span emailSpan) {
        try {
            SignedInPerson.Profile person = signedInPerson.current();
            avatar.setName(person.fullName());
            userName.setText(person.fullName());
            emailSpan.setText(person.email());
        } catch (ApiException e) {
            log.error("Failed to load user data", e);
        }
    }

    private void handleLogout() {
        securityService.logout();
    }
}
