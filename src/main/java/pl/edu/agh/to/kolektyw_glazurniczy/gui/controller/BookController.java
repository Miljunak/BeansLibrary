package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.ImageCacheManager;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Author;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;

import java.io.IOException;
import java.util.stream.Collectors;

@Controller
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BookController {

    public Label availableCopiesLabel;
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
    private Button borrowButton;
    private Book book;

    private final ApplicationStateManager applicationStateManager;
    private final FXMLLoaderFactory fxmlLoaderFactory;
    private final ImageCacheManager imageCacheManager;

    private final IntegerProperty availableCopiesProperty = new SimpleIntegerProperty();

    public BookController(ApplicationStateManager applicationStateManager,
                          FXMLLoaderFactory fxmlLoaderFactory,
                          ImageCacheManager imageCacheManager) {
        this.applicationStateManager = applicationStateManager;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        this.imageCacheManager = imageCacheManager;
    }

    public void initialize(Book book) {
        this.book = book;
        titleLabel.setText(book.getTitle());
        String authorsString = book.getAuthors().stream().map(Author::getName).collect(Collectors.joining());
        authorsLabel.setText(authorsString);
        pagesCountLabel.setText("Liczba stron: " + book.getPagesCount());
        categoryLabel.setText(book.getCategory().name().toLowerCase());
        languageLabel.setText(book.getLanguage().getShortForm());
        availableCopiesProperty.set(book.getBookStatistics().getAvailableCopies());
        availableCopiesLabel.setText("Dostępność - " + book.getBookStatistics().getAvailableCopies() + " sztuk");

        if (applicationStateManager.getUser().isAccountSuspended()) {
            borrowButton.setDisable(true);
        } else {
            borrowButton.disableProperty().bind(Bindings.or(
                    Bindings.isNull(applicationStateManager.userProperty()),
                    Bindings.equal(0, availableCopiesProperty))
            );
        }

        bookCover.setImage(imageCacheManager.getImage(book.getImageUrl()));
    }

    @FXML
    public void gotoBookDetails() {
        FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/bookDetails.fxml"));
        Parent parent;

        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage stage = new Stage();
        stage.setScene(new Scene(parent));
        stage.setTitle(book.getTitle());

        BookDetailsController BDController = loader.getController();
        BDController.initialize(book);

        stage.showAndWait();
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
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(parent));
        stage.setTitle("Borrow");

        BorrowingController borrowingController = loader.getController();

        borrowingController.initialize(book);

        stage.showAndWait();
        availableCopiesLabel.setText("Dostępność - " + book.getBookStatistics().getAvailableCopies() + " sztuk");
        availableCopiesProperty.set(book.getBookStatistics().getAvailableCopies());
    }
}
