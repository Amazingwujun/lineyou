package com.jun.lineyou.ui.app;

import com.jun.lineyou.FxmlHandler;
import com.jun.lineyou.entity.ControllerAndView;
import com.jun.lineyou.ui.controller.SignInController;
import com.jun.lineyou.utils.SpringUtils;
import com.jun.lineyou.utils.ViewContainer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * javafx应用启动器
 *
 * @author Jun
 * @date 2020-06-29 18:04
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        ViewContainer.SIGN_IN = primaryStage;
        FxmlHandler fxmlHandler = SpringUtils.getBean(FxmlHandler.class);

        //登入视图加载
        ControllerAndView<SignInController, Node> controllerAndView = fxmlHandler.loadControllerAndView(SignInController.class);
        primaryStage.setScene(new Scene((Parent) controllerAndView.getView()));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();
    }
}
