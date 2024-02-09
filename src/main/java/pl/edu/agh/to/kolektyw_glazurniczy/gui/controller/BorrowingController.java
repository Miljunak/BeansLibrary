package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Borrowing;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BorrowingService;

import java.time.LocalDate;

@Controller
public class BorrowingController {

    @FXML
    public Label bookNameLabel;
    @FXML
    public Label borrowDateLabel;
    @FXML
    public Label dueDateLabel;
    @FXML
    private Button reserveButton;
    @FXML
    private Button cancelButton;

    private final BorrowingService borrowingService;
    private Book book;
    private boolean borrowingSuccessful = false;
    private final ApplicationStateManager applicationStateManager;

    public BorrowingController(BorrowingService borrowingService, ApplicationStateManager applicationStateManager) {
        this.borrowingService = borrowingService;
        this.applicationStateManager = applicationStateManager;
    }

    @FXML
    private void borrowBook() {
        borrowingSuccessful = borrowingService.borrowBook(book, applicationStateManager.getUser()) != null;
        closeModal();
    }

    public void initialize(Book book) {
        this.book = book;
        LocalDate currentDate = LocalDate.now();
        borrowDateLabel.setText(currentDate.toString());
        dueDateLabel.setText(currentDate.plus(Borrowing.BORROWING_DURATION).toString());
        bookNameLabel.setText(book.getTitle());
    }

    @FXML
    private void cancelReservation() {
        closeModal();
    }

    private void closeModal() {
        reserveButton.getScene().getWindow().hide();
    }

    public boolean isBorrowingSuccessful() {
        return borrowingSuccessful;
    }
}
