package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testEquals() {
        User user1 = User.builder()
                .id(1L)
                .name("name")
                .email("name@email.com")
                .build();
        User user2 = User.builder()
                .id(1L)
                .name("name")
                .email("name@email.com")
                .build();
        User user3 = User.builder()
                .id(1L)
                .name("Name")
                .email("name@email.com")
                .build();
        User user4 = User.builder()
                .id(1L)
                .name("name")
                .email("name@email.ru")
                .build();

        assertEquals(user1, user2);
        assertEquals(user1, user1);
        assertNotEquals(user1, null);
        assertNotEquals(user1, new Object());
        assertNotEquals(user1, user3);
        assertNotEquals(user1, user4);
    }

    @Test
    void testHashCode() {
        User user1 = User.builder()
                .id(1L)
                .name("name")
                .email("name@email.com")
                .build();
        User user2 = User.builder()
                .id(1L)
                .name("name")
                .email("name@email.com")
                .build();
        User user3 = User.builder()
                .id(2L)
                .name("name")
                .email("name@email.com")
                .build();
        User user4 = User.builder()
                .id(1L)
                .name("namee")
                .email("name@email.com")
                .build();
        User user5 = User.builder()
                .id(1L)
                .name("name")
                .email("namee@email.com")
                .build();

        assertEquals(user1.hashCode(), user2.hashCode());
        assertEquals(user1.hashCode(), user1.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
        assertNotEquals(user1.hashCode(), user4.hashCode());
        assertNotEquals(user1.hashCode(), user5.hashCode());
    }
}