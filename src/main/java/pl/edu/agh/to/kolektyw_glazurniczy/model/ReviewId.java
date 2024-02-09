package pl.edu.agh.to.kolektyw_glazurniczy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewId implements Serializable {

    private Integer user;
    private Integer book;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ReviewId reviewId = (ReviewId) o;
        return Objects.equals(user, reviewId.user) &&
                Objects.equals(book, reviewId.book);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, book);
    }
}
