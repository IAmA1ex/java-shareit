package ru.practicum.shareit.booking.model.converter;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.BookingStatus;

import static org.junit.jupiter.api.Assertions.*;

class BookingStatusConverterTest {

    private final BookingStatusConverter converter = new BookingStatusConverter();

    @Test
    void convertToDatabaseColumn() {
        BookingStatus bookingStatusWaiting = BookingStatus.WAITING;
        Long idWaiting = 1L;
        BookingStatus bookingStatusApproved = BookingStatus.APPROVED;
        Long idApproved = 2L;
        BookingStatus bookingStatusRejected = BookingStatus.REJECTED;
        Long idRejected = 3L;
        assertEquals(idWaiting, converter.convertToDatabaseColumn(bookingStatusWaiting));
        assertEquals(idApproved, converter.convertToDatabaseColumn(bookingStatusApproved));
        assertEquals(idRejected, converter.convertToDatabaseColumn(bookingStatusRejected));
        assertNull(converter.convertToDatabaseColumn(null));

    }

    @Test
    void convertToEntityAttribute() {
        BookingStatus bookingStatusWaiting = BookingStatus.WAITING;
        Long idWaiting = 1L;
        BookingStatus bookingStatusApproved = BookingStatus.APPROVED;
        Long idApproved = 2L;
        BookingStatus bookingStatusRejected = BookingStatus.REJECTED;
        Long idRejected = 3L;
        assertEquals(bookingStatusWaiting, converter.convertToEntityAttribute(idWaiting));
        assertEquals(bookingStatusApproved, converter.convertToEntityAttribute(idApproved));
        assertEquals(bookingStatusRejected, converter.convertToEntityAttribute(idRejected));
        assertNull(converter.convertToEntityAttribute(null));
    }
}