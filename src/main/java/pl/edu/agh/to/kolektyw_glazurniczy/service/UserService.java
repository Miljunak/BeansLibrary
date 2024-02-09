package pl.edu.agh.to.kolektyw_glazurniczy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.kolektyw_glazurniczy.model.User;
import pl.edu.agh.to.kolektyw_glazurniczy.model.UserRole;
import pl.edu.agh.to.kolektyw_glazurniczy.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, @Lazy AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User save(User user) {
        logger.debug("Saving user - " + user.toString());
        return userRepository.save(user);
    }

    public Optional<User> findByEmailIgnoreCase(String email) {
        logger.debug("findByEmailIgnoreCase( " + email + ")");
        return userRepository.findByEmailIgnoreCase(email);
    }

    public void suspendUserAccount(User user) {
        if (!(List.of(UserRole.ADMIN, UserRole.WORKER).contains(authService.getCurrentUser().getUserRole()))) {
            logger.warn("Unauthorized action: suspendUserAccount({}).", user.getUsername());
            return;
        }

        logger.info("Suspending user: " + user);
        user.setAccountSuspended(true);
        userRepository.save(user);
    }

    public User changeRole(User user, UserRole role) {
        if(authService.getCurrentUser().getUserRole() != UserRole.ADMIN) {
            logger.warn("Unauthorized action: changeRole({}, {})", user.getUsername(), role);
            return null;
        }

        logger.info("changing role %s on user: %s".formatted(role, user));
        user.setUserRole(role);
        return userRepository.save(user);
    }
}
