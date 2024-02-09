package pl.edu.agh.to.kolektyw_glazurniczy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "borrowings")
public class Borrowing {

    public static final Period BORROWING_DURATION = Period.ofDays(30);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private LocalDate startDate;

    private LocalDate dueDate;

    @Setter
    private LocalDate returnDate;

    public Borrowing(User user, Book book) {
        this(user, book, LocalDate.now(), LocalDate.now().plus(BORROWING_DURATION));
    }

    public Borrowing(User user, Book book, LocalDate startDate, LocalDate dueDate) {
        this.user = user;
        this.book = book;
        this.startDate = startDate;
        this.dueDate = dueDate;
    }
}