package ru.practicum.shareit.user;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.client.ResponseHandler;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService {

    private final UserClient userClient;
    private final ResponseHandler responseHandler;

    public UserDto getUser(Long id) {
        log.info("GATEWAY: получен запрос на получение пользователя.");
        ResponseEntity<Object> response = userClient.getUser(id);
        UserDto userDto = responseHandler.handleResponse(response, new TypeReference<UserDto>(){});
        log.info("GATEWAY: обработан запрос на получение пользователя.");
        return userDto;
    }

    public List<UserDto> getUsers() {
        log.info("GATEWAY: получен запрос на получение всех пользователей.");
        ResponseEntity<Object> response = userClient.getUsers();
        List<UserDto> userDtos = responseHandler.handleResponse(response, new TypeReference<List<UserDto>>(){});
        log.info("GATEWAY: обработан запрос на получение всех пользователей.");
        return userDtos;
    }

    public UserDto addUser(UserDto user) {
        log.info("GATEWAY: получен запрос на добавление пользователя.");
        ResponseEntity<Object> response = userClient.addUser(user);
        UserDto userDtoCreated = responseHandler.handleResponse(response, new TypeReference<UserDto>(){});
        log.info("GATEWAY: обработан запрос на добавление пользователя.");
        return userDtoCreated;
    }

    public UserDto updateUser(Long id, UserDto user) {
        log.info("GATEWAY: получен запрос на обновление пользователя.");
        ResponseEntity<Object> response = userClient.updateUser(id, user);
        UserDto userDtoUpdated = responseHandler.handleResponse(response, new TypeReference<UserDto>(){});
        log.info("GATEWAY: обработан запрос на обновление пользователя.");
        return userDtoUpdated;
    }

    public UserDto deleteUser(Long id) {
        log.info("GATEWAY: получен запрос на удаление пользователя.");
        ResponseEntity<Object> response = userClient.deleteUser(id);
        UserDto userDtoDeleted = responseHandler.handleResponse(response, new TypeReference<UserDto>(){});
        log.info("GATEWAY: обработан запрос на удаление пользователя.");
        return userDtoDeleted;
    }

}
