package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateTimeStringConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Borrowing;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.model.UserRole;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BorrowingService;
import pl.edu.agh.to.kolektyw_glazurniczy.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UsersTableController {

    @FXML
    private TextField filterBox;
    @FXML
    private Button suspendUsersButton;
    @FXML
    private Button changePermissionsButton;
    @FXML
    private CheckBox showSuspendedCheckBox;
    @FXML
    private TableView<UserViewModel> userTable;
    @FXML
    private TableColumn<UserViewModel, Boolean> selectColumn;
    @FXML
    private TableColumn<UserViewModel, String> usernameColumn;
    @FXML
    private TableColumn<UserViewModel, String> emailColumn;
    @FXML
    private TableColumn<UserViewModel, UserRole> roleColumn;
    @FXML
    private TableColumn<UserViewModel, LocalDateTime> registrationDateColumn;
    @FXML
    private TableColumn<UserViewModel, LocalDateTime> lastLoginDateColumn;
    @FXML
    private TableColumn<UserViewModel, Integer> totalBorrowedBooksColumn;
    @FXML
    private TableColumn<UserViewModel, Integer> currentlyBorrowedBooksColumn;
    @FXML
    private TableColumn<UserViewModel, Integer> lateBooksColumn;

    private final ApplicationStateManager applicationStateManager;
    private final UserService userService;
    private final BorrowingService borrowingService;
    private final ObservableList<UserViewModel> userViewModelList = FXCollections.observableArrayList();

    private volatile boolean isRefreshInProgress = false;
    private final LocalDate today = LocalDate.now();

    private FilteredList<UserViewModel> filteredUserViewModelList;

    private final FXMLLoaderFactory fxmlLoaderFactory;

    public UsersTableController(ApplicationStateManager applicationStateManager,
                                BorrowingService borrowingService,
                                FXMLLoaderFactory fxmlLoaderFactory,
                                UserService userService) {
        this.applicationStateManager = applicationStateManager;
        this.userService = userService;
        this.borrowingService = borrowingService;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
    }

    @FXML
    public void initialize() {
        initializeUserTable();

        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {
            UserViewModel userViewModel = new UserViewModel(user);
            userViewModelList.add(userViewModel);

            Platform.runLater(() -> {
                List<Borrowing> lateBorrowings = borrowingService.getBorrowingsForUser(user);
                userViewModel.totalBorrowedBooksProperty().set(lateBorrowings.size());
                userViewModel.currentlyBorrowedBooksProperty.set(
                        (int) lateBorrowings.stream().filter(borrowing -> borrowing.getReturnDate() == null).count()
                );
                userViewModel.lateBooksProperty().set(
                        (int) lateBorrowings.stream().filter(
                                borrowing -> borrowing.getReturnDate() == null && borrowing.getDueDate().isBefore(today)
                        ).count()
                );
            });

        }

        filteredUserViewModelList = new FilteredList<>(userViewModelList);
        userTable.getItems().addAll(userViewModelList);


        if (!applicationStateManager.getUser().getUserRole().equals(UserRole.ADMIN)) {
            suspendUsersButton.setDisable(true);
        }

    }

    private void initializeUserTable() {
        selectColumn.setCellValueFactory(param -> param.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        usernameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().emailProperty());
        roleColumn.setCellValueFactory(param -> param.getValue().userRoleProperty());
        StringConverter<LocalDateTime> converter = new LocalDateTimeStringConverter();
        registrationDateColumn.setCellValueFactory(param -> param.getValue().registrationDateProperty());
        registrationDateColumn.setCellFactory(column -> createFormattedDateCellFactory(converter));
        lastLoginDateColumn.setCellValueFactory(param -> param.getValue().lastLoginDateProperty());
        lastLoginDateColumn.setCellFactory(column -> createFormattedDateCellFactory(converter));

        totalBorrowedBooksColumn.setCellValueFactory(param -> param.getValue().totalBorrowedBooksProperty().asObject());
        currentlyBorrowedBooksColumn.setCellValueFactory(param -> param.getValue().currentlyBorrowedBooksProperty().asObject());
        lateBooksColumn.setCellValueFactory(param -> param.getValue().lateBooksProperty().asObject());

        // Set gray background for suspended users
        userTable.setRowFactory(tableView -> new TableRow<>() {
            @Override
            protected void updateItem(UserViewModel userViewModel, boolean empty) {
                super.updateItem(userViewModel, empty);
                if (userViewModel != null && !empty) {
                    styleProperty().bind(Bindings.when(userViewModel.suspendedProperty())
                            .then("-fx-background-color: #aba4a4; -fx-border-width: 1;")
                            .otherwise(""));
                } else {
                    styleProperty().unbind();
                    setStyle("");
                }
            }
        });
    }

    private static TableCell<UserViewModel, LocalDateTime> createFormattedDateCellFactory(StringConverter<LocalDateTime> converter) {
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

    private List<UserViewModel> getSelectedUsers() {
        return filteredUserViewModelList.stream().filter(userViewModel -> userViewModel.selectedProperty.get()).toList();
    }

    private Optional<ButtonType> showAlert(Alert.AlertType type, String header) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event2 -> alert.close());
        return alert.showAndWait();
    }

    // only visible users should be selected
    public void selectAllAction() {
        userViewModelList.forEach(
                userViewModel -> userViewModel.selectedProperty().set(filteredUserViewModelList.contains(userViewModel)))
        ;
    }

    // all users should be unselected
    public void unselectAllAction() {
        userViewModelList.forEach(userViewModel -> userViewModel.selectedProperty().set(false));
    }

    public void suspendUserAccounts() {
        List<UserViewModel> users = getSelectedUsers();
        if (users.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No users selected");
            return;
        }
        // User cannot block himself
        if (users.stream().map(
                userViewModel -> userViewModel.userEntity.getId()).toList().contains(applicationStateManager.getUser().getId())) {
            showAlert(Alert.AlertType.ERROR, "You cannot suspend yourself");
            return;
        }
        Optional<ButtonType> buttonType = showAlert(
                Alert.AlertType.CONFIRMATION, "Are you sure you want to suspend %d users?".formatted(users.size())
        );

        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            for (UserViewModel user : users) {
                userService.suspendUserAccount(user.userEntity);
                user.suspendedProperty().set(true);
            }
        }
        filterUsers();
    }

    public void changePermissions() {
        List<UserViewModel> selectedUsers = getSelectedUsers();
        if (selectedUsers.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "No users selected");
            return;
        }
        if (selectedUsers.stream().map(
                userViewModel -> userViewModel.userEntity.getId()).toList().contains(applicationStateManager.getUser().getId())) {
            showAlert(Alert.AlertType.ERROR, "You cannot change your own permissions");
            return;
        }


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure you want to change permissions for %d users?".formatted(selectedUsers.size()));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event2 -> alert.close());
        ComboBox<UserRole> comboBox = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        DialogPane dialogPane = alert.getDialogPane();
        VBox vBox = new VBox(new Label("Select new Permission"), comboBox);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setFillWidth(true);
        dialogPane.setContent(vBox);

        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK && comboBox.getValue() != null) {
            for (UserViewModel user : selectedUsers) {
                User changedUser = userService.changeRole(user.userEntity, comboBox.getValue());
                user.userEntity.setUserRole(changedUser.getUserRole());
                user.userRoleProperty().set(user.userEntity.getUserRole());
            }
        }
        refreshTable();
    }

    public void sendNotifications() {
        List<UserViewModel> selectedUsers = getSelectedUsers();
        FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/notificationAdd.fxml"));
        Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (loader.getController() instanceof NotificationAddController notificationAddController) {
            notificationAddController.setAllUsers(userService.getAllUsers());
            notificationAddController.setSelectedUsersGroup(selectedUsers.stream().map(UserViewModel::userEntity).toList());

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(changePermissionsButton.getScene().getWindow());
            stage.setScene(new Scene(parent));

            stage.showAndWait();

        }

    }

    public void filterUsers() {
        boolean showSuspended = showSuspendedCheckBox.isSelected();
        filteredUserViewModelList.setPredicate(userViewModel -> shouldBeVisible(userViewModel, showSuspended));
        refreshTable();
    }

    private void refreshTable() {
        if (!isRefreshInProgress) {
            isRefreshInProgress = true;
            Platform.runLater(() -> {
                userTable.getItems().clear();
                userTable.getItems().addAll(filteredUserViewModelList);
                isRefreshInProgress = false;
            });
        }
    }

    private boolean shouldBeVisible(UserViewModel userViewModel, boolean showSuspended) {
        String query = filterBox.getText();
        return (showSuspended || !userViewModel.suspendedProperty().get()) &&
                (query == null || query.isEmpty() ||
                        userViewModel.nameProperty().get().contains(query) ||
                        userViewModel.emailProperty().get().contains(query));
    }

    public record UserViewModel(
            SimpleStringProperty nameProperty,
            SimpleStringProperty emailProperty,
            SimpleObjectProperty<UserRole> userRoleProperty,
            SimpleObjectProperty<LocalDateTime> registrationDateProperty,
            SimpleObjectProperty<LocalDateTime> lastLoginDateProperty,
            SimpleIntegerProperty totalBorrowedBooksProperty,
            SimpleIntegerProperty currentlyBorrowedBooksProperty,
            SimpleIntegerProperty lateBooksProperty,
            SimpleBooleanProperty selectedProperty,
            SimpleBooleanProperty suspendedProperty,
            User userEntity) {

        public UserViewModel(User user) {
            this(
                    new SimpleStringProperty(user.getUsername()),
                    new SimpleStringProperty(user.getEmail()),
                    new SimpleObjectProperty<>(user.getUserRole()),
                    new SimpleObjectProperty<>(user.getRegistrationDate()),
                    new SimpleObjectProperty<>(user.getLastLoginDate()),
                    new SimpleIntegerProperty(0),
                    new SimpleIntegerProperty(0),
                    new SimpleIntegerProperty(0),
                    new SimpleBooleanProperty(false),
                    new SimpleBooleanProperty(user.isAccountSuspended()),
                    user
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserViewModel that = (UserViewModel) o;
            return Objects.equals(userEntity.getId(), that.userEntity.getId());
        }
    }
}

