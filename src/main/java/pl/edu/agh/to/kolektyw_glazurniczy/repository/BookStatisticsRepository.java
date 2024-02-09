package pl.edu.agh.to.kolektyw_glazurniczy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.kolektyw_glazurniczy.model.BookStatistics;

import java.util.List;

@Repository
public interface BookStatisticsRepository extends JpaRepository<BookStatistics, Integer> {
    List<BookStatistics> findTop5ByOrderByAvailableSinceAsc();
}
