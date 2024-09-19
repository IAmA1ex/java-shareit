package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testEquals() {
        User user1 = User.builder()
                .id(1L)
                .name("user1")
                .email("user1@email.com")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("user2")
                .email("user2@email.com")
                .build();
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("request1")
                .created(LocalDateTime.now())
                .creator(user1)
                .build();
        ItemRequest request2 = ItemRequest.builder()
                .id(2L)
                .description("request2")
                .created(LocalDateTime.now())
                .creator(user2)
                .build();
        Item item1 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request1)
                .build();
        Item item2 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request1)
                .build();
        Item item3 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user2)
                .request(request1)
                .build();
        Item item4 = Item.builder()
                .id(2L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request1)
                .build();
        Item item5 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request2)
                .build();

        assertEquals(item1, item2);
        assertEquals(item1, item1);
        assertNotEquals(item1, null);
        assertNotEquals(item1, new Object());
        assertNotEquals(item1, item3);
        assertNotEquals(item1, item4);
        assertNotEquals(item1, item5);
    }

    @Test
    void testHashCode() {
        User user1 = User.builder()
                .id(1L)
                .name("user1")
                .email("user1@email.com")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("user2")
                .email("user2@email.com")
                .build();
        ItemRequest request1 = ItemRequest.builder()
                .id(1L)
                .description("request1")
                .created(LocalDateTime.now())
                .creator(user1)
                .build();
        ItemRequest request2 = ItemRequest.builder()
                .id(2L)
                .description("request2")
                .created(LocalDateTime.now())
                .creator(user2)
                .build();

        Item item1 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request1)
                .build();
        Item item2 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request1)
                .build();
        Item item3 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user2)
                .request(request1)
                .build();
        Item item4 = Item.builder()
                .id(2L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request1)
                .build();
        Item item5 = Item.builder()
                .id(1L)
                .name("item1")
                .description("description1")
                .available(true)
                .owner(user1)
                .request(request2)
                .build();

        assertEquals(item1.hashCode(), item2.hashCode());
        assertEquals(item1.hashCode(), item1.hashCode());
        assertNotEquals(item1.hashCode(), item3.hashCode());
        assertNotEquals(item1.hashCode(), item4.hashCode());
        assertNotEquals(item1.hashCode(), item5.hashCode());
    }
}