package com.jun.lineyou.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Jun
 * @date 2018-11-30 23:05
 */
@Component
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static void setCtx(ApplicationContext ctx) {
        Assert.notNull(ctx, "Spring 上下文不能为空");
        applicationContext = ctx;
    }

    public static <T> T getBean(Class<T> clazz) {
        Assert.notNull(applicationContext, "Spring 容器初始化异常");
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        Assert.notNull(applicationContext, "Spring 容器初始化异常");
        return applicationContext.getBean(beanName, clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }
}
