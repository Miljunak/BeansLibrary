package pl.edu.agh.to.kolektyw_glazurniczy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Borrowing;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;

import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Integer> {
    List<Borrowing> findByUser(User user);
}
