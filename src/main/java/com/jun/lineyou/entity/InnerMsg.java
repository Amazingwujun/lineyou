package com.jun.lineyou.entity;

import lombok.Data;

/**
 * 内部消息
 *
 * @author Jun
 * @date 2020-07-09 18:41
 */
@Data
public class InnerMsg {

    private static final int SUCCESS = 200;

    /**
     * 数据
     */
    private Object data;

    /**
     * 消息类别：
     * <ol>
     *     <li>1. 登入</li>
     *     <li>1. mqtt</li>
     * </ol>
     */
    private InnerMsgEnum type;

    /**
     * 执行结果，用来判断某些操作类别消息的返回结果，例如登入
     * <ol>
     *     <li>200 -> 成功</li>
     * </ol>
     */
    private int code;

    private InnerMsg(Object data, InnerMsgEnum type, int code) {
        this.data = data;
        this.type = type;
        this.code = code;
    }

    public static InnerMsg success(InnerMsgEnum type) {
        return new InnerMsg(null, type, SUCCESS);
    }

    public static InnerMsg success(InnerMsgEnum type, Object data) {
        return new InnerMsg(data, type, SUCCESS);
    }

    /**
     * 执行结果
     *
     * @return true if msg success
     */
    public boolean isSuccess() {
        return code == SUCCESS;
    }

    public enum InnerMsgEnum {
        sign_in, reconnect, connect_init, pub, fetch_friend_req, fetch_friend_resp,search_friend_init,make_friend_success
    }

    //发布消息
    public static final class PubMsg {

        private String topic;

        private byte[] msg;

        public PubMsg(String topic, byte[] msg) {
            this.topic = topic;
            this.msg = msg;
        }

        public byte[] msg() {
            return msg;
        }

        public String topic() {
            return topic;
        }
    }
}
