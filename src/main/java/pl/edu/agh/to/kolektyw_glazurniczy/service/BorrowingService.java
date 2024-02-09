package pl.edu.agh.to.kolektyw_glazurniczy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.BookStatistics;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Borrowing;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.BookStatisticsRepository;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.BorrowingRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowingService {
    private final BookStatisticsRepository bookStatisticsRepository;
    private final BorrowingRepository borrowingRepository;

    Logger logger = LoggerFactory.getLogger(BorrowingService.class);



    public BorrowingService(BookStatisticsRepository bookStatisticsRepository, BorrowingRepository borrowingRepository) {
        this.bookStatisticsRepository = bookStatisticsRepository;
        this.borrowingRepository = borrowingRepository;
    }


    @Transactional
    public Borrowing borrowBook(Book book, User user) {
        if (user == null) {
            System.err.println("Null user, cannot borrow a book");
            return null;
        }
        if (book == null) {
            System.err.println("Null book, cannot borrow");
            return null;
        }
        if (book.getBookStatistics().getAvailableCopies() == 0) {
            System.err.println("No available copies, cannot borrow");
            return null;
        }
        BookStatistics statistics = book.getBookStatistics();
        statistics.setAvailableCopies(statistics.getAvailableCopies() - 1);
        bookStatisticsRepository.save(statistics);
        Borrowing borrowing = new Borrowing(user, book);
        logger.info("\"{}\" is being borrowed by {}", book.getTitle(), user.getName());
        return borrowingRepository.save(borrowing);

    }

    @Transactional
    public void returnBook(Borrowing borrowing) {
        borrowing.setReturnDate(LocalDate.now());
        BookStatistics bookStatistics = borrowing.getBook().getBookStatistics();
        bookStatistics.setAvailableCopies(bookStatistics.getAvailableCopies() + 1);
        bookStatisticsRepository.save(bookStatistics);
        logger.info("{} is returned by {}", borrowing.getBook().getTitle(), borrowing.getUser().getName());
        borrowingRepository.save(borrowing);
    }

    public List<Borrowing> getBorrowingsForUser(User user) {
        logger.debug("getBorrowingsForUser({})", user.getName());
        return borrowingRepository.findByUser(user);
    }

    public List<Borrowing> getAllBorrowings() {
        logger.debug("getAllBorrowings()");
        return borrowingRepository.findAll();
    }
}
