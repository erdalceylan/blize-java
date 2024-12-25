package com.blize.validator;

import com.blize.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UserExistsValidator implements ConstraintValidator<UserExists, String> {

    @Autowired
    private UserRepository userRepository;
    private UserExists userExists;

    @Override
    public void initialize(UserExists constraintAnnotation) {
        this.userExists = constraintAnnotation;
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String fieldValue, ConstraintValidatorContext context) {
        if (userExists.fieldName().equals("email")) {
            return !userRepository.existsByEmail(fieldValue);
        } else if (userExists.fieldName().equals("username")) {
            return !userRepository.existsByUsername(fieldValue);
        }
        return false;
    }
}