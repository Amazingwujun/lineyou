package com.jun.lineyou.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 内部消息传递机制，类似 go channel
 *
 * @author Jun
 * @date 2020-07-09 11:24
 */
@Slf4j
@Component
public class InnerChannel implements Runnable {

    private static final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(10000);

    private List<Listener> listeners;

    public InnerChannel(List<Listener> listeners) {
        Assert.notEmpty(listeners, "listener can't be null");
        this.listeners = listeners;

        new Thread(this, this.getClass().getName()).start();
    }

    public static void notify(Object msg) {
        queue.add(msg);
    }

    private void notifyListener(Object msg) {
        listeners.forEach(listener -> listener.action(msg));
    }

    @Override
    public void run() {
        log.info("innerChannel start work");

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Object msg = queue.take();

                notifyListener(msg);
            } catch (Exception e) {
                log.error("innerChannel 异常", e);
            }
        }
    }
}
