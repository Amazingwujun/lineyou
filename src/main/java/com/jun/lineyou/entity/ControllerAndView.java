package com.jun.lineyou.entity;

import javafx.scene.Parent;

public final class ControllerAndView<C, V extends Parent> {

    private final C controller;

    private final V view;

    public ControllerAndView(C controller, V view) {
        this.controller = controller;
        this.view = view;
    }

    public C getController() {
        return controller;
    }

    public V getView() {
        return view;
    }
}
