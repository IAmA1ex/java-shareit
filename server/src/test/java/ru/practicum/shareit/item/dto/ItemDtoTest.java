package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import ru.practicum.shareit.booking.dto.BookingForItem;

import java.time.LocalDateTime;
import java.util.Collections;

class ItemDtoTest {

    @Test
    void testEquals() {
        BookingForItem booking1 = BookingForItem.builder()
                .id(1L)
                .bookerId(1L)
                .build();
        BookingForItem booking2 = BookingForItem.builder()
                .id(2L)
                .bookerId(2L)
                .build();
        CommentDto comment1 = CommentDto.builder()
                .id(1L)
                .text("text1")
                .authorName("author1")
                .created(LocalDateTime.now())
                .build();
        CommentDto comment2 = CommentDto.builder()
                .id(2L)
                .text("text2")
                .authorName("author2")
                .created(LocalDateTime.now())
                .build();
        ItemDto dto1 = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment1))
                .requestId(1L)
                .build();
        ItemDto dto2 = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment1))
                .requestId(1L)
                .build();
        ItemDto dto3 = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment2))
                .requestId(1L)
                .build();
        ItemDto dto4 = ItemDto.builder()
                .id(2L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment1))
                .requestId(1L)
                .build();
        assertEquals(dto1, dto2);
        assertEquals(dto1, dto1);
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1, dto4);
    }

    @Test
    void testHashCode() {
        BookingForItem booking1 = BookingForItem.builder()
                .id(1L)
                .bookerId(1L)
                .build();
        BookingForItem booking2 = BookingForItem.builder()
                .id(2L)
                .bookerId(2L)
                .build();
        CommentDto comment1 = CommentDto.builder()
                .id(1L)
                .text("text1")
                .authorName("author1")
                .created(LocalDateTime.now())
                .build();
        CommentDto comment2 = CommentDto.builder()
                .id(2L)
                .text("text2")
                .authorName("author2")
                .created(LocalDateTime.now())
                .build();
        ItemDto dto1 = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment1))
                .requestId(1L)
                .build();
        ItemDto dto2 = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment1))
                .requestId(1L)
                .build();
        ItemDto dto3 = ItemDto.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment2))
                .requestId(1L)
                .build();
        ItemDto dto4 = ItemDto.builder()
                .id(2L)
                .name("item1")
                .description("description1")
                .available(true)
                .lastBooking(booking1)
                .nextBooking(booking2)
                .comments(Collections.singletonList(comment1))
                .requestId(1L)
                .build();
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertEquals(dto1.hashCode(), dto1.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertNotEquals(dto1.hashCode(), dto4.hashCode());
    }
}
