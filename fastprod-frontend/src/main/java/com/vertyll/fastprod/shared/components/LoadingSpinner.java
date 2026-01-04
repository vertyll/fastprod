package com.vertyll.fastprod.shared.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.progressbar.ProgressBar;

public class LoadingSpinner extends Div {

    private static final String CENTER = "center";

    public LoadingSpinner() {
        this(false);
    }

    public LoadingSpinner(boolean fullScreen) {
        addClassName("loading-spinner-container");

        Div overlay = new Div();
        overlay.addClassName("loading-overlay");
        
        if (fullScreen) {
            overlay.getStyle()
                    .set("position", "fixed")
                    .set("top", "0")
                    .set("left", "0")
                    .set("width", "100vw")
                    .set("height", "100vh")
                    .set("background", "rgba(255, 255, 255, 0.8)")
                    .set("z-index", "9999")
                    .set("display", "flex")
                    .set("align-items", CENTER)
                    .set("justify-content", CENTER);
        } else {
            overlay.getStyle()
                    .set("position", "absolute")
                    .set("top", "0")
                    .set("left", "0")
                    .set("width", "100%")
                    .set("height", "100%")
                    .set("background", "rgba(255, 255, 255, 0.8)")
                    .set("z-index", "100")
                    .set("display", "flex")
                    .set("align-items", CENTER)
                    .set("justify-content", CENTER);
        }

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setWidth("200px");

        overlay.add(progressBar);
        add(overlay);
        setVisible(false);
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }
}
