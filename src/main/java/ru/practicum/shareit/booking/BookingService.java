package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingDtoMapper bookingDtoMapper;

    public BookingDto createBooking(BookingDtoShort bookingDtoShort, Long userRenterId) {
        log.info("Запрос на аренду вещи с id = {}.", bookingDtoShort.getItemId());

        if (!bookingTimeIsCorrect(bookingDtoShort)) {
            throw new ValidationException("Время неверно относительно друг друга.");
        }

        // Получение вещи
        Optional<Item> optItem = itemRepository.findById(bookingDtoShort.getItemId());
        if (optItem.isEmpty()) {
            throw new NotFoundException("Вещь с id = " + bookingDtoShort.getItemId() + " не существует.");
        }
        Item item = optItem.get();

        // Проверка доступа для аренды
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь с id = " + bookingDtoShort.getItemId() + " не доступна для аренды.");
        }

        // Проверка на аренду вещи владельцем
        if (item.getOwner().getId().equals(userRenterId)) {
            throw new NotFoundException("Владелец не может арендовать свою вещь.");
        }

        // Проверка доступа для аренды на определенное время
        if (!bookingRepository.availableAtTime(item, BookingStatus.WAITING,
                bookingDtoShort.getStart(), bookingDtoShort.getEnd())) {
            throw new BadRequestException("Вещь с id = " + bookingDtoShort.getItemId() + " не доступна для аренды в это время.");
        }

        // Получение арендатора
        Optional<User> optRenter = userRepository.findById(userRenterId);
        if (optRenter.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userRenterId + " не существует.");
        }
        User renter = optRenter.get();

        // Получение владельца
        Optional<User> optOwner = userRepository.findById(item.getOwner().getId());
        if (optOwner.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + item.getOwner().getId() + " не существует.");
        }
        User owner = optOwner.get();

        // Формирование аренды
        Booking booking = bookingDtoMapper.toBooking(bookingDtoShort, owner, renter, item);
        booking.setStatus(BookingStatus.WAITING);

        // Сохранение аренды
        Booking bookingSaved = bookingRepository.save(booking);

        // Создание ответа
        BookingDto bookingDto = bookingDtoMapper.toBookingDto(booking);
        log.info("Запрос c id = {} на аренду вещи с id = {} создан.", bookingSaved.getId(), bookingDtoShort.getItemId());
        return bookingDto;
    }

    public BookingDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        log.info("Запрос на подтверждение аренды с id = {}.", bookingId);

        // Получение аренды
        Optional<Booking> optBooking = bookingRepository.findById(bookingId);
        if (optBooking.isEmpty()) {
            throw new BadRequestException("Запроса на аренду с id = " + bookingId + " не существует.");
        }
        Booking booking = optBooking.get();

        // Проверка статуса аренды
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Запрос на аренду с id = " + bookingId + " уже обработан.");
        }

        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }

        // Проверка доступа пользователя
        if (!booking.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не имеет доступа.");
        }

        // Получение арендуемой вещи
        Item bookingItem = booking.getItem();

        // Проверка доступа для сдачи
        if (!bookingItem.getAvailable()) {
            throw new BadRequestException("Вещь с id = " + bookingItem.getId() + " уже сдана в аренду.");
        }

        // Подтверждение или отмена
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        // Сохранение
        itemRepository.save(bookingItem);
        bookingRepository.save(booking);

        // Создание ответа
        BookingDto bookingDto = bookingDtoMapper.toBookingDto(booking);
        log.info("Запрос c id = {} на аренду вещи с id = {} обработан ({}).",
                booking.getId(),
                bookingItem.getId(),
                approved.toString().toUpperCase());
        return bookingDto;
    }

    public BookingDto getBooking(Long bookingId, Long userId) {
        log.info("Запрос на получение аренды с id = {}.", bookingId);

        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }

        // Получение аренды
        Optional<Booking> optBooking = bookingRepository.findById(bookingId);
        if (optBooking.isEmpty()) {
            throw new NotFoundException("Запроса на аренду с id = " + bookingId + " не существует.");
        }
        Booking booking = optBooking.get();

        // Создание ответа
        if (booking.getOwner().getId().equals(userId) || booking.getRenter().getId().equals(userId)) {
            BookingDto bookingDto = bookingDtoMapper.toBookingDto(booking);
            log.info("Запрос на получение аренды c id = {} одобрен.", booking.getId());
            return bookingDto;
        } else {
            throw new NotFoundException("Пользователь с id = " + userId + " не имеет доступа.");
        }
    }

    public List<BookingDto> getBookings(Long userId, BookingState state) {
        log.info("Запрос на получение исходящих запросов на аренду от пользователя с id = {} по фильтру {}.",
                userId, state);

        // Проверка существования пользователя
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }

        // Поиск по фильтру
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByRenterIdOrderByIdDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.getBookingsCurrentForRenter(userId);
                break;
            case PAST:
                bookings = bookingRepository.getBookingsPastForRenter(userId, BookingStatus.APPROVED);
                break;
            case FUTURE:
                bookings = bookingRepository.getBookingsFutureForRenter(userId,
                        BookingStatus.WAITING, BookingStatus.APPROVED);
                break;
            case WAITING:
                bookings = bookingRepository.getBookingsWaitingForRenter(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.getBookingsRejectedForRenter(userId, BookingStatus.REJECTED);
                break;
            default:
                throw new BadRequestException("Неправильный статус.");
        }

        // Создание ответа
        List<BookingDto> bookingDto = bookings.stream().map(bookingDtoMapper::toBookingDto).toList();
        log.info("Запрос для пользователя с id = {} показал {} исходящих заявок на аренду.",
                userId,
                bookingDto.size());
        return bookingDto;
    }

    public List<BookingDto> getBookingsOwner(Long userId, BookingState state) {
        log.info("Запрос на получение входящих запросов на аренду от пользователя с id = {} по фильтру {}.",
                userId, state);

        // Проверка существования пользователя
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }

        // Поиск по фильтру
        List<Booking> bookings;
        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByOwnerIdOrderByIdDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.getBookingsCurrentForOwner(userId);
                break;
            case PAST:
                bookings = bookingRepository.getBookingsPastForOwner(userId, BookingStatus.APPROVED);
                break;
            case FUTURE:
                bookings = bookingRepository.getBookingsFutureForOwner(userId,
                        BookingStatus.WAITING, BookingStatus.APPROVED);
                break;
            case WAITING:
                bookings = bookingRepository.getBookingsWaitingForOwner(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.getBookingsRejectedForOwner(userId, BookingStatus.REJECTED);
                break;
            default:
                throw new BadRequestException("Неправильный статус.");
        }

        // Создание ответа
        List<BookingDto> bookingDto = bookings.stream().map(bookingDtoMapper::toBookingDto).toList();
        log.info("Запрос для пользователя с id = {} показал {} входящих заявок на аренду.",
                userId,
                bookingDto.size());
        return bookingDto;
    }

    public boolean bookingTimeIsCorrect(BookingDtoShort bookingDtoShort) {
        // Проводится валидация времени только относительно друг друга
        return bookingDtoShort.getStart() != null && bookingDtoShort.getEnd() != null &&
                bookingDtoShort.getStart().isBefore(bookingDtoShort.getEnd());
    }
}
