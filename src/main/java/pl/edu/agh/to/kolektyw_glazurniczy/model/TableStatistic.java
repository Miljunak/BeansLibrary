package pl.edu.agh.to.kolektyw_glazurniczy.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableStatistic {

    private String name;
    private int booksAmount;
    private int booksBorrowed;
    private double popularity;
    private double averageRating;
    private int differentBooksAmount;

}
