package pl.edu.agh.to.kolektyw_glazurniczy.gui;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;

public class FXMLLoaderFactory {
    private final ApplicationContext applicationContext;

    public FXMLLoaderFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public FXMLLoader getFXMLLoader() {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(applicationContext::getBean);
        return loader;
    }
}
