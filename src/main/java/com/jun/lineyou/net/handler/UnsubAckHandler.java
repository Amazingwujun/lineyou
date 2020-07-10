package com.jun.lineyou.net.handler;

import com.jun.lineyou.annotation.MqttHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link MqttMessageType#UNSUBACK} 消息处理器
 *
 * @author Jun
 * @date 2020-07-09 14:25
 */
@Slf4j
@MqttHandler(type = MqttMessageType.UNSUBACK)
public class UnsubAckHandler implements MqttMessageHandler {


    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage msg) {
        log.info("unsuback:{}", msg);
    }
}
