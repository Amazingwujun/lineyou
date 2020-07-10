package com.jun.lineyou.net.handler;

import com.jun.lineyou.annotation.MqttHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.extern.slf4j.Slf4j;

/**
 * 订阅响应处理器
 *
 * @author Jun
 * @date 2020-07-09 13:47
 */
@Slf4j
@MqttHandler(type = MqttMessageType.SUBACK)
public class SubAckHandler implements MqttMessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage msg) {
        log.info("suback:{}", msg);
    }
}
