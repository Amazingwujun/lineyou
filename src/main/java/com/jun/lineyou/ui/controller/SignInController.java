package com.jun.lineyou.ui.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.constant.Topic;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.ControllerAndView;
import com.jun.lineyou.entity.InnerMsg;
import com.jun.lineyou.entity.ProtoMsg;
import com.jun.lineyou.entity.User;
import com.jun.lineyou.entity.vo.FriendVO;
import com.jun.lineyou.entity.vo.LoginVO;
import com.jun.lineyou.net.NetClient;
import com.jun.lineyou.utils.FxmlHandler;
import com.jun.lineyou.utils.HttpUtils;
import com.jun.lineyou.utils.ViewContainer;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.*;

/**
 * 登入控制器
 *
 * @author Jun
 * @date 2020-06-30 18:36
 */
@Slf4j
@ViewController(fxml = ViewFxml.SIGN_IN)
public class SignInController extends AbstractDragController implements Listener {

    public Label notice;
    private Set<String> userSubTopic = new HashSet<>();
    private Set<FriendVO> friendVOSet = new HashSet<>();

    public JFXButton signIn;
    public JFXTextField username;
    public JFXPasswordField password;
    public Text close;
    public ImageView logo;
    public AnchorPane leftPane, rightPane;
    /**
     * 实例化登入用户
     */
    public static final User user = new User();
    private FxmlHandler fxmlHandler;

    private NetClient netClient;

    @Value("${sign-in-url:http://localhost:18545/user/signIn}")
    private String remoteSignInUrl;

    public SignInController(FxmlHandler fxmlHandler, NetClient netClient) {
        this.fxmlHandler = fxmlHandler;
        this.netClient = netClient;
    }

    public void initialize() {
        initBind();
        initStageDrag();
        initResourceLoad();

        //监听关闭事件
        close.setOnMouseClicked(event -> {
            netClient.close();
            System.exit(0);
        });
    }

