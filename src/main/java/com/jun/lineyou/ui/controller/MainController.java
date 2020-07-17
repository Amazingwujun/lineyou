package com.jun.lineyou.ui.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.constant.Topic;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.ControllerAndView;
import com.jun.lineyou.entity.InnerMsg;
import com.jun.lineyou.entity.ProtoMsg;
import com.jun.lineyou.entity.vo.FriendVO;
import com.jun.lineyou.net.NetClient;
import com.jun.lineyou.utils.FxmlHandler;
import com.jun.lineyou.utils.HttpUtils;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

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
    public Label chatTitle;

    /**
     * 默认头像
     */
    private Image defaultAvatar = new Image(getClass().getResourceAsStream("/img/avatar.jpg"));
    private Map<String, ObservableList<AnchorPane>> msgViewMap = new HashMap<>(20);

    public TextField searchWord;
    private String searchKeyword;

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
    private FxmlHandler fxmlHandler;

    @Value("${search-friend-url:http://localhost:18545/friend/search/}")
    private String searchFriendUrl;

    public MainController(NetClient netClient, FxmlHandler fxmlHandler) {
        this.netClient = netClient;
        this.fxmlHandler = fxmlHandler;
    }

    public void initialize() {
        initDrag(Arrays.asList(topPane, midPane, leftPane));

        //从 SignInController 获取朋友列表
        InnerChannel.notify(InnerMsg.success(InnerMsg.InnerMsgEnum.fetch_friend_req));

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
            chatTitle.setText(to);
            ObservableList<AnchorPane> anchorPanes = msgViewMap.computeIfAbsent(to, k -> FXCollections.observableArrayList());
            msgView.setItems(anchorPanes);
            msgView.refresh();
        });

        searchWord.textProperty().addListener((observable, oldValue, newValue) -> searchKeyword = newValue);
    }


    @Override
    Stage currentStage() {
        return ViewContainer.MAIN;
    }

    @Override
    public void action(InnerMsg msg) {
        if (InnerMsg.InnerMsgEnum.fetch_friend_resp == msg.getType()) {
            Set<FriendVO> data = (Set<FriendVO>) msg.getData();
            data.forEach(friendVO -> {
                Platform.runLater(() -> {
                    AnchorPane pane = friendsView(friendVO.getNickname(), friendVO.getMobile(), friendVO.getOnline());
                    friendsView.getItems().add(pane);
                });
            });
        } else if (InnerMsg.InnerMsgEnum.make_friend_success == msg.getType()) {
            FriendVO data = (FriendVO) msg.getData();
            Platform.runLater(() -> {
                AnchorPane pane = friendsView(data.getNickname(), data.getMobile(), data.getOnline());
                searchWord.clear();
                friendsView.getItems().add(pane);
            });
        }

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
                    msgViewMap.computeIfAbsent(chatMessage.getFrom(), k -> FXCollections.observableArrayList()).add(other);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            });
        } else if (topic.equals(Topic.friend_state_change.topic() + SignInController.user.getUsername())) {
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

    private AnchorPane friendsView(String nickname, String mobile, Boolean isOnline) {
        return PaneUtils.friend(new Image(getClass().getResourceAsStream("/img/avatar.jpg")), mobile, nickname, isOnline);
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
        mobile = searchKeyword;
        if (StringUtils.isEmpty(mobile) || !mobile.matches("1[0-9]{10}")) {
            return;
        }

        HttpUtils.asyncGet(searchFriendUrl + mobile, null, null, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse result) {
                int statusCode = result.getStatusLine().getStatusCode();
                if (200 == statusCode) {
                    try {
                        String resultString = EntityUtils.toString(result.getEntity());
                        JSONObject json = JSON.parseObject(resultString);
                        if (Objects.equals(200, json.getInteger("code"))) {
                            FriendVO data = json.getJSONObject("data").toJavaObject(FriendVO.class);

                            //加载界面并完成传输
                            Platform.runLater(() -> {
                                ControllerAndView<FriendController, Parent> cav = fxmlHandler.loadControllerAndView(FriendController.class);
                                Stage stage = new Stage();
                                ViewContainer.SEARCH_FRIEND = stage;
                                stage.setResizable(false);
                                stage.setScene(new Scene(cav.getView()));
                                stage.initOwner(currentStage());
                                stage.initModality(Modality.WINDOW_MODAL);

                                InnerChannel.notify(InnerMsg.success(InnerMsg.InnerMsgEnum.search_friend_init, data));
                                stage.show();
                            });
                        } else {

                        }
                    } catch (IOException e) {
                        log.info(e.getMessage(), e);
                    }
                } else {

                }
            }

            @Override
            public void failed(Exception ex) {

            }

            @Override
            public void cancelled() {

            }
        });
    }

    private void whenFriendOnline(String mobile, String nickname) {
        AnchorPane friend = PaneUtils.friend(defaultAvatar, mobile, nickname, true);

        //初始化聊天 chatList
        msgViewMap.computeIfAbsent(mobile, k -> FXCollections.observableArrayList());
        friendsView.getItems().add(friend);
    }
}
