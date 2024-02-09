package pl.edu.agh.to.kolektyw_glazurniczy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "reviews")
@IdClass(ReviewId.class)
public class Review {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String title;

    public String getRatingText() {
        return rating + "/5";
    }

    private String reviewText;

    private short rating;

    @Override
    public String toString() {
        return "Review{" + "user=" + user.getName() + ", book=" + book + ", title='" + title + '\'' + ", reviewText='" + reviewText + '\'' + ", rating=" + rating + '}';
    }

    public Review(User user, Book book, String title, String reviewText, short rating) {
        this.user = user;
        this.book = book;
        this.title = title;
        this.reviewText = reviewText;
        this.rating = rating;
    }
}
