package com.jun.lineyou.ui.controller;

import com.jfoenix.controls.JFXListView;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.InnerMsg;
import com.jun.lineyou.utils.ViewContainer;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 主页面控制器
 *
 * @author Jun
 * @date 2020-06-30 19:52
 */
@Slf4j
@ViewController(fxml = ViewFxml.MAIN)
public class MainController extends AbstractDragController implements Listener {

    public Label close;
    public AnchorPane topPane;

    public JFXListView<AnchorPane> viewList;
    public AnchorPane dialogPane;

    public JFXListView<AnchorPane> msgView;
    public JFXListView<AnchorPane> friendsView;

    public void initialize() {
        log.info("mainController init");

//        initDrag(Arrays.asList(topPane));
    }


    @Override
    Stage currentStage() {
        return ViewContainer.MAIN;
    }


    @Override
    public void action(Object msg) {
        Platform.runLater(() -> {
            if (msg instanceof InnerMsg) {
                Label msgLabel = new Label();
                Label timeLabel = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")));
                timeLabel.setStyle("-fx-padding: 1;-fx-text-fill: white;-fx-font-style: bold;-fx-font-size: 11px;-fx-background-color: #bdbdbd;-fx-font-family: Consolas;-fx-background-radius: 3px;");

                ImageView avatar = new ImageView();
                avatar.setFitWidth(30);
                avatar.setFitHeight(30);

                //锚定消息
                AnchorPane.setLeftAnchor(timeLabel, (double) 40);
                AnchorPane.setLeftAnchor(msgLabel, (double) 40);
                AnchorPane.setTopAnchor(msgLabel, (double) 20);

                msgLabel.setWrapText(true);
                msgLabel.setPrefHeight(Label.USE_COMPUTED_SIZE);
                msgLabel.setPrefWidth(Label.USE_COMPUTED_SIZE);
                msgLabel.maxWidthProperty().bind(msgView.widthProperty().divide(2.2));
                msgLabel.setStyle("-fx-padding: 5,3,3,3;-fx-background-radius: 5px;-fx-background-color: #ff846b;-fx-font-family: 'Microsoft JhengHei';-fx-font-size: 12px");
                msgLabel.setText(((InnerMsg) msg).getMsg());

                AnchorPane chatPane = new AnchorPane();
                chatPane.getChildren().addAll(avatar,msgLabel,timeLabel);
                avatar.setImage(new Image(getClass().getResourceAsStream("/img/avatar.jpg")));

                msgView.getItems().add(chatPane);
            }
        });
    }
}
