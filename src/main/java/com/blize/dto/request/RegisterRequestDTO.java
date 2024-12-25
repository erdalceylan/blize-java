package com.blize.dto.request;
import com.blize.validator.UserExists;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {


    @Pattern(regexp = "^[\\p{L}]+$", message = "must contain only letters")
    @NotBlank(message = "{register.required.firstname}")
    @Size(min = 2, max = 64, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Pattern(regexp = "^[\\p{L}]+$", message = "must contain only letters")
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 64, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @UserExists(fieldName = "username", message = "{register.username_already_exists}")
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 180, message = "Username must be between 4 and 30 characters")
    private String username;

    @UserExists(fieldName = "email", message = "{register.email_already_exists}")
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
