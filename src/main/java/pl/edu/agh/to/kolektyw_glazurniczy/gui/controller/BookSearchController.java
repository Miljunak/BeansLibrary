package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.BookCategory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Language;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BookService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BookSearchController {

    @FXML
    private FlowPane listOfBooks;
    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchButton;
    @FXML
    public TextField advancedSearchTextField;
    @FXML
    public Button advancedSearchButton;
    @FXML
    public ComboBox<BookCategory> bookCategoryComboBox;
    @FXML
    public ComboBox<Language> languageComboBox;
    @FXML
    public Slider sliderInput;
    @FXML
    private TableView<Book> searchResultsTable;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> categoryColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> languageColumn;
    @FXML
    private TableColumn<Book, Double> ratingColumn;
    @FXML
    private TableColumn<Book, Integer> pagesColumn;
    @FXML
    private TableColumn<Book, Integer> availableColumn;

    private final BookService bookService;

    private List<Book> allBooks = new ArrayList<>();
    private final ObservableList<Book> unlimitedBookObservableList = FXCollections.observableArrayList();

    Logger logger = LoggerFactory.getLogger(BookSearchController.class);
    private final FXMLLoaderFactory fxmlLoaderFactory;

    public BookSearchController(BookService bookService, FXMLLoaderFactory fxmlLoaderFactory) {
        this.bookService = bookService;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
    }


    @FXML
    public void initialize() {

        // Basic Search
        searchTextField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                displaySearchResults();
            }
        });

        allBooks = this.bookService.getAllBooks();

        displaySearchResults();

        // Advanced search
        bookCategoryComboBox.getItems().setAll(BookCategory.values());

        languageComboBox.getItems().setAll(Language.values());

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        languageColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        pagesColumn.setCellValueFactory(new PropertyValueFactory<>("pagesCount"));
        availableColumn.setCellValueFactory(cellData -> cellData.getValue().availableCopiesProperty().asObject());


        unlimitedBookObservableList.addAll(allBooks);

        searchResultsTable.setItems(FXCollections.observableList(unlimitedBookObservableList));

        searchResultsTable.setRowFactory(tableView -> {
            final TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    // Get the selected book from the clicked row
                    Book selectedBook = row.getItem();

                    openBookDetailsWindow(selectedBook);
                }
            });
            return row;
        });
    }

    private void openBookDetailsWindow(Book book) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("view/bookDetails.fxml"));

            Parent parent = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(parent));
            stage.setTitle(book.getTitle());

            BookDetailsController BDController = loader.getController();
            BDController.initialize(book);

            stage.showAndWait();

            searchResultsTable.getItems().clear();
            displayFilteredSearchResults();

        } catch (IOException e) {
            logger.error("Failed to open BookDetails window: " + e.getMessage());
        }
    }


    @FXML
    public void displaySearchResults() {
        displaySearchResults(searchTextField.getText());
    }

    public void displaySearchResults(String searchQuery) {
        listOfBooks.getChildren().clear();
        List<Book> foundBooks;

        if (searchQuery == null || searchQuery.isBlank()) {
            searchTextField.setText("");
            foundBooks = allBooks.stream().limit(10).collect(Collectors.toList());
        } else {
            searchTextField.setText(searchQuery);
            foundBooks = allBooks.stream().filter(book ->
                            containsCaseInsensitive(book.getTitle(), searchQuery)
                                    || containsCaseInsensitive(book.getAuthors().get(0).getName(), searchQuery))
                    .toList();
        }
        for (Book book : foundBooks) {
            try {
                FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
                loader.setLocation(getClass().getClassLoader().getResource("view/book.fxml"));

                AnchorPane bookView = loader.load();

                BookController controller = loader.getController();
                controller.initialize(book);

                listOfBooks.getChildren().add(bookView);

            } catch (IOException e) {
                logger.error("Failed to display a book: " + e.getMessage());
            }
        }
    }

    private boolean containsCaseInsensitive(String searchedString, String query) {
        return searchedString.toLowerCase().contains(query.toLowerCase());
    }

    @FXML
    public void displayFilteredSearchResults() {

        String searchQuery = advancedSearchTextField.getText();
        String categoryName = (bookCategoryComboBox.getValue() == null || bookCategoryComboBox.getValue().equals(BookCategory.NONE)) ?
                null : bookCategoryComboBox.getValue().toString();
        String language = (languageComboBox.getValue() == null || languageComboBox.getValue().equals(Language.NONE)) ?
                null : languageComboBox.getValue().toString();
        Double rating = sliderInput.getValue();

        displayFilteredSearchResults(searchQuery, categoryName, language, rating);
    }

    private void displayFilteredSearchResults(String searchQuery,
                                              String chosenCategory,
                                              String chosenLanguage,
                                              Double minimumRating) {

        List<Book> filteredBooks = allBooks.stream()
                .filter(book ->
                        searchQuery == null ||
                                book.getTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                book.getAuthor().getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .filter(book -> chosenCategory == null || book.getCategory().toString().equalsIgnoreCase(chosenCategory))
                .filter(book -> chosenLanguage == null || book.getLanguage().toString().equalsIgnoreCase(chosenLanguage))
                .filter(book -> minimumRating == null || book.getRating() >= minimumRating)
                .toList();

        searchResultsTable.getItems().clear();
        searchResultsTable.getItems().addAll(filteredBooks);
    }
}
