package com.jun.lineyou;

import com.jun.lineyou.ui.app.App;
import com.jun.lineyou.utils.SpringUtils;
import javafx.application.Application;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class LineyouApplication {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring/spring-core.xml");
        SpringUtils.setCtx(ctx);
        Application.launch(App.class, args);
    }

}
