package com.jun.lineyou.annotation;

import com.jun.lineyou.constant.ViewFxml;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JAVAFX 视图控制器
 *
 * @author Jun
 * @date 2020-07-07 18:14
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ViewController {

    ViewFxml fxml();
}
