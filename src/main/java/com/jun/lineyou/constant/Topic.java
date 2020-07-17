package com.jun.lineyou.constant;

import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author Jun
 * @date 2020-07-14 16:45
 */
public enum Topic {
    user_state("chat/userStatus", MqttQoS.AT_LEAST_ONCE, false, false),
    friend_state_change("chat/friendStatus/", MqttQoS.AT_MOST_ONCE, true, true),
    chat_msg("chat/msg/", MqttQoS.AT_LEAST_ONCE, true, true);

    private String topic;
    private MqttQoS qos;
    private boolean isPrefix;
    private boolean needSub;

    Topic(String topic, MqttQoS qos, boolean isPrefix, boolean needSub) {
        this.topic = topic;
        this.qos = qos;
        this.isPrefix = isPrefix;
        this.needSub = needSub;
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

    public boolean needSub(){
        return needSub;
    }
}
