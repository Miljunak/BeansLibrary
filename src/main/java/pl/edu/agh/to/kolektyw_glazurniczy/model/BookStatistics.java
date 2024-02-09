package pl.edu.agh.to.kolektyw_glazurniczy.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Random;

@Entity
@Data
@NoArgsConstructor
@Table(name = "bookStatistics")
public class BookStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "book_id", unique = true, nullable = false)
    private Book book;

    private LocalDate availableSince;
    private int totalCopies;

    private static final Random random = new Random(2142420);

    private int availableCopies;
    private int popularity;

    private Double averageRating;

    public BookStatistics(Book book, LocalDate availableSince, int totalCopies, int availableCopies, Integer popularity) {
        this.book = book;
        this.availableSince = availableSince;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.popularity = popularity;
        this.averageRating = Math.floor(random.nextDouble(0, 5) * 100) / 100; // TODO implement ratings
    }
}
