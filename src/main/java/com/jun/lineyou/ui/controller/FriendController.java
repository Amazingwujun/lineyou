package com.jun.lineyou.ui.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.InnerMsg;
import com.jun.lineyou.entity.vo.FriendVO;
import com.jun.lineyou.utils.HttpUtils;
import com.jun.lineyou.utils.ViewContainer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jun
 * @date 2020-07-17 09:51
 */
@Slf4j
@ViewController(fxml = ViewFxml.SEARCH_FRIEND)
public class FriendController implements Listener {

    public Label nickname;
    public Label mobile;
    private Boolean online;

    @Value("${make-friend-url:http://localhost:18545/friend/makeFriend/}")
    private String makeFriendUrl;

    public void initialize() {
    }

    public void makeFriend(ActionEvent actionEvent) {
        Map<String, String> headers = new HashMap<>();
        headers.put("token", SignInController.user.getToken());
        headers.put("principal", SignInController.user.getUsername());
        HttpUtils.asyncPost(makeFriendUrl + mobile.getText(), headers, null, new FutureCallback<HttpResponse>() {

            @Override
            public void completed(HttpResponse result) {
                int statusCode = result.getStatusLine().getStatusCode();
                if (200 == statusCode) {
                    try {
                        String resultString = EntityUtils.toString(result.getEntity());
                        JSONObject json = JSON.parseObject(resultString);
                        if (Objects.equals(200, json.getInteger("code"))) {
                            Platform.runLater(() -> {
                                InnerChannel.notify(InnerMsg.success(
                                        InnerMsg.InnerMsgEnum.make_friend_success,
                                        new FriendVO(mobile.getText(),
                                                nickname.getText(),
                                                online,
                                                null)
                                        )
                                );
                                currentStage().close();
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


    private Stage currentStage() {
        return ViewContainer.SEARCH_FRIEND;
    }

    @Override
    public void action(InnerMsg msg) {
        //优先监听自身初始化事件
        if (msg.getType() == InnerMsg.InnerMsgEnum.search_friend_init) {
            FriendVO data = (FriendVO) msg.getData();
            nickname.setText(data.getNickname());
            mobile.setText(data.getMobile());
            online = data.getOnline();
        }
    }
}
