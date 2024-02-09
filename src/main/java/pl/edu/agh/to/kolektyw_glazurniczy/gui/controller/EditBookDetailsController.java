package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.ImageCacheManager;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Author;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.BookCategory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Language;
import pl.edu.agh.to.kolektyw_glazurniczy.service.AuthorService;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BookService;

import java.util.List;


@Controller
public class EditBookDetailsController {

    @FXML
    private TextField titleTextField;
    @FXML
    private ComboBox<Author> authorsComboBox;
    @FXML
    private ComboBox<BookCategory> categoryComboBox;
    @FXML
    private TextField pagesCountTextField;
    @FXML
    private TextField ibsnTextField;
    @FXML
    private ComboBox<Language> languageComboBox;
    @FXML
    private TextField availableCopiesTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private TextField imageUrlTextField;
    @FXML
    private Button confirmButton;
    @FXML
    private ImageView bookCover;

    private Book book;
    private final ImageCacheManager imageCacheManager;
    private final AuthorService authorService;
    private final BookService bookService;

    public EditBookDetailsController(ImageCacheManager imageCacheManager,
                                     AuthorService authorService,
                                     BookService bookService) {
        this.imageCacheManager = imageCacheManager;
        this.authorService = authorService;
        this.bookService = bookService;
    }

    public void initialize(Book book) {
        this.book = book;

        // Set values into TextFields
        titleTextField.setText(book.getTitle());
        pagesCountTextField.setText(String.valueOf(book.getPagesCount()));
        ibsnTextField.setText(book.getIsbn());
        availableCopiesTextField.setText(String.valueOf(book.getBookStatistics().getAvailableCopies()));
        descriptionTextArea.setText(book.getDescription());

        // Prepare combo boxes and set initial values
        List<Author> authorList = authorService.getAllAuthors();
        authorsComboBox.getItems().setAll(authorList);
        categoryComboBox.getItems().setAll(BookCategory.values());
        languageComboBox.getItems().setAll(Language.values());
        authorsComboBox.setValue(book.getAuthor());
        languageComboBox.setValue(book.getLanguage());
        categoryComboBox.setValue(book.getCategory());

        // Set book cover image and url
        imageUrlTextField.setText(book.getImageUrl());
        bookCover.setImage(imageCacheManager.getImage(book.getImageUrl()));
    }

    @FXML
    public void handleSavingBook() {
        String title = titleTextField.getText();
        int pagesCount = Integer.parseInt(pagesCountTextField.getText());
        String ibsn = ibsnTextField.getText();
        int availableCopies = Integer.parseInt(availableCopiesTextField.getText());
        String desc = descriptionTextArea.getText();
        String url = imageUrlTextField.getText();
        Author author = authorsComboBox.getValue();
        BookCategory category = categoryComboBox.getValue();
        Language language = languageComboBox.getValue();

        handleSavingBook(new Book(ibsn, title, List.of(author), category, pagesCount, language, desc, url), availableCopies);
    }

    private void handleSavingBook(Book updatedBook, int updatedAvailableCopies) {
        try {
            // Update all values
            this.book.setTitle(updatedBook.getTitle());
            this.book.setPagesCount(updatedBook.getPagesCount());
            this.book.setIsbn(updatedBook.getIsbn());
            this.book.setDescription(updatedBook.getDescription());
            this.book.setImageUrl(updatedBook.getImageUrl());
            this.book.setAuthors(updatedBook.getAuthors());
            this.book.setCategory(updatedBook.getCategory());
            this.book.setLanguage(updatedBook.getLanguage());
            this.book.getBookStatistics().setAvailableCopies(updatedAvailableCopies);

            // Update the book in the database
            bookService.updateBook(this.book);

            // Close the scene
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
