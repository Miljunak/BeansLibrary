package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.ImageCacheManager;
import pl.edu.agh.to.kolektyw_glazurniczy.model.*;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BookReviewService;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class BookDetailsController {

    @FXML
    private Label availableCopiesLabel;
    @FXML
    private Label ibsnLabel;
    @FXML
    private ImageView bookCover;
    @FXML
    private Label titleLabel;
    @FXML
    private Label authorsLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label pagesCountLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label avgRateLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Button borrowButton;
    @FXML
    private Button reviewButton;
    @FXML
    private Pane reviewsList;
    @FXML
    public Button editButton;

    ObservableList<Review> bookReviews = FXCollections.observableArrayList();

    private Book book;
    private int rateTotal = 0;
    private int rateNumber = 0;

    private final FXMLLoaderFactory fxmlLoaderFactory;
    private final ApplicationStateManager applicationStateManager;
    private final ImageCacheManager imageCacheManager;
    private final BookReviewService bookReviewService;

    private final IntegerProperty availableCopiesProperty = new SimpleIntegerProperty();

    public BookDetailsController(FXMLLoaderFactory fxmlLoaderFactory,
                                 ApplicationStateManager applicationStateManager,
                                 ImageCacheManager imageCacheManager,
                                 BookReviewService bookReviewService) {
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        this.applicationStateManager = applicationStateManager;
        this.imageCacheManager = imageCacheManager;
        this.bookReviewService = bookReviewService;
    }

    public void initialize(Book book) {

        User user = applicationStateManager.getUser();

        if (user != null && user.getUserRole() == UserRole.ADMIN) {
            editButton.setVisible(true);
            editButton.setDisable(false);
        } else {
            editButton.setVisible(false);
            editButton.setDisable(true);
        }

        this.book = book;
        titleLabel.setText(book.getTitle());
        String authorsString = book.getAuthors().stream().map(Author::getName).collect(Collectors.joining());
        authorsLabel.setText(authorsString);
        pagesCountLabel.setText(String.valueOf(book.getPagesCount()));
        categoryLabel.setText(book.getCategory().name().toLowerCase());
        languageLabel.setText(book.getLanguage().getShortForm());
        ibsnLabel.setText(book.getIsbn());
        availableCopiesLabel.setText(String.valueOf(book.getBookStatistics().getAvailableCopies()));
        availableCopiesProperty.set(book.getBookStatistics().getAvailableCopies());
        descriptionLabel.setText(book.getDescription());

        if (applicationStateManager.getUser().isAccountSuspended()) {
            borrowButton.setDisable(true);
        } else {
            borrowButton.disableProperty().bind(Bindings.or(
                    Bindings.isNull(applicationStateManager.userProperty()),
                    Bindings.equal(0, availableCopiesProperty))
            );
        }

        bookCover.setImage(imageCacheManager.getImage(book.getImageUrl()));

        rateTotal = 0;
        rateNumber = 0;
        avgRateLabel.setText("N/A");

        // all book reviews
        bookReviews = FXCollections.observableArrayList();
        reviewsList.getChildren().clear();
        bookReviews.addListener((ListChangeListener<Review>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Review addedReview : c.getAddedSubList()) {
                        appendReview(addedReview, reviewsList);
                        rateTotal += addedReview.getRating();
                        rateNumber++;
                        calculateAverageRate();
                    }
                }
            }
        });

        bookReviews.addAll(bookReviewService.getBookReviews(book));

        // new review button
        reviewButton.setDisable(true);
        if (user != null && bookReviewService.userCanAddReview(book, user)) {
            reviewButton.setDisable(false);
        }

    }

    private void calculateAverageRate() {
        float avg = rateTotal / (float) rateNumber;
        avgRateLabel.setText(avg + " / 5");
    }

    @FXML
    private void handleBorrowingButtonClick() {
        FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/borrowing.fxml"));
        Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(borrowButton.getScene().getWindow());
        stage.setScene(new Scene(parent));
        stage.setTitle(book.getTitle());

        BorrowingController borrowingController = loader.getController();

        borrowingController.initialize(book);

        stage.showAndWait();
        availableCopiesLabel.setText(String.valueOf(book.getBookStatistics().getAvailableCopies()));
        availableCopiesProperty.set(book.getBookStatistics().getAvailableCopies());
    }

    @FXML
    private void handleAddReviewButtonClick() {
        try {
            FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/bookAddReview.fxml"));
            Pane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add book review");


            BookAddReviewController bookAddReviewController = loader.getController();
            bookAddReviewController.initialize(dialogStage, book, applicationStateManager.getUser());

            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(borrowButton.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            Optional<Review> review = bookReviewService.getUserBookReview(book, applicationStateManager.getUser());
            review.ifPresent(value -> {
                bookReviews.add(value);
                reviewButton.setDisable(true);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void appendReview(Review review, Pane location) {
        FXMLLoader loader;
        AnchorPane bookPreview;

        try {
            loader = new FXMLLoader(getClass().getClassLoader().getResource("view/bookReview.fxml"));
            bookPreview = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Label label;
        label = (Label) bookPreview.lookup("#title");
        label.setText(review.getTitle());
        label = (Label) bookPreview.lookup("#username");
        label.setText(review.getUser().getName());
        label = (Label) bookPreview.lookup("#text");
        label.setText(review.getReviewText());
        label = (Label) bookPreview.lookup("#rating");
        label.setText(review.getRatingText());

        location.getChildren().add(bookPreview);
    }

    public void handleEditButtonClick() {
        try {
            FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/editBookDetails.fxml"));
            Parent parent = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(borrowButton.getScene().getWindow());
            stage.setScene(new Scene(parent));
            stage.setTitle("Edit book details");


            EditBookDetailsController editController = loader.getController();
            editController.initialize(book);

            stage.showAndWait();

            this.initialize(book);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
