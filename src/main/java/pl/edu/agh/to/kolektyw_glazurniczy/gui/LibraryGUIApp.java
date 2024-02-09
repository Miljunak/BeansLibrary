package pl.edu.agh.to.kolektyw_glazurniczy.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import pl.edu.agh.to.kolektyw_glazurniczy.Main;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.controller.LoginController;

import java.io.IOException;

public class LibraryGUIApp extends Application {
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            var loader = new FXMLLoader();
            loader.setLocation(LibraryGUIApp.class.getClassLoader().getResource("view/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Pane loginLayout = loader.load();
            primaryStage.getIcons().add(new Image("/imgs/icon.png"));
            configureStage(primaryStage, loginLayout);
            LoginController loginController = loader.getController();
            // testing
            loginController.fillLoginFormData("admin@gmail.com", "12345678");

            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configureStage(Stage primaryStage, Parent loginLayout) {
        var scene = new Scene(loginLayout);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }

    static class StageReadyEvent extends ApplicationEvent {
        public StageReadyEvent(Stage stage) {
            super(stage);
        }
    }
}