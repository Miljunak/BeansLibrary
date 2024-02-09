package pl.edu.agh.to.kolektyw_glazurniczy.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String message;

    private boolean opened;

    @Enumerated(EnumType.STRING)
    private NotificationDestination notificationDestination;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(nullable = false)
    LocalDateTime sendDateTime;

    @ManyToOne()
    private User sender;

    @ManyToOne(optional = false)
    private User receiver;

    public Notification(String title, String message, NotificationDestination destination, NotificationType type) {
        this.title = title;
        this.message = message;
        this.notificationDestination = destination;
        this.notificationType = type;
        this.opened = false;
    }

    public enum NotificationDestination {
        EMAIL, IN_APP
    }

    public enum NotificationType {
        BOOK_OVERDUE,
        NEW_BOOK,
        EVENT,
        PROMOTION,
        IMPORTANT

    }
}
