package pl.edu.agh.to.kolektyw_glazurniczy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Book;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Review;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;

import java.util.List;

@Repository
public interface BookReviewsRepository extends JpaRepository<Review, Integer> {
    List<Review> findByBook(Book book);

    List<Review> findByUserAndBook(User user, Book book);
}