package com.jun.lineyou.utils;

import com.jun.lineyou.annotation.ViewController;
import com.jun.lineyou.constant.ViewFxml;
import com.jun.lineyou.entity.ControllerAndView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 视图加载器
 *
 * @author Jun
 * @date 2019-12-19 14:27
 */
@Component
public class FxmlHandler {

    private ApplicationContext context;

    public FxmlHandler(ApplicationContext context) {
        this.context = context;
    }


    /**
     * 加载视图
     *
     * @param clazz 控制器类
     * @param <C>   控制器
     * @param <V>   视图
     * @return {@link ControllerAndView}
     */
    public <C, V extends Parent> ControllerAndView<C, V> loadControllerAndView(Class<C> clazz) {
        ViewController viewController = clazz.getAnnotation(ViewController.class);
        ViewFxml fxml = viewController.fxml();

        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(context::getBean);

        V view = loadNode(loader, fxml);
        C controller = loader.getController();

        return new ControllerAndView<>(controller, view);
    }


    private <T> T loadNode(final FXMLLoader loader, final ViewFxml fxml) {
        String name = fxml.getName();

        try {
            loader.setLocation(getClass().getResource(name));

            return loader.load();
        } catch (final IOException e) {
            throw new RuntimeException(String.format("Unable to load FXML '%s'", name), e);
        }
    }
}
