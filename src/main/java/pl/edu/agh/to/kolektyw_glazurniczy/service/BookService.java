package pl.edu.agh.to.kolektyw_glazurniczy.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.BookCategory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.BookStatistics;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.AuthorRepository;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.BookRepository;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.BookStatisticsRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    private final BookRepository bookRepository;

    private final BookStatisticsRepository bookStatisticsRepository;

    private final AuthorRepository authorRepository;

    Logger logger = LoggerFactory.getLogger(BookService.class);


    public BookService(BookRepository bookRepository,
                       BookStatisticsRepository bookStatisticsRepository,
                       AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.bookStatisticsRepository = bookStatisticsRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional
    public Book save(Book book) {
        logger.info("saving a new book: {}", book);
        try {
            authorRepository.saveAll(book.getAuthors());
            return bookRepository.save(book);
        } catch (DataAccessException exception) {
            logger.error("failed to save book {} - {}", book, exception.getMessage());
            throw exception;
        }
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getRecentBooks() {
        logger.debug("getRecentBooks()");
        List<BookStatistics> bookStatistics = bookStatisticsRepository.findTop5ByOrderByAvailableSinceAsc();

        return bookStatistics.stream()
                .map(BookStatistics::getBook)
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByName(String name) {
        logger.debug("getBooksByName({}) called", name);

        return bookRepository.findByTitleContainingIgnoreCase(name);
    }

    @Transactional
    public void updateBook(Book updatedBook) {
        logger.info("updating book: {}", updatedBook);
        bookRepository.save(updatedBook);
    }

    public List<Book> getBooksByCategory(BookCategory category) {
        logger.debug("getBooksByCategory({}) called", category.name());

        List<Book> allBooks = bookRepository.findAll();

        return allBooks.stream()
                .filter(book -> book.getCategory() == category)
                .collect(Collectors.toList());
    }

}
