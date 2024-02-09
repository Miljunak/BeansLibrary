package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import jakarta.validation.ConstraintViolation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.dto.UserRegistrationDTO;
import pl.edu.agh.to.kolektyw_glazurniczy.exception.RegistrationValidationException;
import pl.edu.agh.to.kolektyw_glazurniczy.exception.UserAlreadyExistException;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.service.AuthService;
import pl.edu.agh.to.kolektyw_glazurniczy.service.UserService;

import java.util.stream.Collectors;

@Controller
public class LoginController {

    private final AuthService authService;
    private final FXMLLoaderFactory fxmlLoaderFactory;
    private final ApplicationStateManager applicationStateManager;

    @FXML
    private VBox loginVBox;
    @FXML
    private VBox registerVBox;

    Logger logger = LoggerFactory.getLogger(LoginController.class);


    // login tab
    @FXML
    private TextField loginEmailField;
    @FXML
    private TextField loginPasswordField;

    // register tab
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button loginButton;

    @Autowired
    public LoginController(AuthService authService,
                           FXMLLoaderFactory fxmlLoaderFactory,
                           ApplicationStateManager applicationStateManager) {
        this.authService = authService;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        this.applicationStateManager = applicationStateManager;
    }


    @FXML
    public void initialize() {

        loginEmailField.clear();
        loginPasswordField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();

        registerButton.setOnAction(event -> {
            String name = firstNameField.getText() + " " + lastNameField.getText();
            UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO(
                    name, emailField.getText(), passwordField.getText()
            );

            try {
                User user = authService.register(userRegistrationDTO);
                successAlert(user, "Registered successfully");
                changeViewToLogin();
                loginEmailField.setText(emailField.getText());

            } catch (RegistrationValidationException e) {
                for (var error : e.getErrors()) {
                    logger.error(error.toString());
                }
                String errors = e.getErrors().stream().map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n"));
                exceptionAlert(e.getMessage(), errors);
            } catch (UserAlreadyExistException e) {
                logger.error(e.getMessage());
                exceptionAlert("Error", e.getMessage());
            }
        });
    }

    /**
     * used for testing
     *
     */
    public void fillLoginFormData(String username, String password) {
        loginEmailField.setText(username);
        loginPasswordField.setText(password);
    }


    private void exceptionAlert(String text, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Information");
        alert.setHeaderText(text);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event -> alert.close());

        alert.show();
    }

    private void successAlert(User user, String information) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Information");
        alert.setHeaderText("Success");
        alert.setContentText(information);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event -> alert.close());
        alert.show();
    }

    public void changeViewToLogin() {
        changeView(true);
    }

    public void changeViewToRegister() {
        changeView(false);
    }


    private void changeView(boolean toLogin) {
        registerVBox.setVisible(!toLogin);
        registerVBox.setManaged(!toLogin);

        loginVBox.setVisible(toLogin);
        loginVBox.setManaged(toLogin);
    }

    public void login() {
        try {
            User user = authService.login(loginEmailField.getText(), loginPasswordField.getText());
            logger.info("Successfully logged in");
            applicationStateManager.userProperty().set(user);

            FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/main.fxml"));

            Scene scene = new Scene(loader.load());

            Stage mainStage = new Stage();

            Stage loggingStage = (Stage) loginButton.getScene().getWindow();

            mainStage.setScene(scene);
            mainStage.initOwner(loggingStage);

            mainStage.setTitle("Beans Library");
            mainStage.getIcons().add(new Image("/imgs/icon.png"));

            mainStage.setOnHidden(event -> {
                loggingStage.show();
            });

            // Hide the logging scene
            loggingStage.hide();
            initialize();

            mainStage.show();

        } catch (Exception e) {
            exceptionAlert("Error", e.getMessage());
        }
    }
}
