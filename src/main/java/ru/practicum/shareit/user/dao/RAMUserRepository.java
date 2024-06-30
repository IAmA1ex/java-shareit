package ru.practicum.shareit.user.dao;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;
import java.util.*;

@Component
public class RAMUserRepository implements UserRepository {

    private Long id = 1L;
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();


    @Override
    public User getUser(Long id) {
        return users.get(id);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User addUser(User user) {
        if (containsEmail(user.getEmail())) return null;
        user.setId(generateId());
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public User updateUser(User user) {
        User oldUser = users.get(user.getId());
        if (!oldUser.getEmail().equals(user.getEmail())) {
            emails.remove(oldUser.getEmail());
            emails.add(user.getEmail());
        }
        users.replace(user.getId(), user);
        return user;
    }

    @Override
    public User deleteUser(Long id) {
        User user = users.remove(id);
        emails.remove(user.getEmail());
        return user;
    }

    @Override
    public boolean containsUser(Long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean containsEmail(String email) {
        return emails.contains(email);
    }

    private Long generateId() {
        return id++;
    }
}
