package pl.edu.agh.to.kolektyw_glazurniczy.service;

import org.springframework.stereotype.Service;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Notification;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;

@Service
public class EmailService {

    public void sendEmail(Notification notification, User sender, User receiver) {
        // TODO implement sending emails
    }
}
