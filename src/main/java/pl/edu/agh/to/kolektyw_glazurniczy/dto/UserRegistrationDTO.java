package pl.edu.agh.to.kolektyw_glazurniczy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegistrationDTO {

    @NotNull(message = "Name cannot be empty")
    private final String name;

    @NotNull(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private final String email;

    @Size(min = 8, message = "Password must have at least 8 characters")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain a digit")
    private final String password;
}
