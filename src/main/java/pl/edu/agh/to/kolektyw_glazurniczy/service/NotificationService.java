package pl.edu.agh.to.kolektyw_glazurniczy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.kolektyw_glazurniczy.model.*;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getUnreadNotificationForUser(User user) {
        logger.debug("getUnreadNotificationForUser({})", user.getUsername());
        return notificationRepository.findAllByReceiverAndOpenedIsFalse(user);
    }

    public List<Notification> getNotificationForUser(User user) {
        if(user == null)
            return Collections.emptyList();
        logger.info("getNotificationForUser({})", user.getUsername());
        return notificationRepository.findAllByReceiver(user);
    }

    public Notification sendNotification(Notification notification, User sender, User receiver) {
        logger.info("sendNotification to {}", receiver.getUsername());
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setSendDateTime(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public Notification sendSystemNotification(Notification notification, User receiver) {
        logger.info("sendSystemNotification to {}", receiver.getUsername());
        notification.setSender(null);
        notification.setReceiver(receiver);
        notification.setSendDateTime(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    public void markAsOpen(Notification newValue) {
        notificationRepository.save(newValue);
    }
}
