package com.vertyll.fastprod.base.ui.component;

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
import com.vertyll.fastprod.modules.user.dto.UserProfileDto;
import com.vertyll.fastprod.modules.user.service.UserService;
import com.vertyll.fastprod.shared.dto.ApiResponse;
import com.vertyll.fastprod.shared.security.SecurityService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserMenu extends HorizontalLayout {

    private final UserService userService;
    private final SecurityService securityService;
    private final Avatar avatar;
    private final Span userName;

    public UserMenu(UserService userService, SecurityService securityService) {
        this.userService = userService;
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

        Div menuContainer = new Div();
        menuContainer.add(avatar);
        menuContainer.getStyle().set("cursor", "pointer");

        MenuItem menuItem = menuBar.addItem(menuContainer);
        SubMenu subMenu = menuItem.getSubMenu();

        Div profileHeader = new Div();
        profileHeader.getStyle()
                .set("padding", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("min-width", "250px");

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
        subMenu.addItem(profileHeader).setEnabled(false);

        MenuItem profileItem = subMenu.addItem("My Profile", _ ->
                UI.getCurrent().navigate("profile")
        );
        profileItem.addComponentAsFirst(VaadinIcon.USER.create());

        MenuItem logoutItem = subMenu.addItem("Logout", _ -> handleLogout());
        logoutItem.addComponentAsFirst(VaadinIcon.SIGN_OUT.create());

        add(menuBar);

        loadUserData(userEmailSpan);
    }

    private void loadUserData(Span emailSpan) {
        try {
            ApiResponse<UserProfileDto> response = userService.getCurrentUser();
            if (response.data() != null) {
                UserProfileDto user = response.data();
                String fullName = user.firstName() + " " + user.lastName();
                avatar.setName(fullName);
                userName.setText(fullName);
                emailSpan.setText(user.email());
            }
        } catch (Exception e) {
            log.error("Failed to load user data", e);
        }
    }

    private void handleLogout() {
        securityService.logout();
        UI.getCurrent().navigate("login");
    }
}
