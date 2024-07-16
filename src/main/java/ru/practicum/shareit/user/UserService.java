package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ObjectValidator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ObjectValidator<User> userValidator;

    public User getUser(Long id) {
        log.info("Запрос на получение пользователя с id = {}.", id);
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не существует.");
        }
        User user = optUser.get();
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
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicatedDataException("Пользователь с электронной почтой " + user.getEmail() + " уже существует.");
        }
        User newUser = userRepository.save(user);
        log.info("Создан пользователь {}.", newUser);
        return newUser;
    }

    public User updateUser(Long id, User user) {
        log.info("Запрос на обновление пользователя полями {}.", user);
        Optional<User> optOldUser = userRepository.findById(id);
        if (optOldUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не существует.");
        }
        User oldUser = optOldUser.get();
        User newUser = User.builder()
                .id(id)
                .email(user.getEmail() == null ? oldUser.getEmail() : user.getEmail())
                .name(user.getName() == null ? oldUser.getName() : user.getName())
                .build();

        userValidator.validateObject(newUser);

        if (!oldUser.getEmail().equals(newUser.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicatedDataException("Пользователь с электронной почтой " + user.getEmail() + " уже существует.");
        }
        User updated = userRepository.save(newUser);
        log.info("Пользователь обновлен {}.", updated);
        return updated;
    }

    public User deleteUser(Long id) {
        log.info("Запрос на удаление пользователя с id = {}.", id);
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не существует.");
        }
        User user = optUser.get();
        userRepository.deleteById(id);
        log.info("Удален пользователь {}.", user);
        return user;
    }

}
