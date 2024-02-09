package pl.edu.agh.to.kolektyw_glazurniczy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Notification;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findAllByReceiver(User user);

    List<Notification> findAllByReceiverAndOpenedIsFalse(User user);

    List<Notification> findAllBySender(User user);
}