package com.vertyll.fastprod.shared.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vertyll.fastprod.shared.security.SecurityBeforeEnterListener;
import com.vertyll.fastprod.shared.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringComponent
@Slf4j
@RequiredArgsConstructor
public class SecurityConfiguration implements VaadinServiceInitListener {

    private final transient SecurityService securityService;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            // Create a new instance of the listener for each UI
            SecurityBeforeEnterListener listener = new SecurityBeforeEnterListener(securityService);
            uiEvent.getUI().addBeforeEnterListener(listener);
            log.debug("Security listener registered for UI");
        });
    }
}
