package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Notification;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Controller
public class NotificationsController {


    @FXML
    private Button refreshButton;
    @FXML
    private TableView<Notification> notificationsTable;
    @FXML
    private TableColumn<Notification, Void> unreadColumn;
    @FXML
    private TableColumn<Notification, Notification.NotificationType> notificationTypeColumn;
    @FXML
    private TableColumn<Notification, String> senderColumn;
    @FXML
    private TableColumn<Notification, String> notificationTitleColumn;
    @FXML
    private TableColumn<Notification, LocalDateTime> sendDateColumn;
    @FXML
    private Label shownNotificationTitleLabel;
    @FXML
    private Label shownNotificationTypeLabel;
    @FXML
    private TextArea shownNotificationMessageLabel;
    @FXML
    private Label shownNotificationDateLabel;
    @FXML
    private CheckBox hideReadCheckBox;
    @FXML
    private TextField filterNotificationsTextField;

    private final NotificationService notificationService;
    private final ApplicationStateManager applicationStateManager;

    private final ObservableList<Notification> userNotifications = FXCollections.observableArrayList();

    private FilteredList<Notification> filteredNotifications;

    private Predicate<Notification> filterPredicate = (notification) -> true;
    private Predicate<Notification> hideReadPredicate = (notification) -> true;;


    @Getter
    private final SimpleIntegerProperty unreadNotificationsProperty = new SimpleIntegerProperty(0);

    public NotificationsController(NotificationService notificationService,
                                   ApplicationStateManager applicationStateManager) {
        this.notificationService = notificationService;
        this.applicationStateManager = applicationStateManager;
    }

    @FXML
    public void initialize() {
        initializeTable();
        retrieveNotifications(false);

        filteredNotifications = new FilteredList<>(userNotifications);
        filteredNotifications.setPredicate(filterPredicate.and(hideReadPredicate));

        notificationsTable.setItems(filteredNotifications);
        notificationsTable.getSortOrder().setAll(sendDateColumn);

        setEmptyMessage();
    }

    private void initializeTable() {
        unreadColumn.setCellFactory(createUnreadNotificationCellFactory());
        notificationTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        notificationTypeColumn.setCellValueFactory(new PropertyValueFactory<>("notificationType"));
        senderColumn.setCellValueFactory(param -> new SimpleStringProperty(getSenderName(param.getValue().getSender())));

        StringConverter<LocalDateTime> converter = new LocalDateTimeStringConverter();
        sendDateColumn.setCellFactory(column -> createFormattedDateCellFactory(converter));
        sendDateColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSendDateTime()));


        notificationsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                shownNotificationTitleLabel.setText(newValue.getTitle());
                shownNotificationTypeLabel.setText(newValue.getNotificationType().name());
                shownNotificationMessageLabel.setText(newValue.getMessage());
                shownNotificationDateLabel.setText(converter.toString(newValue.getSendDateTime()));
                if(!newValue.isOpened()) {
                    newValue.setOpened(true);
                    notificationService.markAsOpen(newValue);
                    unreadNotificationsProperty.set((int) userNotifications.stream().filter(not -> !not.isOpened()).count());
                }
                notificationsTable.refresh();
            }
        });
        refreshButton.setOnAction(event -> {
            refreshButton.setDisable(true);
            retrieveNotifications(true);
            refreshButton.setDisable(false);

        });
    }

    private String getSenderName(User sender) {
        if(sender == null) {
            return "APP";
        } else {
            return sender.getName();
        }
    }

    private void setEmptyMessage() {
        shownNotificationTitleLabel.setText("");
        shownNotificationTypeLabel.setText("");
        shownNotificationMessageLabel.setText("Select a notification to view it here");
        shownNotificationDateLabel.setText("");
    }

    private static TableCell<Notification, LocalDateTime> createFormattedDateCellFactory(StringConverter<LocalDateTime> converter) {
        return new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(converter.toString(item));
                }
            }
        };
    }

    private Callback<TableColumn<Notification, Void>, TableCell<Notification, Void>> createUnreadNotificationCellFactory() {
        return param -> new TableCell<>() {
            private final Circle circle = new Circle(8);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty) {
                    setGraphic(null);
                    getTableRow().getStyleClass().clear();
                    setStyle("");
                } else {
                    Notification notification = (Notification) getTableView().getItems().get(getIndex());
                    circle.setFill(getCircleColor(notification.getNotificationType()));
                    setGraphic(circle);
                    if (notification.isOpened()) {
                        getTableRow().getStyleClass().remove("table-row-unread");
                        setStyle("-fx-opacity: 0.4;");
                    } else {
                        getTableRow().getStyleClass().add("table-row-unread");
                    }
                }
            }
        };
    }

    private Paint getCircleColor(Notification.NotificationType notificationType) {
        if(notificationType == null)
            return Color.WHITE;
        return switch (notificationType) {
            case BOOK_OVERDUE -> Color.ORANGE;
            case NEW_BOOK -> Color.YELLOW;
            case EVENT -> Color.BLUE;
            case PROMOTION -> Color.GREEN;
            case IMPORTANT -> Color.RED;
            default -> Color.WHITE;
        };
    }

    public void retrieveNotifications(boolean update) {
        if (!update && !userNotifications.isEmpty())
            return;

        new Thread(() -> {
            List<Notification> notifications = notificationService.getNotificationForUser(applicationStateManager.getUser());
            Platform.runLater(() -> {
                userNotifications.setAll(notifications);
                unreadNotificationsProperty.set((int) notifications.stream().filter(notification -> !notification.isOpened()).count());
            });

        }).start();
    }

    public void toggleNotificationsVisibility() {
        if(hideReadCheckBox.isSelected()) {
            hideReadPredicate = (notification) -> !notification.isOpened();
        } else {
            hideReadPredicate = (notification) -> true;
        }
        filteredNotifications.setPredicate(filterPredicate.and(hideReadPredicate));
    }

    public void filterNotifications() {
        String query = filterNotificationsTextField.getText();
        filterPredicate = (notification) -> query.isEmpty()
                || notification.getTitle().contains(query)
                || notification.getNotificationType().name().contains(query.toUpperCase())
                || getSenderName(notification.getSender()).contains(query);

        filteredNotifications.setPredicate(filterPredicate.and(hideReadPredicate));
    }
}
