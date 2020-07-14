package com.jun.lineyou.net.handler;

import com.jun.lineyou.annotation.MqttHandler;
import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.entity.InnerMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.TimeUnit;

/**
 * {@link MqttMessageType#CONNACK} 消息处理器
 *
 * @author Jun
 * @date 2020-07-09 10:21
 */
@Slf4j
@MqttHandler(type = MqttMessageType.CONNACK)
public class ConnAckHandler implements MqttMessageHandler {

    private MqttMessage heartbeatMessage;

    /**
     * 心跳周期，默认 60s
     */
    @Value("${heartbeat.duration:60}")
    private int heartbeatDuration;

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage msg) {
        log.info("receive connack:{}", msg);

        InnerChannel.notify(InnerMsg.success(InnerMsg.InnerMsgEnum.sign_in));

        startHeartbeat(ctx);
    }

    private void startHeartbeat(ChannelHandlerContext ctx) {
        ctx.executor().scheduleAtFixedRate(() -> {
            if (heartbeatMessage == null) {
                heartbeatMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PINGREQ, false, MqttQoS.AT_LEAST_ONCE, false, 0),
                        null, null);
            }

            ctx.writeAndFlush(heartbeatMessage);
        }, 0, heartbeatDuration, TimeUnit.SECONDS);
    }
}
