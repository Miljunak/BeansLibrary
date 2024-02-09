package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Notification;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Notification.NotificationDestination;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Notification.NotificationType;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.service.EmailService;
import pl.edu.agh.to.kolektyw_glazurniczy.service.NotificationService;

import java.util.ArrayList;
import java.util.List;

@Controller
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NotificationAddController {

    @FXML
    private ComboBox<Notification.NotificationType> notificationTypeComboBox;
    @FXML
    private Label notificationTitleLabel;
    @FXML
    private Button sendButton;
    @FXML
    private Button cancelButton;
    @FXML
    private CheckBox sendEmailCheckBox;
    @FXML
    private CheckBox sendInAppCheckBox;
    @FXML
    private TextArea notificationMessageTextArea;
    @FXML
    private ComboBox<User> selectedUsersComboBox;

    private final NotificationService notificationService;
    private final EmailService emailService;

    private final User ALL_USERS = new User("All Users", null, null);
    private final User SELECTED_GROUP = new User("Selected Group", null, null);


    private final ApplicationStateManager applicationStateManager;
    private List<User> allUsers = new ArrayList<>();
    private List<User> selectedUsersGroup = new ArrayList<>();

    public NotificationAddController(NotificationService notificationService,
                                     EmailService emailService,
                                     ApplicationStateManager applicationStateManager) {
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.applicationStateManager = applicationStateManager;
    }

    @FXML
    public void initialize() {
        sendInAppCheckBox.setSelected(true);
        notificationTypeComboBox.getItems().setAll(NotificationType.values());
        notificationTypeComboBox.setValue(NotificationType.NEW_BOOK);

        selectedUsersComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<User> call(ListView<User> param) {
                return new ListCell<User>() {
                    @Override
                    protected void updateItem(User item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        selectedUsersComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User object) {
                if (object == null) {
                    return null;
                }
                return object.getName();
            }

            @Override
            public User fromString(String string) {
                return allUsers.stream().filter(user -> user.getUsername().equals(string)).toList().get(0);
            }
        });

        selectedUsersComboBox.getItems().add(ALL_USERS);
        selectedUsersComboBox.setValue(ALL_USERS);
    }

    public void setAllUsers(List<User> users) {
        this.allUsers = users;
        selectedUsersComboBox.getItems().addAll(users);
    }

    public void setSelectedUsersGroup(List<User> users) {
        this.selectedUsersGroup = users;
        if (!users.isEmpty()) {
            selectedUsersComboBox.getItems().add(SELECTED_GROUP);
            selectedUsersComboBox.setValue(SELECTED_GROUP);

        }
    }

    public void sendNotification() {
        sendEmailCheckBox.getScene().setCursor(Cursor.WAIT);

        String title = notificationTitleLabel.getText();
        String message = notificationMessageTextArea.getText();
        Notification.NotificationType notificationType = notificationTypeComboBox.getValue();
        User currentSelection = selectedUsersComboBox.getValue();
        User sender = applicationStateManager.getUser();
        List<User> messageReceivers;


        if (currentSelection == SELECTED_GROUP) {
            messageReceivers = selectedUsersGroup;
        } else if (currentSelection == ALL_USERS) {
            messageReceivers = allUsers;
        } else {
            messageReceivers = List.of(currentSelection);
        }

        new Thread(() -> {
            if (sendInAppCheckBox.isSelected()) {

                for (User user : messageReceivers) {
                    Notification notification = new Notification(title, message, NotificationDestination.IN_APP, notificationType);
                    notificationService.sendNotification(notification, sender, user);
                }
            }
            if (sendEmailCheckBox.isSelected()) {
                for (User user : messageReceivers) {
                    Notification notification = new Notification(title, message, NotificationDestination.EMAIL, notificationType);
                    emailService.sendEmail(notification, sender, user);
                }
            }
            Platform.runLater(() -> {
                sendEmailCheckBox.getScene().setCursor(Cursor.DEFAULT);
                closeStage();
            });
        }).start();

    }

    public void cancelNotification() {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) sendButton.getScene().getWindow();
        stage.close();
    }
}
