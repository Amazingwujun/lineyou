package com.jun.lineyou.net;

import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.net.handler.MessageDelegatingHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 业务处理器
 *
 * @author Jun
 * @date 2020-07-09 10:10
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class BizHandler extends SimpleChannelInboundHandler<MqttMessage> {

    private MessageDelegatingHandler delegatingHandler;

    public BizHandler(MessageDelegatingHandler delegatingHandler) {
        this.delegatingHandler = delegatingHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) throws Exception {
        //异常处理
        if (msg.decoderResult().isFailure()) {
            exceptionCaught(ctx, msg.decoderResult().cause());
            return;
        }

        delegatingHandler.handle(ctx, msg);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx)   {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelId id = ctx.channel().id();
        log.info("channel:{} inactive", id.asShortText());
        InnerChannel.notify("reconnect");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

    }
}
