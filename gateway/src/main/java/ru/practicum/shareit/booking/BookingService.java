package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.type.TypeReference;
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
import ru.practicum.shareit.client.ResponseHandler;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingClient bookingClient;
    private final BookingDtoMapper bookingDtoMapper;
    private final ResponseHandler responseHandler;

    public BookingDto createBooking(BookingDtoShort bookingDtoShort, Long userRenterId) {
        log.info("GATEWAY: получен запрос на аренду вещи с id = {}.", bookingDtoShort.getItemId());
        BookItemRequestDto bookItemRequestDto = bookingDtoMapper.toBookItemRequestDto(bookingDtoShort);
        ResponseEntity<Object> response = bookingClient.bookItem(userRenterId, bookItemRequestDto);
        BookingDto bookingDto = responseHandler.handleResponse(response, new TypeReference<BookingDto>() {});
        log.info("GATEWAY: обработан запрос на аренду вещи с id = {}.", bookingDtoShort.getItemId());
        return bookingDto;
    }

    public BookingDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        log.info("GATEWAY: получен запрос на подтверждение аренды с id = {}.", bookingId);
        ResponseEntity<Object> response = bookingClient.handleBooking(bookingId, userId, approved);
        BookingDto bookingDto = responseHandler.handleResponse(response, new TypeReference<BookingDto>() {});
        log.info("GATEWAY: обработан запрос на подтверждение аренды с id = {}.", bookingId);
        return bookingDto;
    }

    public BookingDto getBooking(Long bookingId, Long userId) {
        log.info("GATEWAY: получен запрос на получение аренды с id = {}.", bookingId);
        ResponseEntity<Object> response = bookingClient.getBooking(userId, bookingId);
        BookingDto bookingDto = responseHandler.handleResponse(response, new TypeReference<BookingDto>() {});
        log.info("GATEWAY: обработан запрос на получение аренды c id = {}.", bookingId);
        return bookingDto;
    }

    public List<BookingDto> getBookings(Long userId, BookingState state) {
        log.info("GATEWAY: получен запрос на получение всех запросов на аренду.");
        ResponseEntity<Object> response = bookingClient.getBookings(userId, state);
        List<BookingDto> bookingDtos = responseHandler.handleResponse(response, new TypeReference<List<BookingDto>>(){});
        log.info("GATEWAY: обработан запрос на получение всех запросов на аренду.");
        return bookingDtos;
    }

    public List<BookingDto> getBookingsOwner(Long userId, BookingState state) {
        log.info("GATEWAY: получен запрос на получение исходящих запросов на аренду от пользователя.");
        ResponseEntity<Object> response = bookingClient.getBookingsOwner(userId, state);
        List<BookingDto> bookingDtos = responseHandler.handleResponse(response, new TypeReference<List<BookingDto>>(){});
        log.info("GATEWAY: обработан запрос на получение исходящих запросов на аренду пользователя.");
        return bookingDtos;
    }
}
