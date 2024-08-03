package ru.practicum.shareit.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Set;

@Component
public class ObjectValidator<T> {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public void validateObject(final T object) {
        if (object == null) {
            throw new ValidationException("null");
        }
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            violations.forEach(error -> errors.append(error.getMessage()).append(" "));
            throw new ValidationException(errors.toString());
        }
    }
}
