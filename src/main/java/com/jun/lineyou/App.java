package com.jun.lineyou;

import com.jun.lineyou.entity.ControllerAndView;
import com.jun.lineyou.ui.controller.SignInController;
import com.jun.lineyou.utils.FxmlHandler;
import com.jun.lineyou.utils.HttpUtils;
import com.jun.lineyou.utils.SpringUtils;
import com.jun.lineyou.utils.ViewContainer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * javafx应用启动器
 *
 * @author Jun
 * @date 2020-06-29 18:04
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        initSpringCtx();

        ViewContainer.SIGN_IN = primaryStage;
        FxmlHandler fxmlHandler = SpringUtils.getBean(FxmlHandler.class);

        //登入视图加载
        ControllerAndView<SignInController, Parent> controllerAndView = fxmlHandler.loadControllerAndView(SignInController.class);
        primaryStage.setScene(new Scene(controllerAndView.getView()));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();
    }

    //初始化 spring 容器
    private void initSpringCtx() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring/spring-core.xml");
        SpringUtils.setCtx(ctx);
        try {
            //完成 static 代码块载入
            Class.forName(HttpUtils.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }
}
