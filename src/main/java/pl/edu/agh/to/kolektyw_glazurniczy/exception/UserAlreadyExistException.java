package pl.edu.agh.to.kolektyw_glazurniczy.exception;

import org.springframework.security.core.AuthenticationException;

public class UserAlreadyExistException extends AuthenticationException {

    public UserAlreadyExistException(String message) {
        super(message);
    }
}
