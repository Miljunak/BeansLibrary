package pl.edu.agh.to.kolektyw_glazurniczy;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.LibraryGUIApp;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        Application.launch(LibraryGUIApp.class, args);
    }
}