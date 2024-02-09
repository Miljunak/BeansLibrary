package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;


import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Borrowing;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BorrowingService;

import java.time.LocalDate;
import java.util.List;

@Controller
public class UserProfileController {

    @FXML
    private Label userTypeLabel;
    @FXML
    private TableColumn<Borrowing, LocalDate> dueDateColumn;
    @FXML
    private Label usernameLabel;
    @FXML
    private TableView<Borrowing> borrowingTable;
    @FXML
    private TableColumn<Borrowing, String> bookNameColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> returnDateColumn;
    @FXML
    private TableColumn<Borrowing, Void> returnColumn;

    private final BorrowingService borrowingService;
    private User user;


    public UserProfileController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @FXML
    public void initialize() {
        initializeBorrowingTable();
    }

    void update(User user) {
        this.user = user;
        if (user == null) {
            usernameLabel.setText("not logged in");
            usernameLabel.setText("");
            borrowingTable.getItems().clear();
        } else {
            usernameLabel.setText(user.getUsername());
            userTypeLabel.setText(user.getUserRole().name());
        }
        updateBorrowingTable();
    }

    private void initializeBorrowingTable() {
        bookNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getBook().getTitle()));
        borrowDateColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getStartDate()));
        dueDateColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getDueDate()));
        returnDateColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getReturnDate()));
        returnColumn.setCellFactory(createReturnButtonCellFactory());
    }

    private Callback<TableColumn<Borrowing, Void>, TableCell<Borrowing, Void>> createReturnButtonCellFactory() {
        return param -> new javafx.scene.control.TableCell<>() {
            private final Button returnButton = new Button("Return");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty) {
                    setGraphic(null);
                } else {
                    Borrowing borrowing = getTableView().getItems().get(getIndex());

                    if (borrowing.getReturnDate() == null) {
                        returnButton.setOnAction(event -> handleReturnButtonClick(borrowing));
                        setGraphic(returnButton);
                    } else {
                        setGraphic(null);
                    }

                    // Highlight the row if return date is before the current day
                    if (borrowing.getReturnDate() != null && borrowing.getReturnDate().isBefore(LocalDate.now())) {
                        setStyle("-fx-background-color: #e78080;");
                    } else {
                        setStyle(""); // Reset style
                    }
                }
            }
        };
    }

    private void updateBorrowingTable() {
        if (user != null) {
            List<Borrowing> borrowings = borrowingService.getBorrowingsForUser(user);
            ObservableList<Borrowing> observableBorrowings = FXCollections.observableArrayList(borrowings);
            borrowingTable.setItems(observableBorrowings);
        } else {
            borrowingTable.setItems(FXCollections.emptyObservableList());
        }
    }

    private void handleReturnButtonClick(Borrowing borrowing) {
        borrowingService.returnBook(borrowing);
        updateBorrowingTable();
    }
}
