package pl.edu.agh.to.kolektyw_glazurniczy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Review;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.BookReviewsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BookReviewService {
    Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookReviewsRepository bookReviewsRepository;

    public BookReviewService(BookReviewsRepository bookReviewsRepository) {
        this.bookReviewsRepository = bookReviewsRepository;
    }

    public List<Review> getBookReviews(Book book) {
        return bookReviewsRepository.findByBook(book);
    }

    public boolean userCanAddReview(Book book, User user) {
        return bookReviewsRepository.findByUserAndBook(user, book).isEmpty();
    }

    public Optional<Review> getUserBookReview(Book book, User user) {
        List<Review> byUserAndBook = bookReviewsRepository.findByUserAndBook(user, book);
        if (byUserAndBook.isEmpty()) return Optional.empty();
        return Optional.ofNullable(byUserAndBook.get(0));
    }

    public void addReview(Review review) {
        bookReviewsRepository.save(review);
    }
}
