package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Review;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BookReviewService;


@Controller
public class BookAddReviewController {
    private Stage stage;

    @FXML
    private Label bookTitle;
    @FXML
    private TextField reviewTitle;
    @FXML
    private TextArea reviewText;

    private Short rate = 1;

    @FXML
    private RadioButton R1;
    @FXML
    private RadioButton R2;
    @FXML
    private RadioButton R3;
    @FXML
    private RadioButton R4;
    @FXML
    private RadioButton R5;

    private ToggleGroup radioGroup;
    private final BookReviewService bookReviewService;
    private User user;
    private Book book;

    public BookAddReviewController(BookReviewService bookReviewService) {
        this.bookReviewService = bookReviewService;
    }

    public void initialize(Stage stage, Book book, User user) {
        this.stage = stage;
        this.book = book;
        this.user = user;
        bookTitle.setText(book.getTitle());
        radioGroup = new ToggleGroup();

    }

    @FXML
    private void handleSendReviewButton() {
        String reviewTitleText = reviewTitle.getText();
        if (reviewTitleText.isEmpty()) {
            reviewTitleText = "Title not given";
        }
        Review review = new Review(user, book, reviewTitleText, reviewText.getText(), rate);
        bookReviewService.addReview(review);
        stage.close();
    }

    @FXML
    private void handleRadioButtonAction() {
        R1.setToggleGroup(radioGroup);
        R2.setToggleGroup(radioGroup);
        R3.setToggleGroup(radioGroup);
        R4.setToggleGroup(radioGroup);
        R5.setToggleGroup(radioGroup);

        RadioButton selectedRadioButton = (RadioButton) radioGroup.getSelectedToggle();
        if (selectedRadioButton != null) {
            rate = Short.parseShort(selectedRadioButton.getText());
        }

    }
}
