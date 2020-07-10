package com.jun.lineyou.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 内部消息
 *
 * @author Jun
 * @date 2020-07-09 18:41
 */
@Data
@AllArgsConstructor
public class InnerMsg {

    private String msg;

    /**
     *
     */
    private int type;
}
