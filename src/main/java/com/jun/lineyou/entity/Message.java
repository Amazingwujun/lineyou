package com.jun.lineyou.entity;

/**
 * im 消息
 *
 * @author Jun
 * @date 2020-07-01 15:10
 */
public class Message {

    private Integer messageId;

    /**
     * 纳秒
     */
    private Long sendTime = System.nanoTime();

    /**
     * 消息内容
     */
    private String content;
}
