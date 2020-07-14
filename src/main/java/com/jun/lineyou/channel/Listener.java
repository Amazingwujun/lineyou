package com.jun.lineyou.channel;

import com.jun.lineyou.entity.InnerMsg;

/**
 * 监听器
 *
 * @author Jun
 * @date 2020-07-09 11:23
 */
public interface Listener {

    void action(InnerMsg msg);
}
