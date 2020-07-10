package com.jun.lineyou.ui.controller;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.List;

/**
 * 用于处理窗口拖动
 *
 * @author Jun
 * @date 2020-07-01 15:33
 */
public abstract class AbstractDragController {

    private double offsetX, offsetY;

    /**
     * 返回当前控制器的 stage
     */
    abstract Stage currentStage();

    /**
     * 处理鼠标拖拽事件
     *
     * @param event 鼠标事件
     */
    protected void handleMouseEvent(MouseEvent event) {
        EventType<? extends MouseEvent> eventType = event.getEventType();
        if (eventType == MouseEvent.MOUSE_PRESSED) {
            event.consume();
            this.offsetX = event.getScreenX() - currentStage().getX();
            this.offsetY = event.getScreenY() - currentStage().getY();
        } else if (eventType == MouseEvent.MOUSE_DRAGGED) {
            event.consume();
            double mouseX = event.getScreenX();
            double mouseY = event.getScreenY();
            currentStage().setX(mouseX - offsetX);
            currentStage().setY(mouseY - offsetY);
        }
    }

    protected void initDrag(List<Node> nodes){
        nodes.forEach(node -> {
            node.setOnMousePressed(this::handleMouseEvent);
            node.setOnMouseDragged(this::handleMouseEvent);
        });
    }
}
