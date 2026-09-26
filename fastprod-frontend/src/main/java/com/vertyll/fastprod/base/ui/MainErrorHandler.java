package com.vertyll.fastprod.base.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinServiceInitListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MainErrorHandler {

    private static final String AN_UNEXPECTED_ERROR_HAS_OCCURRED_PLEASE_TRY_AGAIN_LATER =
            "An unexpected error has occurred. Please try again later.";

    @Bean
    @SuppressWarnings("FutureReturnValueIgnored")
    public VaadinServiceInitListener errorHandlerInitializer() {
        return event -> event.getSource()
            .addSessionInitListener(sessionInitEvent -> sessionInitEvent.getSession().setErrorHandler(errorEvent -> {
                log.error("An unexpected error occurred", errorEvent.getThrowable());
                errorEvent.getComponent().flatMap(Component::getUI).ifPresent(ui -> {
                    Notification notification =
                            new Notification(AN_UNEXPECTED_ERROR_HAS_OCCURRED_PLEASE_TRY_AGAIN_LATER);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setPosition(Notification.Position.TOP_CENTER);
                    notification.setDuration(3000);

                    ui.access(notification::open);
                });
            }));
    }
}
