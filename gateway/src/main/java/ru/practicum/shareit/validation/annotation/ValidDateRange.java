package ru.practicum.shareit.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "Неверное значение дат.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

