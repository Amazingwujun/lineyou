package com.jun.lineyou.net.handler;

import com.jun.lineyou.annotation.MqttHandler;
import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.entity.InnerMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;

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
        MqttPublishMessage mpm = (MqttPublishMessage) msg;

        String topicName = mpm.variableHeader().topicName();
        ByteBuf payload = mpm.payload();
        byte[] payloadBytes = new byte[payload.readableBytes()];
        payload.readBytes(payloadBytes);

        InnerChannel.notify(InnerMsg.success(InnerMsg.InnerMsgEnum.pub, new InnerMsg.PubMsg(topicName, payloadBytes)));
    }
}
