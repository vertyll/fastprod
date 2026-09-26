package com.vertyll.fastprod.base.ui;

import java.util.concurrent.Future;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vertyll.fastprod.shared.i18n.I18n;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinServiceInitListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MainErrorHandler {

    private static final String AN_UNEXPECTED_ERROR_HAS_OCCURRED_PLEASE_TRY_AGAIN_LATER_KEY =
            "errors.common.unexpectedTryLater";

    @Bean
    public VaadinServiceInitListener errorHandlerInitializer() {
        return event -> event.getSource()
            .addSessionInitListener(sessionInitEvent -> sessionInitEvent.getSession().setErrorHandler(errorEvent -> {
                log.error("An unexpected error occurred", errorEvent.getThrowable());
                errorEvent.getComponent().flatMap(Component::getUI).ifPresent(ui -> {
                    Notification notification =
                            new Notification(I18n.t(AN_UNEXPECTED_ERROR_HAS_OCCURRED_PLEASE_TRY_AGAIN_LATER_KEY));
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setPosition(Notification.Position.TOP_CENTER);
                    notification.setDuration(3000);

                    Future<Void> _ = ui.access(notification::open);
                });
            }));
    }
}
