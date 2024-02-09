package pl.edu.agh.to.kolektyw_glazurniczy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    static Map<UserRole, Integer> MAX_BORROWED_AT_TIME = new EnumMap<>(
            Map.of(UserRole.CUSTOMER, 3,
                    UserRole.WORKER, 10,
                    UserRole.ADMIN, 10000));


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Embedded
    private UserPreferences preferences;

    @Setter
    private LocalDateTime lastLoginDate;

    @Setter
    @Column(nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Setter
    private boolean isAccountSuspended;

    @OneToMany
    List<Notification> notificationList;

    public User(String name, String email, String password) {
        this(name, email, password, UserRole.CUSTOMER);
    }

    public User(String name, String email, String password, UserRole userRole) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.preferences = new UserPreferences(false, true);
        this.isAccountSuspended = false;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", userType=" + userRole +
                '}';
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}