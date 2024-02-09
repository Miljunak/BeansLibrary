package pl.edu.agh.to.kolektyw_glazurniczy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {

    NONE("NONE"),
    POLISH("PL"),
    ENGLISH("EN"),
    FRENCH("FR"),
    SPANISH("SP"),
    RUSSIAN("RUS");

    private final String shortForm;
}
