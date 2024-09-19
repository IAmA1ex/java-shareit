package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;

    private final Long maxRealId = 100L;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);

        when(userRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxRealId) {
                return Optional.of(new User(id, "name" + id, "user" + id + "@email.com"));
            } else return Optional.empty();
        });

        when(userRepository.findAll()).thenReturn(List.of());

        when(userRepository.save(any())).thenAnswer(arguments -> {
            User user = arguments.getArgument(0);
            if (user.getEmail().equals("exist@email.com")) {
                RuntimeException runtimeException = new RuntimeException();
                runtimeException.initCause(new RuntimeException("[23505-414] EMAIL NULLS FIRST"));
                throw runtimeException;
            }
            if (user.getId() == null) user.setId(maxRealId + 1);
            return user;
        });

        when(userRepository.existsById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxRealId) return true;
            return false;
        });

        when(userRepository.existsByEmail(anyString())).thenAnswer(arguments -> {
            String email = arguments.getArgument(0);
            return email.contains("exist");
        });
    }

    @Test
    void getUser() {
        Long realId = getRandomKey();
        Long fakeId = getFakeKey();

        User user = new User(realId, "name" + realId, "user" + realId + "@email.com");

        assertEquals(user, userService.getUser(realId));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUser(fakeId));
        assertEquals("Пользователь с id = " + fakeId + " не существует.", exception.getMessage());
    }

    @Test
    void getUsers() {
        assertEquals(List.of(), userService.getUsers());
    }

    @Test
    void addUser() {
        User newUser = new User(null, "newUser", "newUser@email.com");
        User newUserWithExistEmail = new User(null, "newUserWithExistEmail", "exist@email.com");

        User savedUser = userService.addUser(newUser);
        newUser.setId(maxRealId + 1);

        assertEquals(newUser, savedUser);

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class, () ->
                userService.addUser(newUserWithExistEmail));
        assertEquals("Пользователь с электронной почтой " + newUserWithExistEmail.getEmail() + " уже существует.",
                exception.getMessage());
    }

    @Test
    void updateUser() {
        User notExistUser = new User(maxRealId + 1, "notExistUser", "notExistUser@email.com");
        User notValidUserName1 = new User(maxRealId, "", "newNotValidUser@email.com");
        User notValidUserName2 = new User(maxRealId, null, "newNotValidUser@email.com");
        User notValidUserEmail1 = new User(maxRealId, "notValidUser", "notValidUserAemail.com");
        User notValidUserEmail2 = new User(maxRealId, "notValidUser", "");
        User notValidUserEmail3 = new User(maxRealId, "notValidUser", null);
        User existEmailUser = new User(getRandomKey(),
                "newUserWithExistEmail", "existEmail@email.com");
        User updateUser = new User(maxRealId, "updateUser", "updateUser@email.com");

        NotFoundException exception1 = assertThrows(NotFoundException.class, () -> {
            userService.updateUser(notExistUser.getId(), notExistUser);
        });
        assertEquals("Пользователь с id = " + notExistUser.getId() + " не существует.",
            exception1.getMessage());

        User userValid = userService.updateUser(notValidUserName1.getId(), notValidUserName1);
        assertEquals(notValidUserName1.getId(), userValid.getId());
        assertEquals(notValidUserName1.getEmail(), userValid.getEmail());
        assertNotEquals(notValidUserName1.getName(), userValid.getName());

        userValid = userService.updateUser(notValidUserName2.getId(), notValidUserName2);
        assertEquals(notValidUserName2.getId(), userValid.getId());
        assertEquals(notValidUserName2.getEmail(), userValid.getEmail());
        assertNotEquals(notValidUserName2.getName(), userValid.getName());

        userValid = userService.updateUser(notValidUserEmail1.getId(), notValidUserEmail1);
        assertEquals(notValidUserEmail1.getId(), userValid.getId());
        assertEquals(notValidUserEmail1.getName(), userValid.getName());
        assertNotEquals(notValidUserEmail1.getEmail(), userValid.getEmail());

        userValid = userService.updateUser(notValidUserEmail2.getId(), notValidUserEmail2);
        assertEquals(notValidUserEmail2.getId(), userValid.getId());
        assertEquals(notValidUserEmail2.getName(), userValid.getName());
        assertNotEquals(notValidUserEmail2.getEmail(), userValid.getEmail());

        userValid = userService.updateUser(notValidUserEmail3.getId(), notValidUserEmail3);
        assertEquals(notValidUserEmail3.getId(), userValid.getId());
        assertEquals(notValidUserEmail3.getName(), userValid.getName());
        assertNotEquals(notValidUserEmail3.getEmail(), userValid.getEmail());

        DuplicatedDataException exception3 = assertThrows(DuplicatedDataException.class, () -> {
            userService.updateUser(existEmailUser.getId(), existEmailUser);
        });
        assertEquals("Пользователь с электронной почтой " + existEmailUser.getEmail() + " уже существует.",
            exception3.getMessage());

        User updatedUser = userService.updateUser(updateUser.getId(), updateUser);
        assertEquals(updateUser, updatedUser);
    }

    @Test
    void deleteUser() {
        Long realId = getRandomKey();
        Long fakeId = getFakeKey();

        User realUser = new User(realId, "name" + realId, "user" + realId + "@email.com");

        User deletedUser = userService.deleteUser(realId);
        assertEquals(realUser, deletedUser);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.deleteUser(fakeId);
        });
        assertEquals("Пользователь с id = " + fakeId + " не существует.", exception.getMessage());
    }

    public Long getRandomKey() {
        Random random = new Random();
        return 1 + random.nextLong(maxRealId);
    }

    public Long getFakeKey() {
        return maxRealId + 1;
    }
}