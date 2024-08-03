package ru.practicum.shareit.booking.model.converter;

import jakarta.persistence.AttributeConverter;
import ru.practicum.shareit.booking.model.BookingStatus;

public class BookingStatusConverter implements AttributeConverter<BookingStatus, Long> {

    @Override
    public Long convertToDatabaseColumn(BookingStatus status) {
        if (status == null) {
            return null;
        }
        return status.getId();
    }

    @Override
    public BookingStatus convertToEntityAttribute(Long id) {
        if (id == null) {
            return null;
        }
        return BookingStatus.fromId(id);
    }
}