package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.ImageCacheManager;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.BookCategory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Borrowing;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BookService;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BorrowingService;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;

@Controller
public class HomepageController {

    @FXML
    private TextField searchTextField;
    @FXML
    private Button searchButton;
    @FXML
    private FlowPane listOfNewBooks;
    @FXML
    private FlowPane listOfRecommendedBooks;
    @FXML
    private Separator booksSeparator;
    @FXML
    private Label recommendedLabel;

    private final ApplicationStateManager applicationStateManager;
    private final ImageCacheManager imageCacheManager;
    private final FXMLLoaderFactory fxmlLoaderFactory;
    private final BookService bookService;
    private final BorrowingService borrowingService;

    @Autowired
    public HomepageController(BookService bookService,
                              ApplicationStateManager applicationStateManager,
                              ImageCacheManager imageCacheManager,
                              BorrowingService borrowingService,
                              FXMLLoaderFactory fxmlLoaderFactory) {
        this.bookService = bookService;
        this.applicationStateManager = applicationStateManager;
        this.imageCacheManager = imageCacheManager;
        this.fxmlLoaderFactory = fxmlLoaderFactory;
        this.borrowingService = borrowingService;
    }

    @FXML
    public void initialize() {
        initializeDefaults(); // Has to be different function to avoid too many re-initializing recommendations

        List<Book> recommendedBooks = getRecommendedBooks();
        displayBooks(recommendedBooks, listOfRecommendedBooks);
    }

    public void initializeDefaults() {
        List<Book> recentBooks = this.bookService.getRecentBooks();
        displayBooks(recentBooks, listOfNewBooks);

        searchButton.setOnAction(event -> {
            String searchQuery = searchTextField.getText();

            FXMLLoader loader = applicationStateManager.loadView("bookSearch.fxml");

            if (loader.getController() instanceof BookSearchController bookSearchController) {
                bookSearchController.displaySearchResults(searchQuery);
            } else {
                System.err.println("controller supposed to be BookSearchController");
            }
        });

        applicationStateManager.userProperty().addListener(change -> getRecommendedBooks());
    }

    private void displayBooks(List<Book> bookList, Pane location) {
        location.getChildren().clear();
        for (Book book : bookList) {
            try {
                FXMLLoader loader = fxmlLoaderFactory.getFXMLLoader();
                loader.setLocation(getClass().getClassLoader().getResource("view/bookPreview.fxml"));

                AnchorPane bookPreview = loader.load();

                Label label = (Label) bookPreview.lookup("#titleLabel");
                label.setText(book.getTitle());

                label.setOnMouseClicked(event -> {
                    FXMLLoader detailsLoader = fxmlLoaderFactory.getFXMLLoader();
                    detailsLoader.setLocation(getClass().getClassLoader().getResource("view/bookDetails.fxml"));
                    Parent parent;

                    try {
                        parent = detailsLoader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    Stage stage = new Stage();
                    stage.setScene(new Scene(parent));
                    stage.setTitle(book.getTitle());

                    BookDetailsController BDController = detailsLoader.getController();
                    BDController.initialize(book);

                    stage.showAndWait();

                    this.initializeDefaults();
                });
                label.setCursor(Cursor.HAND);

                ImageView bookCover = (ImageView) bookPreview.lookup("#bookCover");
                bookCover.setImage(imageCacheManager.getImage(book.getImageUrl()));

                location.getChildren().add(bookPreview);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Book> getRecommendedBooks() {
        List<Book> recommendedBooks = new ArrayList<>();
        if (applicationStateManager.getUser() != null) {
            User user = applicationStateManager.getUser();

            booksSeparator.setVisible(true);
            recommendedLabel.setVisible(true);
            listOfRecommendedBooks.setVisible(true);

            List<Borrowing> userBorrowings = borrowingService.getBorrowingsForUser(user);
            List<Book> sortedBooksList = userBorrowings.stream()
                    .sorted(Comparator.comparing(Borrowing::getStartDate).reversed())
                    .map(Borrowing::getBook).toList();

            List<BookCategory> recentCategories = new ArrayList<>();
            for (int i = 0; i < min(sortedBooksList.size(), 3); i++) { // If available gets 3 (or less) latest categories
                recentCategories.add(sortedBooksList.get(i).getCategory());
            }

            Random random = new Random();

            for (int i = 0; i < 3; i++) {
                if (!recentCategories.isEmpty()) {
                    BookCategory randomCategory = recentCategories.get(random.nextInt(recentCategories.size()));
                    List<Book> booksInCategory = bookService.getBooksByCategory(randomCategory);
                    removeCommonBooks(sortedBooksList, booksInCategory);

                    if (!booksInCategory.isEmpty() && recommendedBooks.size() < booksInCategory.size()) {
                        Book randomBook = booksInCategory.get(random.nextInt(booksInCategory.size()));
                        while (containsBook(recommendedBooks, randomBook)) {
                            randomBook = booksInCategory.get(random.nextInt(booksInCategory.size()));
                        }  // Yes in THEORY this can be infinite loop but come on, it is practically impossible

                        recommendedBooks.add(randomBook);

                    } else {
                        recommendRandomBooks(recommendedBooks);
                    }

                } else {
                    recommendRandomBooks(recommendedBooks);
                }
            }

        } else {
            booksSeparator.setVisible(false);
            recommendedLabel.setVisible(false);
            listOfRecommendedBooks.setVisible(false);
        }
        return recommendedBooks;
    }

    private boolean containsBook(List<Book> bookList, Book book) {
        int bookId = book.getId();
        for (Book bookItem : bookList) {
            if (bookItem.getId() == bookId) {
                return true;
            }
        }
        return false;
    }

    private void removeCommonBooks(List<Book> list1, List<Book> list2) {
        for (Book book1 : list1) {
            for (Book book2 : list2) {
                if (Objects.equals(book1.getId(), book2.getId())) {
                    list2.remove(book2);
                    break;
                }
            }
        }
    }

    private void recommendRandomBooks(List<Book> recommendedBooks) {
        Random random = new Random();
        List<Book> allBooks = bookService.getAllBooks();
        if (!allBooks.isEmpty()) {
            Book randomBook = allBooks.get(random.nextInt(allBooks.size()));
            while (containsBook(recommendedBooks, randomBook)) {
                randomBook = allBooks.get(random.nextInt(allBooks.size()));
            }

            recommendedBooks.add(randomBook);
        }
    }
}