package com.jun.lineyou.ui.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jun.lineyou.FxmlHandler;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.ControllerAndView;
import com.jun.lineyou.entity.User;
import com.jun.lineyou.net.NetClient;
import com.jun.lineyou.utils.ExecutorUtils;
import com.jun.lineyou.utils.ViewContainer;
import io.netty.handler.codec.mqtt.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * 登入控制器
 *
 * @author Jun
 * @date 2020-06-30 18:36
 */
@Slf4j
@ViewController(fxml = ViewFxml.SIGN_IN)
public class SignInController extends AbstractDragController implements Listener {

    /**
     * 实例化登入用户
     */
    private User user = new User();

    public JFXButton signIn;

    @FXML
    public JFXTextField username;

    @FXML
    public JFXPasswordField password;

    public Label close;

    public ImageView logo;

    public AnchorPane leftPane, rightPane;

    @Autowired
    private FxmlHandler fxmlHandler;

    @Autowired
    private NetClient netClient;

    private String signInServerUrl = "http://localhost:8080/api/signIn";

    public void initialize() {
        log.info("sign-in controller init");

        initBind();
        initStageDrag();
        initResourceLoad();

        //监听关闭事件
        close.setOnMouseClicked(event -> System.exit(0));
    }

    public void signIn(ActionEvent actionEvent) {
        actionEvent.consume();

        JFXButton target = (JFXButton) actionEvent.getTarget();
        target.setText("正在登录...");
        target.setDisable(true);

        if (!user.getUsername().matches("1[1-9][0-9]{9}")){
            target.setText("手机号码格式错误");
            target.setDisable(false);
            return;
        }

        ExecutorUtils.run(() -> netClient.start());

        target.disableProperty().set(false);
        target.setText("登入");
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
        initDrag(Arrays.asList(leftPane,rightPane));
    }

    @Override
    Stage currentStage() {
        return ViewContainer.SIGN_IN;
    }

    @Override
    public void action(Object msg) {
        log.info("rec:{}", msg);

        if ("sign in success".equals(msg)) {
            onSignInSucceeded();
        }
        if ("connect success".equals(msg)) {
            //登入
            MqttConnectMessage build = MqttMessageBuilders.connect()
                    .cleanSession(false)
                    .clientId(user.getUsername())
                    .keepAlive(60)
                    .password(user.getPassword().getBytes())
                    .username(user.getUsername())
                    .build();

            netClient.send(build);
        }
    }

    /**
     * 登入成功调用
     */
    private void onSignInSucceeded() {
        //订阅 topic
        MqttSubscribeMessage submsg = MqttMessageBuilders
                .subscribe()
                .messageId(1)
                .addSubscription(MqttQoS.AT_LEAST_ONCE, "resp/signIn/" + user.getUsername())
                .addSubscription(MqttQoS.AT_LEAST_ONCE,"chat/msg/" + user.getUsername())
                .build();
        netClient.send(submsg);

        //加载主界面
        if (ViewContainer.MAIN == null) {
            Platform.runLater(() -> {
                //加载主页面
                ControllerAndView<MainController, Node> controllerAndView = fxmlHandler.loadControllerAndView(MainController.class);
                ViewContainer.MAIN = new Stage();
                ViewContainer.MAIN.setScene(new Scene((Parent) controllerAndView.getView()));
                ViewContainer.MAIN.show();

                currentStage().close();
            });
        }
    }
}


