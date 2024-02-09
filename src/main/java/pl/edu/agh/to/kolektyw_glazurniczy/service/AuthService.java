package pl.edu.agh.to.kolektyw_glazurniczy.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.kolektyw_glazurniczy.dto.UserRegistrationDTO;
import pl.edu.agh.to.kolektyw_glazurniczy.exception.LoginException;
import pl.edu.agh.to.kolektyw_glazurniczy.exception.RegistrationValidationException;
import pl.edu.agh.to.kolektyw_glazurniczy.exception.UserAlreadyExistException;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AuthService implements UserDetailsService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    Logger logger = LoggerFactory.getLogger(AuthService.class);
    private User currentUser;


    public AuthService(UserService userService, PasswordEncoder passwordEncoder, Validator validator) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    public User register(UserRegistrationDTO request)
            throws UserAlreadyExistException, RegistrationValidationException {

        Set<ConstraintViolation<UserRegistrationDTO>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new RegistrationValidationException(violations);
        }

        if (emailExists(request.getEmail())) {
            throw new UserAlreadyExistException("User already exist with email:" + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), request.getEmail(), encodedPassword);
        user.setRegistrationDate(LocalDateTime.now());
        user = userService.save(user);
        authenticateUser(user);
        logger.info("user successfully registered in {} ", user);
        return user;
    }

    private void authenticateUser(User user) {
        currentUser = user;
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword(), user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public User login(String email, String password) throws UserAlreadyExistException {
        User user = userService.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new LoginException("Incorrect email or password");
        }

        authenticateUser(user);
        user.setLastLoginDate(LocalDateTime.now());
        user = userService.save(user);
        logger.info("user successfully logged-in in {} ", user);
        return user;
    }

    private boolean emailExists(String email) {
        return userService.findByEmailIgnoreCase(email).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmailIgnoreCase(email).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
