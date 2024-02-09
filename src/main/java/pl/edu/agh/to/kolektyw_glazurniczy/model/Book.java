package pl.edu.agh.to.kolektyw_glazurniczy.model;

import jakarta.persistence.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String isbn;

    private String title;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Author> authors;

    @Enumerated(EnumType.STRING)
    private BookCategory category;

    private int pagesCount;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String description;

    private String imageUrl;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private BookStatistics bookStatistics;


    public Book(String title, Author author, BookCategory category,
                int pagesCount, Language language, String isbn,
                String description, String imageUrl) {
        this(isbn, title, List.of(author), category, pagesCount, language, description, imageUrl);
    }

    public Book(String isbn, String title, List<Author> authors, BookCategory category, int pagesCount,
                Language language, String description, String imageUrl) {
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.category = category;
        this.pagesCount = pagesCount;
        this.language = language;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Double getRating() {
        return bookStatistics.getAverageRating();
    }

    public Author getAuthor() {
        return authors.get(0); // Returns only first author because realistically, we only use 1 author always.
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", pagesCount=" + pagesCount +
                ", language=" + language +
                '}';
    }

    public IntegerProperty availableCopiesProperty() {
        return new SimpleIntegerProperty(bookStatistics.getAvailableCopies());
    }
}

