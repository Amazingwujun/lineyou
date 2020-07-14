package com.jun.lineyou.ui.controller;

import com.jfoenix.controls.JFXListView;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.InnerMsg;
import com.jun.lineyou.net.NetClient;
import com.jun.lineyou.utils.ViewContainer;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
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

    /**
     * 发送对象
     */
    private String to;

    public JFXListView<AnchorPane> msgView;
    public ListView<AnchorPane> friendsView;
    public TextArea sendMsg;
    public FontAwesomeIconView close;
    public AnchorPane topPane, midPane, leftPane;

    private NetClient netClient;

    public MainController(NetClient netClient) {
        super();
        this.netClient = netClient;
    }

    public void initialize() {
        initDrag(Arrays.asList(topPane, midPane, leftPane));

        close.setOnMouseClicked(event -> {
            netClient.close();
            System.exit(0);
        });

        friendsView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            to = newValue.getId();
        });

        //为了便于测试，生成两个默认用户
        friendsView.getItems().addAll(friendsView("小白", "17620078988"), friendsView("小黑", "15679197197"));
    }


    @Override
    Stage currentStage() {
        return ViewContainer.MAIN;
    }


    @Override
    public void action(InnerMsg msg) {
        if (InnerMsg.InnerMsgEnum.pub != msg.getType()) {
            return;
        }
        Platform.runLater(() -> {
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
            msgLabel.setText((String) msg.getData());

            AnchorPane chatPane = new AnchorPane();
            chatPane.getChildren().addAll(avatar, msgLabel, timeLabel);
            avatar.setImage(new Image(getClass().getResourceAsStream("/img/avatar.jpg")));

            msgView.getItems().add(chatPane);
        });
    }


    private AnchorPane friendsView(String nickname, String mobile) {
        AnchorPane pane = new AnchorPane();
        pane.setId(mobile);
        ImageView avatar = new ImageView(); //头像
        Label nicknameLabel = new Label();
        Circle online = new Circle();

        //头像
        avatar.setFitHeight(30);
        avatar.setFitWidth(30);
        avatar.setImage(new Image(getClass().getResourceAsStream("/img/avatar.jpg")));

        //昵称
        AnchorPane.setLeftAnchor(nicknameLabel, (double) 40);
        nicknameLabel.setText(nickname);
        nicknameLabel.setStyle("-fx-padding: 3;-fx-background-radius: 5px;-fx-font-size: 12px");

        //在线状态
        AnchorPane.setLeftAnchor(online, (double) 40);
        AnchorPane.setTopAnchor(online, (double) 20);
        online.setRadius(5);
        online.setFill(Paint.valueOf("green"));

        pane.getChildren().addAll(avatar, nicknameLabel, online);

        return pane;
    }


    /**
     * 发送消息给其它人
     *
     * @param actionEvent
     */
    public void sendMsg(ActionEvent actionEvent) {
        if (StringUtils.isEmpty(to)) {
            //
            log.error("必须选择一个对象");
        } else {
            MqttPublishMessage msg = MqttMessageBuilders.publish()
                    .messageId(netClient.genMsgId())
                    .qos(MqttQoS.AT_MOST_ONCE)
                    .retained(false)
                    .payload(Unpooled.wrappedBuffer(sendMsg.getText().getBytes(StandardCharsets.UTF_8)))
                    .topicName("chat/msg/" + to)
                    .build();

            netClient.send(msg);
        }
    }
}
