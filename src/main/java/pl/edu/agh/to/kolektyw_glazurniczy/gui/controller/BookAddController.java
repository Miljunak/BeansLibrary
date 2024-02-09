package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.*;
import pl.edu.agh.to.kolektyw_glazurniczy.service.AuthorService;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BookService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BookAddController {


    private final BookService bookService;
    private final AuthorService authorService;
    private final FXMLLoaderFactory fxmlLoaderFactory;
    private final ApplicationStateManager applicationStateManager;
    @FXML
    public ComboBox<BookCategory> categoryComboBox;
    @FXML
    public ComboBox<Language> languageComboBox;
    @FXML
    public TextArea descriptionTextArea;
    @FXML
    private TextField titleTextField;
    @FXML
    private Label newAuthorLabel;
    @FXML
    private ComboBox<Author> authorComboBox;
    @FXML
    private TextField isbnTextField;
    @FXML
    private TextField imageUrlTextField;
    @FXML
    private TextField pageCountTextField;


    public BookAddController(BookService bookService,
                             AuthorService authorService,
                             FXMLLoaderFactory fxmlLoaderFactory,
                             ApplicationStateManager applicationStateManager) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        this.applicationStateManager = applicationStateManager;
    }


    @FXML
    public void initialize() {
        List<Author> authorList = authorService.getAllAuthors();
        authorComboBox.setItems(FXCollections.observableList(authorList));

        categoryComboBox.getItems().setAll(BookCategory.values());

        languageComboBox.getItems().setAll(Language.values());
        languageComboBox.setValue(Language.POLISH);

        pageCountTextField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            return newText.matches("([1-9][0-9]*)?") ? change : null;
        }));
    }


    @FXML
    private void handleSaveButton() {
        // Retrieve values from the form
        String title = titleTextField.getText();
        Author selectedAuthor = authorComboBox.getValue();
        String isbn = isbnTextField.getText();
        String description = descriptionTextArea.getText();
        int pageCount;

        try {
            pageCount = Integer.parseInt(pageCountTextField.getText());
        } catch (NumberFormatException i) {
            pageCount = -1;
        }
        BookCategory category = categoryComboBox.getValue();
        Language language = languageComboBox.getValue();
        String imageUrl = imageUrlTextField.getText();
        List<String> validationErrors = new ArrayList<>();

        if (title == null || title.isBlank()) {
            validationErrors.add("Title cannot be empty");
        }
        if (pageCount <= 0 || pageCount > 10000) {
            validationErrors.add("Invalid page count number");
        }
        if (isbn == null || isbn.isBlank()) {
            validationErrors.add("ISBN cannot be empty");
        }
        if (selectedAuthor == null) {
            validationErrors.add("Author is not selected");
        }

        if (category == null) {
            validationErrors.add("Category is not selected");
        }

        if (!validationErrors.isEmpty()) {
            System.out.println("Validation errors: \n" + String.join("\n", validationErrors));
            showInvalidDataAlert("Book form had validation errors", validationErrors);
            return;
        }

        Book newBook = new Book(isbn, title, List.of(selectedAuthor), category,
                pageCount, language, description, imageUrl);

        newBook.setBookStatistics(new BookStatistics(newBook, LocalDate.now(), 0, 0, 0));

        try {
            bookService.save(newBook);
            FXMLLoader loader = applicationStateManager.loadView("homepage.fxml");
            HomepageController homepageController = loader.getController();
            homepageController.initialize();
        } catch (DataAccessException exception) {
            showInvalidDataAlert("Failed to save the book", List.of(exception.getMessage()));
        }
    }

    private void showInvalidDataAlert(String header, List<String> validationErrors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(String.join("\n", validationErrors));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(event2 -> alert.close());
        alert.show();
    }


    @FXML
    public void addNewAuthor() {
        FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("view/authorAdd.fxml"));
        Parent parent = null;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(parent));
        stage.setTitle("Add new author");

        AuthorAddController authorAddController = loader.getController();

        stage.showAndWait();


        if (authorAddController.isAuthorAdded()) {
            List<Author> authorList = authorService.getAllAuthors();
            authorComboBox.setItems(FXCollections.observableList(authorList));
        }
    }
}
