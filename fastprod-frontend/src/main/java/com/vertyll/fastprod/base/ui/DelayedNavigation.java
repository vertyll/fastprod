package com.vertyll.fastprod.base.ui;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DelayedNavigation {

    private DelayedNavigation() {
    }

    public static void navigate(UI ui, Class<? extends Component> target, Duration delay) {
        CompletableFuture.delayedExecutor(delay.toMillis(), TimeUnit.MILLISECONDS).execute(() -> {
            try {
                Future<Void> _ = ui.access(() -> ui.navigate(target));
            } catch (UIDetachedException e) {
                log.debug("Skipped delayed navigation for a detached UI", e);
            }
        });
    }
}
