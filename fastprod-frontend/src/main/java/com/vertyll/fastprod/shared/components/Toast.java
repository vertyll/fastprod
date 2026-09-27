package com.vertyll.fastprod.shared.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public final class Toast {

    private static final int DURATION_MS = 5000;

    private Toast() {
    }

    public static void show(String message, NotificationVariant variant) {
        Notification notification = new Notification();
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.setDuration(DURATION_MS);

        Div text = new Div();
        text.setText(message);
        text.getStyle().set("white-space", "normal").set("max-width", "400px").set("text-align", "center");

        notification.add(text);
        notification.open();
    }
}
