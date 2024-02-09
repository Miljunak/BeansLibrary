package pl.edu.agh.to.kolektyw_glazurniczy.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferences {

    private boolean darkTheme;
    private boolean receiveEmails;
}
