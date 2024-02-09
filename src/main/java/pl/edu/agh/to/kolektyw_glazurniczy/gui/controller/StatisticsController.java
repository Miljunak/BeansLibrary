package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.TableStatistic;
import pl.edu.agh.to.kolektyw_glazurniczy.service.BookService;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StatisticsController {

    @FXML
    private TableView<TableStatistic> authorsTable;
    @FXML
    private TableColumn<TableStatistic, String> authorNameColumn;
    @FXML
    private TableColumn<TableStatistic, Integer> booksAmountColumn;
    @FXML
    private TableColumn<TableStatistic, Integer> booksBorrowedColumn;
    @FXML
    private TableColumn<TableStatistic, Double> popularityColumn;
    @FXML
    private TableColumn<TableStatistic, Double> averageRatingColumn;
    @FXML
    private TableView<TableStatistic> categoryTable;
    @FXML
    private TableColumn<TableStatistic, String> categoryNameColumn;
    @FXML
    private TableColumn<TableStatistic, Integer> categoryBooksAmountColumn;
    @FXML
    private TableColumn<TableStatistic, Integer> categoryBooksBorrowedColumn;
    @FXML
    private TableColumn<TableStatistic, Double> categoryPopularityColumn;
    @FXML
    private TableColumn<TableStatistic, Double> categoryAverageRatingColumn;

    private final BookService bookService;

    public StatisticsController(BookService bookService) {
        this.bookService = bookService;
    }

    @FXML
    public void initialize() {
        List<Book> bookList = bookService.getAllBooks();
        initializeAuthorStatistics(bookList);
        initializeCategoryStatistics(bookList);
    }

    private void initializeCategoryStatistics(List<Book> bookList) {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");

        Map<String, TableStatistic> categoryStatisticsMap = new HashMap<>();

        for (Book book : bookList) {
            String categoryName = book.getCategory().name();
            TableStatistic categoryStatistic = categoryStatisticsMap.getOrDefault(
                    categoryName, new TableStatistic(categoryName, 0, 0, 0, 0, 0)
            );

            int availableCopies = book.getBookStatistics().getAvailableCopies();
            int totalCopies = book.getBookStatistics().getTotalCopies();
            double rating = book.getRating();

            // Update the TableStatistic for the current author
            categoryStatistic.setBooksAmount(categoryStatistic.getBooksAmount() + totalCopies);
            categoryStatistic.setBooksBorrowed(categoryStatistic.getBooksAmount() - categoryStatistic.getBooksBorrowed() - availableCopies);

            double newPopularity = (double) categoryStatistic.getBooksBorrowed() / categoryStatistic.getBooksAmount();
            String popularityString = decimalFormat.format(newPopularity).replace(',', '.');
            categoryStatistic.setPopularity(Double.parseDouble(popularityString));

            double newAverageRating = ((categoryStatistic.getAverageRating() * categoryStatistic.getDifferentBooksAmount()) + rating) / (categoryStatistic.getDifferentBooksAmount() + 1);
            String averageRatingString = decimalFormat.format(newAverageRating).replace(',', '.');
            categoryStatistic.setAverageRating(Double.parseDouble(averageRatingString));

            categoryStatistic.setDifferentBooksAmount(categoryStatistic.getDifferentBooksAmount() + 1);

            categoryStatisticsMap.put(categoryName, categoryStatistic);
        }

        ObservableList<TableStatistic> categoryStatisticsList = FXCollections.observableArrayList(categoryStatisticsMap.values());

        categoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryBooksAmountColumn.setCellValueFactory(new PropertyValueFactory<>("booksAmount"));
        categoryBooksBorrowedColumn.setCellValueFactory(new PropertyValueFactory<>("booksBorrowed"));
        categoryPopularityColumn.setCellValueFactory(new PropertyValueFactory<>("popularity"));
        categoryAverageRatingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        categoryTable.setItems(categoryStatisticsList);
    }

    private void initializeAuthorStatistics(List<Book> bookList) {

        DecimalFormat decimalFormat = new DecimalFormat("#.###");

        Map<String, TableStatistic> authorStatisticsMap = new HashMap<>();

        for (Book book : bookList) {
            String authorName = book.getAuthor().getName();
            TableStatistic authorStatistic = authorStatisticsMap.getOrDefault(
                    authorName, new TableStatistic(authorName, 0, 0, 0, 0, 0)
            );

            int availableCopies = book.getBookStatistics().getAvailableCopies();
            int totalCopies = book.getBookStatistics().getTotalCopies();
            double rating = book.getRating();

            // Update the TableStatistic for the current category
            authorStatistic.setBooksAmount(authorStatistic.getBooksAmount() + totalCopies);
            authorStatistic.setBooksBorrowed(authorStatistic.getBooksAmount() - authorStatistic.getBooksBorrowed() - availableCopies);
            double newPopularity = (double) authorStatistic.getBooksBorrowed() / authorStatistic.getBooksAmount();
            String popularityString = decimalFormat.format(newPopularity).replace(',', '.');
            authorStatistic.setPopularity(Double.parseDouble(popularityString));

            double newAverageRating = ((authorStatistic.getAverageRating() * authorStatistic.getDifferentBooksAmount()) + rating) / (authorStatistic.getDifferentBooksAmount() + 1);
            String averageRatingString = decimalFormat.format(newAverageRating).replace(',', '.');
            authorStatistic.setAverageRating(Double.parseDouble(averageRatingString));

            authorStatistic.setDifferentBooksAmount(authorStatistic.getDifferentBooksAmount() + 1);

            authorStatisticsMap.put(authorName, authorStatistic);
        }

        ObservableList<TableStatistic> authorStatisticsList = FXCollections.observableArrayList(authorStatisticsMap.values());

        authorNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        booksAmountColumn.setCellValueFactory(new PropertyValueFactory<>("booksAmount"));
        booksBorrowedColumn.setCellValueFactory(new PropertyValueFactory<>("booksBorrowed"));
        popularityColumn.setCellValueFactory(new PropertyValueFactory<>("popularity"));
        averageRatingColumn.setCellValueFactory(new PropertyValueFactory<>("averageRating"));

        authorsTable.setItems(authorStatisticsList);
    }
}
