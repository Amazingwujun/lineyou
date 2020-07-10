package com.jun.lineyou.net.handler;

import com.jun.lineyou.annotation.MqttHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link MqttMessageType#PUBACK} 消息处理器
 *
 * @author Jun
 * @date 2020-07-09 13:55
 */
@Slf4j
@MqttHandler(type = MqttMessageType.PUBACK)
public class PubAckHandler implements MqttMessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage msg) {
        log.info("pub ack:{}", msg);
    }
}
