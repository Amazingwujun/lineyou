package com.jun.lineyou.net.handler;

import com.jun.lineyou.annotation.MqttHandler;
import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.entity.InnerMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
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

        MqttFixedHeader fixedHeader = mpm.fixedHeader();
        String topicName = mpm.variableHeader().topicName();
        MqttQoS qos = fixedHeader.qosLevel();
        ByteBuf payload = mpm.payload();
        byte[] payloadBytes = new byte[payload.readableBytes()];
        payload.readBytes(payloadBytes);

        //消息交付
        InnerChannel.notify(InnerMsg.success(InnerMsg.InnerMsgEnum.pub, new InnerMsg.PubMsg(topicName, payloadBytes)));

        if (MqttQoS.AT_LEAST_ONCE == qos) {
            MqttMessage pubAck = MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBACK, false, fixedHeader.qosLevel(), fixedHeader.isRetain(), 0),
                    MqttMessageIdVariableHeader.from(mpm.variableHeader().packetId()),
                    null
            );
            ctx.writeAndFlush(pubAck);
        } else if (MqttQoS.AT_MOST_ONCE == qos) {

        } else if (MqttQoS.EXACTLY_ONCE == qos) {
            //目前没有 qos2 的需求
        }
    }
}
