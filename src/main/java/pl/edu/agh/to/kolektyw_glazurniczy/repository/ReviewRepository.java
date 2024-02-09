package pl.edu.agh.to.kolektyw_glazurniczy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

}
