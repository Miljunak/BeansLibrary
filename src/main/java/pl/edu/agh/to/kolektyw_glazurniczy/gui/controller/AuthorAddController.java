package pl.edu.agh.to.kolektyw_glazurniczy.gui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import pl.edu.agh.to.kolektyw_glazurniczy.model.Author;
import pl.edu.agh.to.kolektyw_glazurniczy.service.AuthorService;

import java.time.LocalDate;
import java.time.Period;

@Controller
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AuthorAddController {

    private final AuthorService authorService;
    public Button cancelButton;
    @FXML
    public TextField authorNameTextField;
    @FXML
    private DatePicker birthdayDatePicker;
    @FXML
    private Button addButton;
    private boolean authorAdded = false;

    public AuthorAddController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @FXML
    public void initialize() {
        LocalDate maxDate = LocalDate.now();
        birthdayDatePicker.setDayCellFactory(d -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(item.isAfter(maxDate));
            }
        });
        birthdayDatePicker.setValue(LocalDate.now().minus(Period.ofYears(20)));
    }


    @FXML
    public void addAuthor() {
        String authorName = authorNameTextField.getText();
        LocalDate birthday = birthdayDatePicker.getValue();
        addButton.getScene().setCursor(Cursor.WAIT);
        try {
            authorService.saveAuthor(new Author(authorName, birthday));
            authorAdded = true;
        } catch (DataIntegrityViolationException exception) {
            authorAdded = false;
        }
        addButton.getScene().setCursor(Cursor.DEFAULT);
        closeModal();
    }

    public boolean isAuthorAdded() {
        return authorAdded;
    }

    public void setAuthorAdded(boolean authorAdded) {
        this.authorAdded = authorAdded;
    }

    public void cancelAdd(ActionEvent actionEvent) {
        closeModal();
    }


    private void closeModal() {
        addButton.getScene().getWindow().hide();
    }
}
