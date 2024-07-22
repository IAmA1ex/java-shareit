package ru.practicum.shareit.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Booking> {

    @Override
    public boolean isValid(Booking booking, ConstraintValidatorContext context) {
        if (booking == null) {
            return true;
        }
        LocalDateTime startDate = booking.getStartTime();
        LocalDateTime endDate = booking.getEndTime();
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }
}
