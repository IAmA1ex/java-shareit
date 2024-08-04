package ru.practicum.shareit.user.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserRepositoryTest {

    private final UserRepository userRepository;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;

    @BeforeEach
    void setUp() {
        usedIds = new ArrayList<>();
    }

    @Test
    void existsById() {
        User user = getUser(null);
        User userSaved = userRepository.save(user);
        Long id = userSaved.getId();
        boolean exists = userRepository.existsById(id);
        assertTrue(exists);
    }

    @Test
    void existsByEmail() {
        User user = getUser(null);
        User userSaved = userRepository.save(user);
        boolean exists = userRepository.existsByEmail(userSaved.getEmail());
        assertTrue(exists);
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");
        assertFalse(notExists);
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

}