    public void signIn(ActionEvent actionEvent) {
        actionEvent.consume();

        JFXButton target = (JFXButton) actionEvent.getTarget();
        target.setDisable(true);

        if (!user.getUsername().matches("1[1-9][0-9]{9}")) {
            target.setDisable(false);
            return;
        }

        HttpUtils.asyncPost(remoteSignInUrl, user.toJsonString(), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                int statusCode = result.getStatusLine().getStatusCode();
                if (200 == statusCode) {
                    try {
                        String resultString = EntityUtils.toString(result.getEntity());
                        JSONObject json = JSON.parseObject(resultString);
                        if (Objects.equals(200, json.getInteger("code"))) {
                            LoginVO data = json.getJSONObject("data").toJavaObject(LoginVO.class);
                            onSignInSucceeded(data);
                        } else {
                            String msg = json.getString("msg");
                            Platform.runLater(() -> notice.setText(msg));
                        }
                    } catch (IOException e) {
                        log.info(e.getMessage(), e);
                    }
                } else {

                }
            }

            @Override
            public void failed(Exception ex) {
                Platform.runLater(() -> notice.setText("网络异常:" + ex.getMessage()));
            }

            @Override
            public void cancelled() {

            }
        });

        target.disableProperty().set(false);
        target.setText("登入");
    }

    /**
     * 登入成功，需要执行的逻辑如下：
     * <ul>
     *     <li></li>
     *     <li></li>
     * </ul>
     *
     * @param data
     */
    private void onSignInSucceeded(LoginVO data) {
        user.setToken(data.getToken());
        userSubTopic = data.getSubTopics();
        friendVOSet = data.getFriends();
        netClient.start();
    }

    private void initResourceLoad() {
        logo.setImage(new Image("/img/logo.png"));
    }

    /**
     * 初始化属性绑定
     */
    private void initBind() {
        user.usernameProperty().bind(username.textProperty());
        user.passwordProperty().bind(password.textProperty());
    }

    /**
     * 初始化窗口拖拽处理
     */
    private void initStageDrag() {
        initDrag(Arrays.asList(leftPane, rightPane));
    }

    @Override
    Stage currentStage() {
        return ViewContainer.SIGN_IN;
    }

    @Override
    public void action(InnerMsg msg) {
        log.info("rec:{}", msg);

        if (InnerMsg.InnerMsgEnum.sign_in == msg.getType() && msg.isSuccess()) {
            onSignInSucceeded();
        } else if (InnerMsg.InnerMsgEnum.sign_in == msg.getType() && !msg.isSuccess()) {
            //todo 暂时只处理登入成功
        }
        if (InnerMsg.InnerMsgEnum.connect_init == msg.getType()) {
            ProtoMsg.UserStateMessage userStateMessage = ProtoMsg.UserStateMessage.newBuilder()
                    .setOnline(false)
                    .setMobile(user.getUsername())
                    .build();

            //登入
            MqttConnectMessage build = MqttMessageBuilders.connect()
                    .cleanSession(false)
                    .clientId(user.getUsername())
                    .keepAlive(60)
                    .password(user.getPassword().getBytes())
                    .username(user.getUsername())
                    .willFlag(true)
                    .willQoS(MqttQoS.AT_LEAST_ONCE)
                    .willRetain(true)
                    .willMessage(userStateMessage.toByteArray())
                    .willTopic(Topic.user_state.topic())
                    .build();

            netClient.send(build);
        }

        //返回用户朋友
        if (InnerMsg.InnerMsgEnum.fetch_friend_req == msg.getType()) {
            InnerChannel.notify(InnerMsg.success(InnerMsg.InnerMsgEnum.fetch_friend_resp, friendVOSet));
        }
    }

    /**
     * 登入成功调用
     */
    private void onSignInSucceeded() {
        //订阅 topic
        List<MqttTopicSubscription> needSub = new ArrayList<>();
        for (Topic topic : Topic.values()) {
            String tc = topic.topic();
            if (!userSubTopic.contains(tc)) {
                needSub.add(new MqttTopicSubscription(topic.isPrefix() ? (tc + SignInController.user.getUsername()) : tc, topic.qos()));
            }
        }
        if (!needSub.isEmpty()) {
            MqttFixedHeader mqttFixedHeader =
                    new MqttFixedHeader(MqttMessageType.SUBSCRIBE, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessageIdVariableHeader mqttVariableHeader = MqttMessageIdVariableHeader.from(netClient.genMsgId());
            MqttSubscribePayload mqttSubscribePayload = new MqttSubscribePayload(needSub);
            netClient.send(new MqttSubscribeMessage(mqttFixedHeader, mqttVariableHeader, mqttSubscribePayload));
        }

        //成功上线
        ProtoMsg.UserStateMessage userStateMessage = ProtoMsg.UserStateMessage.newBuilder()
                .setOnline(true)
                .setMobile(user.getUsername())
                .build();
        MqttPublishMessage onlineMsg = MqttMessageBuilders
                .publish()
                .messageId(netClient.genMsgId())
                .qos(MqttQoS.AT_LEAST_ONCE)
                .retained(true)
                .topicName(Topic.user_state.topic())
                .payload(Unpooled.wrappedBuffer(userStateMessage.toByteArray()))
                .build();
        netClient.send(onlineMsg);

        //加载主界面
        if (ViewContainer.MAIN == null) {
            Platform.runLater(() -> {
                //加载主页面
                ControllerAndView<MainController, Parent> controllerAndView = fxmlHandler.loadControllerAndView(MainController.class);
                ViewContainer.MAIN = new Stage();
                Scene scene = new Scene(controllerAndView.getView());
                scene.setFill(Color.TRANSPARENT);
                ViewContainer.MAIN.setScene(scene);
                ViewContainer.MAIN.initStyle(StageStyle.TRANSPARENT);
                ViewContainer.MAIN.show();

                currentStage().close();
            });
        } else {
            if (!ViewContainer.MAIN.isShowing()) {
                Platform.runLater(() -> ViewContainer.MAIN.show());
            }
        }
    }

    /**
     * 注册
     *
     * @param actionEvent
     */
    public void signUp(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            if (ViewContainer.SIGN_UP != null) {
                ViewContainer.SIGN_UP.show();
            } else {
                Stage stage = new Stage();
                ViewContainer.SIGN_UP = stage;
                stage.initModality(Modality.WINDOW_MODAL);
                stage.setResizable(false);
                stage.initOwner(currentStage());

                ControllerAndView<SignUpController, Parent> cav = fxmlHandler.loadControllerAndView(SignUpController.class);
                stage.setScene(new Scene(cav.getView()));
                stage.show();
            }
        });
    }
}


