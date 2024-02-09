package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationStateManager {

    // view cache - reduces unnecessary view creations and speeds up loading times
    private final Map<String, Parent> loadedViews = new HashMap<>();
    private final Map<String, FXMLLoader> loadedControllers = new HashMap<>();


    private final MainController mainController;
    private final ObjectProperty<User> user = new SimpleObjectProperty<>();
    private final FXMLLoaderFactory fxmlLoaderFactory;

    public ApplicationStateManager(@Lazy MainController mainController,
                                   FXMLLoaderFactory fxmlLoaderFactory) {
        this.mainController = mainController;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
    }

    public FXMLLoader loadView(String fxmlFileName) {
        Parent newView;

        try {
            if (loadedViews.containsKey(fxmlFileName)) {
                newView = loadedViews.get(fxmlFileName);
            } else {
                FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
                loader.setLocation(getClass().getClassLoader().getResource("view/" + fxmlFileName));
                newView = loader.load();
                // Cache the loaded view
                loadedViews.put(fxmlFileName, newView);
                loadedControllers.put(fxmlFileName, loader);
            }

            mainController.setCenterView(newView);

            return loadedControllers.get(fxmlFileName);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public User getUser() {
        return user.get();
    }

    public ObjectProperty<User> userProperty() {
        return user;
    }

    public void clearState() {
        loadedViews.clear();
        loadedControllers.clear();
    }
}
