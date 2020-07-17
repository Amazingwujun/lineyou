package com.jun.lineyou.utils;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 用于生成聊天背景
 *
 * @author Jun
 * @date 2020-07-14 11:35
 */
public class PaneUtils {

    public static AnchorPane friend(Image image, String mobile, String nickname, Boolean isOnline) {
        AnchorPane pane = new AnchorPane();
        pane.setId(mobile);
        ImageView avatar = new ImageView();
        Label nicknameLabel = new Label();
        Circle online = new Circle();

        //css
        pane.getStylesheets().add(PaneUtils.class.getResource("/css/chat.css").toExternalForm());
        nicknameLabel.getStyleClass().add("nickname");

        //头像
        avatar.setFitHeight(35);
        avatar.setFitWidth(35);
        avatar.setImage(image);

        //昵称
        AnchorPane.setLeftAnchor(nicknameLabel, (double) 45);
        nicknameLabel.setText(StringUtils.isEmpty(nickname) ? mobile : nickname);

        //在线状态
        AnchorPane.setLeftAnchor(online, (double) 48);
        AnchorPane.setTopAnchor(online, (double) 25);
        online.setRadius(5);
        if (isOnline == null) {
            online.setFill(Paint.valueOf("red"));
        } else {
            online.setFill(isOnline ? Paint.valueOf("green") : Paint.valueOf("gray"));
        }

        pane.getChildren().addAll(avatar, nicknameLabel, online);
        return pane;
    }

    public static AnchorPane self(Image image, String msg, ReadOnlyDoubleProperty widthProp) {
        return genPane(image, msg, widthProp, "self");
    }

    public static AnchorPane other(Image image, String msg, ReadOnlyDoubleProperty widthProp) {
        return genPane(image, msg, widthProp, "other");
    }

    private static AnchorPane genPane(Image image, String msg, ReadOnlyDoubleProperty widthProp, String type) {
        ImageView avatar = new ImageView();
        Label timestamp = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Label message = new Label();

        timestamp.getStyleClass().addAll("timestamp");
        message.getStyleClass().addAll("msg", type);

        avatar.setImage(image);
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);

        message.setWrapText(true);
        message.setPrefHeight(Label.USE_COMPUTED_SIZE);
        message.setPrefWidth(Label.USE_COMPUTED_SIZE);
        message.maxWidthProperty().bind(widthProp.divide(2.2));
        message.setText(msg);

        if ("self".equalsIgnoreCase(type)) {
            AnchorPane.setRightAnchor(avatar, 5.0);
            AnchorPane.setRightAnchor(timestamp, 40.0);
            AnchorPane.setRightAnchor(message, 40.0);
            AnchorPane.setTopAnchor(message, 20.0);
        } else {
            AnchorPane.setLeftAnchor(timestamp, (double) 40);
            AnchorPane.setLeftAnchor(message, (double) 40);
            AnchorPane.setTopAnchor(message, (double) 20);
        }

        AnchorPane chatPane = new AnchorPane();
        chatPane.getStylesheets().addAll(PaneUtils.class.getResource("/css/chat.css").toExternalForm());
        chatPane.getChildren().addAll(avatar, message, timestamp);

        return chatPane;
    }
}
