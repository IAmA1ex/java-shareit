package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long id) {
        log.info("Запрос на получение пользователя с id = {}.", id);
        User user = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + id + " не существует."));
        log.info("Получен пользователь {}.", user);
        return user;
    }

    public List<User> getUsers() {
        log.info("Запрос на получение всех пользователей.");
        List<User> users = userRepository.findAll();
        log.info("Получено {} пользователей.", users.size());
        return users;
    }

    public User addUser(User user) {
        log.info("Запрос на добавление пользователя {}.", user);
        User newUser;
        try {
            newUser = userRepository.save(user);
        } catch (Exception e) {
            if (e.getCause().getMessage().contains("[23505-") &&
                    e.getCause().getMessage().contains("EMAIL NULLS FIRST")) {
                throw new DuplicatedDataException("Пользователь с электронной почтой " + user.getEmail() + " уже существует.");
            }
            throw new RuntimeException("Ошибка при создании пользователя.");
        }
        log.info("Создан пользователь {}.", newUser);
        return newUser;
    }

    public User updateUser(Long id, User user) {
        log.info("Запрос на обновление пользователя полями {}.", user);
        User oldUser = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + id + " не существует."));
        User newUser = User.builder()
                .id(id)
                .email(user.getEmail() != null && !user.getEmail().isBlank() && isEmail(user.getEmail()) ?
                        user.getEmail() : oldUser.getEmail())
                .name(user.getName() != null && !user.getName().isBlank() ?
                        user.getName() : oldUser.getName())
                .build();
        if (!oldUser.getEmail().equals(newUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new DuplicatedDataException("Пользователь с электронной почтой " + user.getEmail() + " уже существует.");
            }
        }
        User updated = userRepository.save(newUser);
        log.info("Пользователь обновлен {}.", updated);
        return updated;
    }

    private boolean isEmail(String email) {
        String EMAIL_REGEX = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,3}$";
        Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public User deleteUser(Long id) {
        log.info("Запрос на удаление пользователя с id = {}.", id);
        User user = userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + id + " не существует."));
        userRepository.deleteById(id);
        log.info("Удален пользователь {}.", user);
        return user;
    }

}
