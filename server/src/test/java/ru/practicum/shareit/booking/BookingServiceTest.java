package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import ru.practicum.shareit.item.dto.mapper.MapperItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceTest {

    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private BookingDtoMapper bookingDtoMapper;
    private BookingService bookingService;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;

    private boolean isAvailable;
    private boolean isAvailableAtTime;
    private BookingStatus bookingStatus;
    private Long itemId;
    private Long itemOwnerId;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingDtoMapper = new BookingDtoMapper(new MapperItemDto());
        bookingService = new BookingService(bookingRepository, itemRepository, userRepository, bookingDtoMapper);

        usedIds = new ArrayList<>();
        isAvailable = true;
        isAvailableAtTime = true;
        bookingStatus = BookingStatus.WAITING;
        itemId = getRandomKey();
        itemOwnerId = getRandomKey();

        when(itemRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxId) return Optional.of(getItem(id, id));
            return Optional.empty();
        });

        when(userRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxId) return Optional.of(getUser(id));
            return Optional.empty();
        });

        when(userRepository.existsById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            return id > 0 && id <= maxId;
        });

        when(bookingRepository.availableAtTime(any(Item.class), any(BookingStatus.class),
                any(LocalDateTime.class), any(LocalDateTime.class))).thenAnswer(arguments -> {
                    return isAvailableAtTime;
        });

        when(bookingRepository.save(any(Booking.class))).thenAnswer(arguments -> {
            Booking booking = arguments.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        when(bookingRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxId) return Optional.of(getBooking(id));
            return Optional.empty();
        });
    }

    @Test
    void createBooking() {

        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        Long realItemId = getRandomKey();
        Long fakeItemId = getFakeKey();

        // Проверка корректного времени
        BookingDtoShort bookingDtoTime = getBookingDtoShort(realItemId, 2, 3);  // неправильное время
        ValidationException exceptionTime = assertThrows(ValidationException.class, () ->
                bookingService.createBooking(bookingDtoTime, realUserId));
        assertEquals("Время неверно относительно друг друга.", exceptionTime.getMessage());

        // Проверка вещи
        BookingDtoShort bookingDtoItem = getBookingDtoShort(fakeItemId, 4, 3);
        NotFoundException exceptionItem = assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(bookingDtoItem, realUserId));
        assertEquals("Вещь с id = " + fakeItemId + " не существует.", exceptionItem.getMessage());

        BookingDtoShort normalBookingDto = getBookingDtoShort(realItemId, 5, 3);

        // Проверка доступа
        isAvailable = false;
        BadRequestException exceptionAvailable = assertThrows(BadRequestException.class, () ->
                bookingService.createBooking(normalBookingDto, realUserId));
        assertEquals("Вещь с id = " + realItemId + " не доступна для аренды.", exceptionAvailable.getMessage());
        isAvailable = true;

        // Проверка на аренду вещи владельцем
        Long ownerUserId = realItemId;
        NotFoundException exceptionOwner = assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(normalBookingDto, ownerUserId));
        assertEquals("Владелец не может арендовать свою вещь.", exceptionOwner.getMessage());

        // Проверка доступа для аренды на определенное время
        isAvailableAtTime = false;
        BadRequestException exceptionAvailableAtTime = assertThrows(BadRequestException.class, () ->
                bookingService.createBooking(normalBookingDto, realUserId));
        assertEquals("Вещь с id = " + realItemId + " не доступна для аренды в это время.",
                exceptionAvailableAtTime.getMessage());
        isAvailableAtTime = true;

        // Проверка арендатора
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(normalBookingDto, fakeUserId));
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка создания
        BookingDto bookingDto = bookingService.createBooking(normalBookingDto, realUserId);
        assertEquals(realItemId, bookingDto.getItem().getId());
        assertEquals(normalBookingDto.getItemId(), bookingDto.getItem().getId());
        assertEquals(normalBookingDto.getStart(), bookingDto.getStart());
        assertEquals(normalBookingDto.getEnd(), bookingDto.getEnd());
        assertEquals(BookingStatus.WAITING, bookingDto.getStatus());
        assertEquals(realUserId, bookingDto.getBooker().getId());

    }

    @Test
    void approveBooking() {
        Long realUserId = itemOwnerId;
        Long realUserNotOwnerId = getRandomKey();
        Long fakeUserId = getFakeKey();
        Long realBookingId = getRandomKey();
        Long fakeBookingId = getFakeKey();
        Boolean approved = Boolean.TRUE;
        Boolean rejected = Boolean.FALSE;

        // Проверка получения аренды
        NotFoundException exceptionBooking = assertThrows(NotFoundException.class, () -> {
            bookingService.approveBooking(fakeBookingId, realUserId, approved);
        });
        assertEquals("Запроса на аренду с id = " + fakeBookingId + " не существует.", exceptionBooking.getMessage());

        // Проверка статуса аренды: APPROVED
        bookingStatus = BookingStatus.APPROVED;
        BadRequestException exceptionApproved = assertThrows(BadRequestException.class, () -> {
            bookingService.approveBooking(realBookingId, realUserId, approved);
        });
        assertEquals("Запрос на аренду с id = " + realBookingId + " уже обработан.", exceptionApproved.getMessage());

        // Проверка статуса аренды: REJECTED
        bookingStatus = BookingStatus.REJECTED;
        BadRequestException exceptionRejected = assertThrows(BadRequestException.class, () -> {
            bookingService.approveBooking(realBookingId, realUserId, approved);
        });
        assertEquals("Запрос на аренду с id = " + realBookingId + " уже обработан.", exceptionRejected.getMessage());
        bookingStatus = BookingStatus.WAITING;

        // Проверка существования пользователя
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            bookingService.approveBooking(realBookingId, fakeUserId, approved);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка доступа пользователя
        NotFoundException exceptionAccess = assertThrows(NotFoundException.class, () -> {
            bookingService.approveBooking(realBookingId, realUserNotOwnerId, approved);
        });
        assertEquals("Пользователь с id = " + realUserNotOwnerId + " не имеет доступа.", exceptionAccess.getMessage());

        // Проверка доступа для сдачи
        isAvailable = false;
        BadRequestException exceptionBooked = assertThrows(BadRequestException.class, () -> {
            bookingService.approveBooking(realBookingId, realUserId, approved);
        });
        assertEquals("Вещь с id = " + itemId + " уже сдана в аренду.", exceptionBooked.getMessage());
        isAvailable = true;

        // Проверка подтверждения
        BookingDto bookingDtoApproved = bookingService.approveBooking(realBookingId, realUserId, approved);
        assertEquals(itemId, bookingDtoApproved.getItem().getId());
        assertEquals(BookingStatus.APPROVED, bookingDtoApproved.getStatus());
        assertEquals(1L, bookingDtoApproved.getId());

        // Проверка отмены
        BookingDto bookingDtoRejected = bookingService.approveBooking(realBookingId, realUserId, rejected);
        assertEquals(itemId, bookingDtoRejected.getItem().getId());
        assertEquals(BookingStatus.REJECTED, bookingDtoRejected.getStatus());
        assertEquals(1L, bookingDtoRejected.getId());

    }

    @Test
    void getBooking() {
        Long realUserIdOther = getRandomKey();
        Long fakeUserId = getFakeKey();
        Long realBookingId = getRandomKey();
        Long fakeBookingId = getFakeKey();
        Long realUserIdOwner = itemOwnerId;
        Long realUserIdRenter = realBookingId;

        // Проверка пользователя
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            bookingService.getBooking(realBookingId, fakeUserId);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка аренды
        NotFoundException exceptionBooking = assertThrows(NotFoundException.class, () -> {
            bookingService.getBooking(fakeBookingId, realUserIdOther);
        });
        assertEquals("Запроса на аренду с id = " + fakeBookingId + " не существует.", exceptionBooking.getMessage());

        // Проверка доступа
        NotFoundException exceptionAccess = assertThrows(NotFoundException.class, () -> {
            bookingService.getBooking(realBookingId, realUserIdOther);
        });
        assertEquals("Пользователь с id = " + realUserIdOther + " не имеет доступа.", exceptionAccess.getMessage());

        // Проверка получения владельцем
        BookingDto bookingDtoOwner = bookingService.getBooking(realBookingId, realUserIdOwner);
        assertEquals(realBookingId, bookingDtoOwner.getId());
        // Проверить id пользователя нет т.к. в ItemDto нет этого поля

        // Проверка получения арендатором
        BookingDto bookingDtoRenter = bookingService.getBooking(realBookingId, realUserIdRenter);
        assertEquals(realBookingId, bookingDtoRenter.getId());
        assertEquals(realUserIdRenter, bookingDtoRenter.getBooker().getId());

    }

    @Test
    void getBookings() {
        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        // Проверка пользователя
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            bookingService.getBookings(fakeUserId, BookingState.ALL);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка ALL
        List<BookingDto> bookingDtosALL = bookingService.getBookings(realUserId, BookingState.ALL);
        verify(bookingRepository, times(1)).findAllByRenterIdOrderByIdDesc(realUserId);

        // Проверка CURRENT
        List<BookingDto> bookingDtosCURRENT = bookingService.getBookings(realUserId, BookingState.CURRENT);
        verify(bookingRepository, times(1)).getBookingsCurrentForRenter(realUserId);

        // Проверка PAST
        List<BookingDto> bookingDtosPAST = bookingService.getBookings(realUserId, BookingState.PAST);
        verify(bookingRepository, times(1)).getBookingsPastForRenter(realUserId,
                BookingStatus.APPROVED);

        // Проверка FUTURE
        List<BookingDto> bookingDtosFUTURE = bookingService.getBookings(realUserId, BookingState.FUTURE);
        verify(bookingRepository, times(1)).getBookingsFutureForRenter(realUserId,
                BookingStatus.WAITING, BookingStatus.APPROVED);

        // Проверка WAITING
        List<BookingDto> bookingDtosWAITING = bookingService.getBookings(realUserId, BookingState.WAITING);
        verify(bookingRepository, times(1)).getBookingsWaitingForRenter(realUserId,
                BookingStatus.WAITING);

        // Проверка REJECTED
        List<BookingDto> bookingDtosREJECTED = bookingService.getBookings(realUserId, BookingState.REJECTED);
        verify(bookingRepository, times(1)).getBookingsRejectedForRenter(realUserId,
                BookingStatus.REJECTED);
    }

    @Test
    void getBookingsOwner() {
        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        // Проверка пользователя
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            bookingService.getBookingsOwner(fakeUserId, BookingState.ALL);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка ALL
        List<BookingDto> bookingDtosALL = bookingService.getBookingsOwner(realUserId, BookingState.ALL);
        verify(bookingRepository, times(1)).findAllByOwnerIdOrderByIdDesc(realUserId);

        // Проверка CURRENT
        List<BookingDto> bookingDtosCURRENT = bookingService.getBookingsOwner(realUserId, BookingState.CURRENT);
        verify(bookingRepository, times(1)).getBookingsCurrentForOwner(realUserId);

        // Проверка PAST
        List<BookingDto> bookingDtosPAST = bookingService.getBookingsOwner(realUserId, BookingState.PAST);
        verify(bookingRepository, times(1)).getBookingsPastForOwner(realUserId,
                BookingStatus.APPROVED);

        // Проверка FUTURE
        List<BookingDto> bookingDtosFUTURE = bookingService.getBookingsOwner(realUserId, BookingState.FUTURE);
        verify(bookingRepository, times(1)).getBookingsFutureForOwner(realUserId,
                BookingStatus.WAITING, BookingStatus.APPROVED);

        // Проверка WAITING
        List<BookingDto> bookingDtosWAITING = bookingService.getBookingsOwner(realUserId, BookingState.WAITING);
        verify(bookingRepository, times(1)).getBookingsWaitingForOwner(realUserId,
                BookingStatus.WAITING);

        // Проверка REJECTED
        List<BookingDto> bookingDtosREJECTED = bookingService.getBookingsOwner(realUserId, BookingState.REJECTED);
        verify(bookingRepository, times(1)).getBookingsRejectedForOwner(realUserId,
                BookingStatus.REJECTED);

    }

    private Long getRandomKey() {
        Random random = new Random();
        while (true) {
            Long id = reservedId + 1 + random.nextLong(maxId - reservedId);
            if (!usedIds.contains(id)) {
                usedIds.add(id);
                return id;
            }
            if (usedIds.size() + reservedId == maxId) return null;
        }
    }

    private Long getFakeKey() {
        Random random = new Random();
        return 5 * reservedId + 1 + random.nextLong(5 * reservedId);
    }

    private User getUser(Long userId) {
        return new User(userId, "name" + userId, "email" + userId + "@email.com");
    }

    private BookingDtoShort getBookingDtoShort(Long itemId, int minusStartDays, int minusEndDays) {
        return BookingDtoShort.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusDays(minusStartDays))
                .end(LocalDateTime.now().minusDays(minusEndDays))
                .build();
    }

    private Item getItem(Long itemId, Long ownerId) {
        return Item.builder()
                .id(itemId)
                .name("name" + itemId)
                .description("description" + itemId)
                .available(isAvailable)
                .owner(getUser(ownerId))
                .request(null)
                .build();
    }

    private Booking getBooking(Long bookingId) {
        return Booking.builder()
                .id(bookingId)
                .renter(getUser(bookingId))
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .item(getItem(itemId, itemOwnerId))
                .status(bookingStatus)
                .build();
    }

    private List<Booking> getBookingsWithState(Long count, BookingState state) {
        List<Booking> bookings = new ArrayList<>();
        for (Long i = 1L; i <= count; i++) {
            Booking booking = getBooking(i);
            booking.getItem().setDescription(state.name());
            bookings.add(booking);
        }
        return bookings;
    }

}