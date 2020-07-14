package com.jun.lineyou.net.handler;

import com.jun.lineyou.annotation.MqttHandler;
import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.entity.InnerMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * {@link MqttMessageType#PUBLISH} 消息处理器
 *
 * @author Jun
 * @date 2020-07-09 14:20
 */
@Slf4j
@MqttHandler(type = MqttMessageType.PUBLISH)
public class PublishHandler implements MqttMessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, MqttMessage msg) {
        ByteBuf payload = (ByteBuf) msg.payload();
        byte[] data = new byte[payload.readableBytes()];
        payload.readBytes(data);
        log.info("publish:{}", new String(data, StandardCharsets.UTF_8));

        InnerChannel.notify(InnerMsg.success(InnerMsg.InnerMsgEnum.pub, new String(data,StandardCharsets.UTF_8)));
    }
}
