package com.jun.lineyou.ui.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.vo.LoginVO;
import com.jun.lineyou.utils.HttpUtils;
import com.jun.lineyou.utils.ViewContainer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 注册处理器
 *
 * @author Jun
 * @date 2020-07-17 11:33
 */
@Slf4j
@ViewController(fxml = ViewFxml.SIGN_UP)
public class SignUpController {

    public JFXTextField mobile, nickname;
    public JFXPasswordField password;
    public Label notice;

    @Value("${sign-up-url:http://localhost:18545/user/signUp}")
    private String signUpUrl;

    public void initialize() {
        currentStage().onCloseRequestProperty().addListener((observable, oldValue, newValue) -> {
            mobile.clear();
            nickname.clear();
            password.clear();
            notice.setText("");
        });

        mobile.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isEmpty(newValue)) {
                mobile.getStyleClass().removeAll("success", "warn");
                return;
            }

            if (!newValue.matches("1[0-9]{10}")) {
                mobile.getStyleClass().remove("success");
                mobile.getStyleClass().add("warn");
            } else {
                mobile.getStyleClass().remove("warn");
                mobile.getStyleClass().add("success");
            }
        });
    }


    private Stage currentStage() {
        return ViewContainer.SIGN_UP;
    }

    public void signUp(ActionEvent actionEvent) {
        if (!mobile.getText().matches("1[0-9]{10}")) {
            Platform.runLater(() -> {
                notice.setText("手机号码格式非法");
            });
            return;
        }
        if (!password.getText().matches("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$")) {
            Platform.runLater(() -> notice.setText("密码长度介于6-12位，且存在数字与字母"));
            return;
        }
        if (!StringUtils.hasText(nickname.getText())) {
            Platform.runLater(() -> notice.setText("昵称不能为空"));
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mobile", mobile.getText());
        jsonObject.put("password", password.getText());
        jsonObject.put("nickname", nickname.getText());
        HttpUtils.asyncPost(signUpUrl, jsonObject.toString(), new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                int statusCode = result.getStatusLine().getStatusCode();
                if (200 == statusCode) {
                    try {
                        String resultString = EntityUtils.toString(result.getEntity());
                        JSONObject json = JSON.parseObject(resultString);
                        if (Objects.equals(200, json.getInteger("code"))) {
                            Platform.runLater(() -> {
                                currentStage().close();
                                ViewContainer.SIGN_IN.show();
                            });
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

            }

            @Override
            public void cancelled() {

            }
        });
    }
}
