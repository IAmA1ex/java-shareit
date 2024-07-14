package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;
import java.util.List;

public interface UserRepository {

    User getUser(Long id);

    User addUser(User user);

    User updateUser(User user);

    User deleteUser(Long id);

    boolean containsUser(Long id);

    boolean containsEmail(String email);

    List<User> getUsers();
}
