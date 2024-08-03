package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingClient bookingClient;
    private final BookingDtoMapper bookingDtoMapper;

    public BookingDto createBooking(BookingDtoShort bookingDtoShort, Long userRenterId) {
        log.info("GATEWAY: получен запрос на аренду вещи с id = {}.", bookingDtoShort.getItemId());
        BookItemRequestDto bookItemRequestDto = bookingDtoMapper.toBookItemRequestDto(bookingDtoShort);
        ResponseEntity<Object> response = bookingClient.bookItem(userRenterId, bookItemRequestDto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            BookingDto bookingDto = (BookingDto) response.getBody();
            log.info("GATEWAY: обработан запрос на аренду вещи с id = {}.", bookingDtoShort.getItemId());
            return bookingDto;
        }
        return null; // !!!!!
    }

    public BookingDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        log.info("GATEWAY: получен запрос на подтверждение аренды с id = {}.", bookingId);
        ResponseEntity<Object> response = bookingClient.handleBooking(bookingId, userId, approved);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            BookingDto bookingDto = (BookingDto) response.getBody();
            log.info("GATEWAY: обработан запрос на подтверждение аренды с id = {}.", bookingId);
            return bookingDto;
        }
        return null; // !!!!!
    }

    public BookingDto getBooking(Long bookingId, Long userId) {
        log.info("GATEWAY: получен запрос на получение аренды с id = {}.", bookingId);
        ResponseEntity<Object> response = bookingClient.getBooking(userId, bookingId);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            BookingDto bookingDto = (BookingDto) response.getBody();
            log.info("GATEWAY: обработан запрос на получение аренды c id = {}.", bookingId);
            return bookingDto;
        }
        return null; // !!!!!
    }

    public List<BookingDto> getBookings(Long userId, BookingState state) {
        log.info("GATEWAY: получен запрос на получение исходящих запросов на аренду (1).");
        ResponseEntity<Object> response = bookingClient.getBookings(userId, state);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<BookingDto> bookingDtos = (List<BookingDto>) response.getBody();
            log.info("GATEWAY: обработан запрос на получение исходящих запросов на аренду (1).");
            return bookingDtos;
        }
        return null; // !!!!!
    }

    public List<BookingDto> getBookingsOwner(Long userId, BookingState state) {
        log.info("GATEWAY: получен запрос на получение исходящих запросов на аренду (2).");
        ResponseEntity<Object> response = bookingClient.getBookingsOwner(userId, state);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<BookingDto> bookingDtos = (List<BookingDto>) response.getBody();
            log.info("GATEWAY: обработан запрос на получение исходящих запросов на аренду (2).");
            return bookingDtos;
        }
        return null; // !!!!!
    }
}
