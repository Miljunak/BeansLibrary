package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.model.UserRole;
import pl.edu.agh.to.kolektyw_glazurniczy.service.AuthService;

import java.util.List;

@Controller
public class MainController {

    private final AuthService authService;
    private final ApplicationStateManager applicationStateManager;
    private final NotificationsController notificationsController;

    @FXML
    private ToggleButton logoutButton;
    @FXML
    private ToggleButton homeButton;
    @FXML
    private ToggleButton booksListButton;
    @FXML
    private ToggleButton addBookButton;
    @FXML
    private ToggleButton userButton;
    @FXML
    private ToggleButton notificationsButton;
    @FXML
    private ToggleButton usersTableButton;
    @FXML
    private ToggleButton statisticsButton;
    @Getter
    @FXML
    private ToggleGroup navbar;
    @FXML
    private BorderPane mainContentPane;
    @FXML
    private Label userNameLabel;

    @FXML
    private Label unreadNotificationsLabel;


    @Autowired
    public MainController(AuthService authService,
                          ApplicationStateManager applicationStateManager,
                          @Lazy NotificationsController notificationsController) {
        this.authService = authService;
        this.applicationStateManager = applicationStateManager;
        this.notificationsController = notificationsController;
    }

    public void setCenterView(Node node) {
        mainContentPane.setCenter(node);
    }

    @FXML
    public void initialize() {
        homeButton.setOnAction(event -> {
            FXMLLoader loader = applicationStateManager.loadView("homepage.fxml");
            HomepageController homepageController = loader.getController();
            homepageController.initialize();
        });
        userButton.setOnAction(event -> {
            FXMLLoader loader = applicationStateManager.loadView("userProfile.fxml");
            UserProfileController userProfileController = (UserProfileController) loader.getController();
            userProfileController.update(applicationStateManager.getUser());
        });
        booksListButton.setOnAction(event -> {
            FXMLLoader loader = applicationStateManager.loadView("bookSearch.fxml");
            BookSearchController bookSearchController = loader.getController();
            bookSearchController.displaySearchResults();
        });

        addBookButton.setOnAction(event -> {
            FXMLLoader loader = applicationStateManager.loadView("bookAdd.fxml");
        });

        usersTableButton.setOnAction(event -> {
            FXMLLoader loader = applicationStateManager.loadView("usersTable.fxml");
        });

        statisticsButton.setOnAction(event -> {
            FXMLLoader loader = applicationStateManager.loadView("statistics.fxml");
        });

        notificationsButton.setOnAction(event -> {
            FXMLLoader loader = applicationStateManager.loadView("notifications.fxml");
        });

        logoutButton.setOnAction(event -> {
            logout();
        });

        if(userNameLabel.getScene() != null) {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();

            stage.setOnCloseRequest(event -> logout());
        }


        updateLayout(applicationStateManager.getUser());
        applicationStateManager.loadView("homepage.fxml");

        applicationStateManager.userProperty().addListener((observable, oldValue, newValue) -> {
            updateLayout(newValue);
        });

        unreadNotificationsLabel.textProperty().bind(notificationsController.getUnreadNotificationsProperty().asString());
        unreadNotificationsLabel.managedProperty().bind(notificationsController.getUnreadNotificationsProperty().greaterThan(0));
        unreadNotificationsLabel.visibleProperty().bind(notificationsController.getUnreadNotificationsProperty().greaterThan(0));

    }

    public void logout() {
        applicationStateManager.clearState();
        applicationStateManager.userProperty().set(null);
        authService.logout();
        Window.getWindows().forEach(window -> {
            if(window instanceof Stage stage) {
                if(!stage.getTitle().equals("Login")) {
                    Platform.runLater(stage::close);
                }
            }
        });
    }

    void updateLayout(User user) {
        if(user == null)
            return;
        applicationStateManager.loadView("homepage.fxml");
        notificationsController.retrieveNotifications(true);

        this.userNameLabel.setText("User: " + user.getName());

        List<UserRole> authorizedRoles = List.of(UserRole.ADMIN, UserRole.WORKER);
        boolean isAuthorized = (authorizedRoles.contains(user.getUserRole()));
        addBookButton.setManaged(isAuthorized);
        addBookButton.setVisible(isAuthorized);

        usersTableButton.setManaged(isAuthorized);
        usersTableButton.setVisible(isAuthorized);
    }
}

