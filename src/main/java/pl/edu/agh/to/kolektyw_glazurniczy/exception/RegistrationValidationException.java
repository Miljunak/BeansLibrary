package pl.edu.agh.to.kolektyw_glazurniczy.exception;

import jakarta.validation.ConstraintViolation;
import org.springframework.security.core.AuthenticationException;
import pl.edu.agh.to.kolektyw_glazurniczy.dto.UserRegistrationDTO;

import java.util.Set;

public class RegistrationValidationException extends AuthenticationException {
    private final Set<ConstraintViolation<UserRegistrationDTO>> errors;

    public RegistrationValidationException(Set<ConstraintViolation<UserRegistrationDTO>> errors) {
        super("Registration validation exception");
        this.errors = errors;
    }

    public Set<ConstraintViolation<UserRegistrationDTO>> getErrors() {
        return errors;
    }
}
