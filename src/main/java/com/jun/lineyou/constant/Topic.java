package com.jun.lineyou.constant;

import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author Jun
 * @date 2020-07-14 16:45
 */
public enum Topic {
    user_state("chat/userStatus", MqttQoS.AT_LEAST_ONCE, false),
    friend_state_change("chat/friendStatus/", MqttQoS.AT_MOST_ONCE, true),
    chat_msg("chat/msg/", MqttQoS.AT_LEAST_ONCE, true);

    private String topic;
    private MqttQoS qos;
    private boolean isPrefix;

    Topic(String topic, MqttQoS qos, boolean isPrefix) {
        this.topic = topic;
        this.qos = qos;
        this.isPrefix = isPrefix;
    }

    public String topic() {
        return topic;
    }

    public boolean isPrefix() {
        return isPrefix;
    }

    public MqttQoS qos() {
        return qos;
    }
}
