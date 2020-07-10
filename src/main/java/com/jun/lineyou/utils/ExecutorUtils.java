package com.jun.lineyou.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务执行器
 *
 * @author Jun
 * @date 2020-07-01 14:51
 */
public class ExecutorUtils {

    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    public static void run(Runnable runnable){
        executorService.execute(runnable);
    }
}
