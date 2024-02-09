package pl.edu.agh.to.kolektyw_glazurniczy;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.edu.agh.to.kolektyw_glazurniczy.gui.FXMLLoaderFactory;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;

@Configuration
public class Config implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    public ObjectProperty<User> userObjectProperty() {
        return new SimpleObjectProperty<>();
    }

    @Bean
    public FXMLLoaderFactory fxmlLoaderFactory() {
        return new FXMLLoaderFactory(applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}