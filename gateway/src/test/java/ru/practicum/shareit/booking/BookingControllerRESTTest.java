package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerRESTTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;

    @BeforeEach
    void setUp() {

        usedIds = new ArrayList<>();

        when(bookingService.createBooking(any(BookingDtoShort.class), anyLong())).thenAnswer(arguments -> {
            BookingDtoShort bookingDtoShort = arguments.getArgument(0);
            Long userRenterId = arguments.getArgument(1);
            return getBookingDto(1L, bookingDtoShort, userRenterId);
        });

        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean())).thenAnswer(arguments -> {
            Long bookingId = arguments.getArgument(0);
            Long itemOwnerId = arguments.getArgument(1);
            Boolean approved = arguments.getArgument(2);
            BookingDtoShort bookingDtoShort = getBookingDtoShort(13L);
            BookingDto bookingDto = getBookingDto(bookingId, bookingDtoShort, 14L);
            bookingDto.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
            return bookingDto;
        });

        when(bookingService.getBooking(anyLong(), anyLong())).thenAnswer(arguments -> {
            Long bookingId = arguments.getArgument(0);
            Long userId = arguments.getArgument(1);
            BookingDtoShort bookingDtoShort = getBookingDtoShort(9L);
            return getBookingDto(bookingId, bookingDtoShort, userId);
        });

        when(bookingService.getBookings(anyLong(), any(BookingState.class))).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            BookingState bookingState = arguments.getArgument(1);
            List<BookingDto> bookingDtos = new ArrayList<>();
            for (long i = 1; i <= 3; i++) {
                BookingDtoShort bookingDtoShort = getBookingDtoShort(getRandomKey());
                BookingDto bookingDto = getBookingDto(i, bookingDtoShort, userId);
                bookingDtos.add(bookingDto);
            }
            return bookingDtos;
        });

        when(bookingService.getBookingsOwner(anyLong(), any(BookingState.class))).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            BookingState bookingState = arguments.getArgument(1);
            List<BookingDto> bookingDtos = new ArrayList<>();
            for (long i = 1; i <= 3; i++) {
                BookingDtoShort bookingDtoShort = getBookingDtoShort(getRandomKey());
                BookingDto bookingDto = getBookingDto(i, bookingDtoShort, userId);
                bookingDtos.add(bookingDto);
            }
            return bookingDtos;
        });

    }

    @Test
    void createBooking() {
        Long itemId = getRandomKey();
        Long userRenterId = getRandomKey();
        BookingDtoShort bookingDtoShort = getBookingDtoShort(itemId);

        try {
            mockMvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookingDtoShort))
                            .header("X-Sharer-User-Id", userRenterId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.item.id").value(itemId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.booker.id").value(userRenterId));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    void approveBooking() {
        Long bookingId = getRandomKey();
        Long itemOwnerId = getRandomKey();
        Boolean approved = Boolean.TRUE;
        BookingStatus bookingStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;

        try {
            mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                            .param("approved", approved.toString())
                            .header("X-Sharer-User-Id", itemOwnerId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(bookingId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(bookingStatus.toString()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getBooking() {
        Long bookingId = getRandomKey();
        Long ownerOrRenterUserId = getRandomKey();

        try {
            mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                            .header("X-Sharer-User-Id", ownerOrRenterUserId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(bookingId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.booker.id").value(ownerOrRenterUserId));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getBookings() {
        Long ownerOrRenterUserId = getRandomKey();
        BookingState bookingState = BookingState.PAST;

        try {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", ownerOrRenterUserId)
                            .param("state", bookingState.toString()))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[*].booker.id", everyItem(equalTo(ownerOrRenterUserId.intValue()))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[*].status", everyItem(equalTo(BookingStatus.APPROVED.toString()))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getBookingsSlash() {
        getBookings();
    }

    @Test
    void getBookingsOwner() {
        Long ownerOrRenterUserId = getRandomKey();
        BookingState bookingState = BookingState.PAST;

        try {
            mockMvc.perform(get("/bookings")
                            .header("X-Sharer-User-Id", ownerOrRenterUserId)
                            .param("state", bookingState.toString()))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[*].booker.id", everyItem(equalTo(ownerOrRenterUserId.intValue()))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[*].status", everyItem(equalTo(BookingStatus.APPROVED.toString()))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
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

    private BookingDto getBookingDto(Long bookingDtoId, BookingDtoShort bookingDtoShort, Long userId) {
        return BookingDto.builder()
                .id(bookingDtoId)
                .start(bookingDtoShort.getStart())
                .end(bookingDtoShort.getEnd())
                .status(BookingStatus.APPROVED)
                .booker(getUserDto(userId))
                .item(getItemDto(bookingDtoShort.getItemId()))
                .build();
    }

    private BookingDtoShort getBookingDtoShort(Long itemId) {
        return BookingDtoShort.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();
    }

    private UserDto getUserDto(Long userDtoId) {
        return UserDto.builder()
                .id(userDtoId)
                .name("name" + userDtoId)
                .email("email" + userDtoId + "@email.com")
                .build();
    }

    private ItemDto getItemDto(Long itemDtoId) {
        return ItemDto.builder()
                .id(itemDtoId)
                .name("name" + itemDtoId)
                .description("description" + itemDtoId)
                .available(Boolean.TRUE)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .requestId(null)
                .build();
    }
}