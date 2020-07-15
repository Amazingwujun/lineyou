package com.jun.lineyou.ui.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.constant.Topic;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.InnerMsg;
import com.jun.lineyou.entity.ProtoMsg;
import com.jun.lineyou.net.NetClient;
import com.jun.lineyou.utils.PaneUtils;
import com.jun.lineyou.utils.ViewContainer;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 主页面控制器
 *
 * @author Jun
 * @date 2020-06-30 19:52
 */
@Slf4j
@ViewController(fxml = ViewFxml.MAIN)
public class MainController extends AbstractDragController implements Listener {

    public SplitPane splitPane;

    /**
     * 默认头像
     */
    private Image defaultAvatar = new Image(getClass().getResourceAsStream("/img/avatar.jpg"));
    private Map<String, ObservableList<AnchorPane>> msgViewMap = new HashMap<>(20);

    public TextField searchWord;

    /**
     * 发送对象
     */
    private String to;

    public ListView<AnchorPane> msgView;
    public ListView<AnchorPane> friendsView;
    public TextArea sendMsg;
    public FontAwesomeIconView close;
    public AnchorPane topPane, midPane, leftPane;

    private NetClient netClient;

    public MainController(NetClient netClient) {
        this.netClient = netClient;
    }

    public void initialize() {
        initDrag(Arrays.asList(topPane, midPane, leftPane));

        close.setOnMouseClicked(event -> {
            netClient.close();
            System.exit(0);
        });

        splitPane.getDividers().forEach(divider -> divider.positionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0.9) {
                divider.setPosition(0.9);
            }
            if (newValue.doubleValue() < 0.1) {
                divider.setPosition(0.1);
            }
        }));

        friendsView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(to, newValue.getId())) {
                return;
            }
            to = newValue.getId();
            ObservableList<AnchorPane> anchorPanes = msgViewMap.get(to);
            if (anchorPanes == null) {
                anchorPanes = FXCollections.observableArrayList();
            }
            msgView.setItems(anchorPanes);
            msgView.refresh();
        });
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
        InnerMsg.PubMsg data = (InnerMsg.PubMsg) msg.getData();
        final String topic = data.topic();
        final byte[] bytes = data.msg();

        if (topic.equals("chat/msg/" + SignInController.user.getUsername())) {
            Platform.runLater(() -> {
                try {
                    ProtoMsg.ChatMessage chatMessage = ProtoMsg.ChatMessage.parseFrom(bytes);

                    String strMsg = chatMessage.getMsg();
                    AnchorPane other = PaneUtils.other(
                            new Image(getClass().getResourceAsStream("/img/avatar.jpg")),
                            strMsg,
                            msgView.widthProperty()
                    );
                    msgViewMap.get(chatMessage.getFrom()).add(other);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            });
        } else if (Topic.USER_STATE_CHANGE.equals(topic)) {
            //用户状态变更
            try {
                ProtoMsg.UserStateMessage userStateMessage = ProtoMsg.UserStateMessage.parseFrom(bytes);
                String mobile = userStateMessage.getMobile();
                String nickname = userStateMessage.getNickname();
                boolean online = userStateMessage.getOnline();

                //忽略自己
                if (SignInController.user.getUsername().equals(mobile)) {
                    return;
                }

                //更新朋友状态
                Platform.runLater(() -> {
                    AnchorPane anchorPane = null;
                    for (AnchorPane item : friendsView.getItems()) {
                        if (Objects.equals(item.getId(), mobile)) {
                            anchorPane = item;
                        }
                    }
                    if (anchorPane == null) {
                        if (online) {
                            whenFriendOnline(mobile, nickname);
                        }
                    } else {
                        for (Node child : anchorPane.getChildren()) {
                            if (child instanceof Circle) {
                                ((Circle) child).setFill(online ? Paint.valueOf("green") : Paint.valueOf("gray"));
                                break;
                            }
                        }
                    }
                });
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    private AnchorPane friendsView(String nickname, String mobile) {
        return PaneUtils.friend(new Image(getClass().getResourceAsStream("/img/avatar.jpg")), mobile, nickname);
    }

    /**
     * 发送消息给其它人
     */
    public void sendMsg() {
        if (StringUtils.isEmpty(to)) {
            //
            log.error("必须选择一个对象");
        } else {
            String msgText = sendMsg.getText();
            if (!StringUtils.hasText(msgText)) {
                return;
            }

            ProtoMsg.ChatMessage chatMessage = ProtoMsg.ChatMessage.newBuilder()
                    .setMsg(msgText)
                    .setFrom(SignInController.user.getUsername())
                    .setTo(to)
                    .setTimestamp(System.currentTimeMillis())
                    .build();
            MqttPublishMessage msg = MqttMessageBuilders.publish()
                    .messageId(netClient.genMsgId())
                    .qos(MqttQoS.AT_LEAST_ONCE)
                    .retained(false)
                    .payload(Unpooled.wrappedBuffer(chatMessage.toByteArray()))
                    .topicName("chat/msg/" + to)
                    .build();
            netClient.send(msg);

            Platform.runLater(() -> {
                sendMsg.clear();
                AnchorPane self = PaneUtils.self(new Image(getClass().getResourceAsStream("/img/avatar.jpg")), msgText, msgView.widthProperty());
                msgViewMap.get(to).add(self);
            });
        }
    }

    /**
     * 搜索其它人
     *
     * @param event
     */
    public void searchFriend(MouseEvent event) {
        String mobile = searchWord.getText();
        if (StringUtils.isEmpty(mobile)) {
            return;
        }

        //
        MqttPublishMessage search = MqttMessageBuilders.publish()
                .topicName("chat/searchOther/" + mobile)
                .retained(false)
                .payload(Unpooled.EMPTY_BUFFER)
                .messageId(netClient.genMsgId())
                .qos(MqttQoS.AT_LEAST_ONCE)
                .build();

        netClient.send(search);
    }

    private void whenFriendOnline(String mobile, String nickname) {
        AnchorPane friend = PaneUtils.friend(defaultAvatar, mobile, nickname);

        //初始化聊天 chatList
        msgViewMap.computeIfAbsent(mobile, k -> FXCollections.observableArrayList());
        friendsView.getItems().add(friend);
    }
}
