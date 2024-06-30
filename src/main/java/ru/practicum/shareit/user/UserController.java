package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable final Long id) {
        return userService.getUser(id);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody final User user) {
        return userService.addUser(user);
    }

    @PatchMapping("/{id}")
    public User updateUser(@RequestBody final User user, @PathVariable final Long id) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public User deleteUser(@PathVariable final Long id) {
        return userService.deleteUser(id);
    }

}
