package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestTest {

    @Test
    void testEquals() {
        User user1 = User.builder()
                .id(1L)
                .name("name")
                .email("name@email.com")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("name")
                .email("name@email.com")
                .build();
        LocalDateTime now = LocalDateTime.now();
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("description1")
                .created(now)
                .creator(user1)
                .build();
        ItemRequest request2 = ItemRequest.builder()
                .id(1L)
                .description("description1")
                .created(now)
                .creator(user1)
                .build();
        ItemRequest request3 = ItemRequest.builder()
                .id(1L)
                .description("description1")
                .created(now)
                .creator(user2)
                .build();
        ItemRequest request4 = ItemRequest.builder()
                .id(2L)
                .description("description1")
                .created(now)
                .creator(user1)
                .build();

        assertEquals(request1, request2);
        assertEquals(request1, request1);
        assertNotEquals(request1, null);
        assertNotEquals(request1, new Object());
        assertNotEquals(request1, request3);
        assertNotEquals(request1, request4);
    }

    @Test
    void testHashCode() {
        User user = User.builder()
                .id(1L)
                .name("name")
                .email("name@email.com")
                .build();
        User differentUser = User.builder()
                .id(2L)
                .name("name")
                .email("name@email.com")
                .build();
        LocalDateTime now = LocalDateTime.now();
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("description1")
                .created(now)
                .creator(user)
                .build();
        ItemRequest request2 = ItemRequest.builder()
                .id(1L)
                .description("description1")
                .created(now)
                .creator(user)
                .build();
        ItemRequest request3 = ItemRequest.builder()
                .id(1L)
                .description("description1")
                .created(now)
                .creator(differentUser)
                .build();
        ItemRequest request4 = ItemRequest.builder()
                .id(2L)
                .description("description1")
                .created(now)
                .creator(user)
                .build();

        assertEquals(request1.hashCode(), request2.hashCode());
        assertEquals(request1.hashCode(), request1.hashCode());
        assertNotEquals(request1.hashCode(), request3.hashCode());
        assertNotEquals(request1.hashCode(), request4.hashCode());
    }
}