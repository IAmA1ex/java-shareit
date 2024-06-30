package ru.practicum.shareit.user;

import jakarta.validation.*;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.Set;

@Component
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(Long id) {
        return userRepository.getUser(id);
    }

    public List<User> getUsers() {
        return userRepository.getUsers();
    }

    public User addUser(User user) {
        if (userRepository.containsEmail(user.getEmail())) {
            throw new DuplicatedDataException("Пользователь с электронной почтой " + user.getEmail() + " уже существует");
        }
        return userRepository.addUser(user);
    }

    public User updateUser(Long id, User user) {
        if (!userRepository.containsUser(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не существует");
        }
        User oldUser = userRepository.getUser(id);
        User newUser = User.builder()
                .id(id)
                .email(user.getEmail() == null ? oldUser.getEmail() : user.getEmail())
                .name(user.getName() == null ? oldUser.getName() : user.getName())
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(newUser);
        if (!violations.isEmpty()) {
            throw new ValidationException(violations.stream().toList().getFirst().getMessage());
        }
        if (!oldUser.getEmail().equals(newUser.getEmail()) && userRepository.containsEmail(user.getEmail())) {
            throw new DuplicatedDataException("Пользователь с электронной почтой " + user.getEmail() + " уже существует");
        }
        return userRepository.updateUser(newUser);
    }

    public User deleteUser(Long id) {
        return userRepository.deleteUser(id);
    }

}
