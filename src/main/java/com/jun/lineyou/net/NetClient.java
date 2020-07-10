package com.jun.lineyou.net;

import com.jun.lineyou.channel.InnerChannel;
import com.jun.lineyou.channel.Listener;
import com.jun.lineyou.entity.RemoteServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 网络客户端
 *
 * @author Jun
 * @date 2020-07-09 10:00
 */
@Slf4j
@Component
public class NetClient implements Listener {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private EventLoopGroup group;

    private Channel channel;

    private RemoteServer remoteServer;

    private int msgId;

    /**
     * 用户手动关闭连接标识
     */
    private boolean isManualClose = false;

    @Value("${biz.remote-host:0.0.0.0}")
    private String remoteHost;

    @Value("${biz.remote-port:1883}")
    private int remotePort;

    @Value("${biz.reconnect-duration:5}")
    private int reconnectDuration;

    /**
     * 心跳周期，默认 60s
     */
    @Value("${biz.heartbeat-duration:60}")
    private int heartbeatDuration;

    private BizHandler bizHandler;

    public NetClient(BizHandler bizHandler) {
        this.bizHandler = bizHandler;
    }

    public void start() {
        if (channel == null || !channel.isActive()) {
            executorService.execute(this::connect);
        }
    }

    /**
     * 建立与远程服务器的连接
     */
    private void connect() {
        if (group == null) {
            group = new NioEventLoopGroup();
        }

        try {
            Bootstrap b = new Bootstrap();
            b
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 0, (long) (heartbeatDuration * 1.5), TimeUnit.SECONDS));
                            pipeline.addLast(new MqttDecoder(10 * 1024));
                            pipeline.addLast(MqttEncoder.INSTANCE);
                            pipeline.addLast(bizHandler);
                        }
                    });

            if (remoteServer != null) {
                channel = b.connect(remoteServer.getRemoteHost(), remoteServer.getRemotePort()).sync().channel();
            } else {
                channel = b.connect(remoteHost, remotePort).sync().channel();
            }

            //notify connect success
            InnerChannel.notify("connect success");

            channel.closeFuture().sync();
        } catch (Exception e) {
            if (group != null && !isManualClose) {
                try {
                    TimeUnit.SECONDS.sleep(reconnectDuration);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                start();
            }
        }
    }

    /**
     * 发送 mqtt 消息
     *
     * @param mqttMessage mqtt消息
     * @return true if send
     */
    public boolean send(MqttMessage mqttMessage) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(mqttMessage);
            return true;
        }

        return false;
    }

    public void setServer(RemoteServer remoteServer) {
        this.remoteServer = remoteServer;
    }

    /**
     * 关闭服务
     */
    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }

        if (group != null) {
            group.shutdownGracefully();
            group = null;
        }
        isManualClose = true;
    }

    public synchronized int genMsgId() {
        if (msgId > 65535 || msgId < 1) {
            msgId = 1;
        }
        return msgId++;
    }

    @Override
    public void action(Object msg) {
        if ("reconnect".equals(msg)) {
            start();
        }
    }
}
