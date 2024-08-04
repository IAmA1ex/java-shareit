package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService {

    private final UserClient userClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserDto getUser(Long id) {
        log.info("GATEWAY: получен запрос на получение пользователя.");
        ResponseEntity<Object> response = userClient.getUser(id);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            UserDto userDto = getBodyClass(response, UserDto.class);
            log.info("GATEWAY: обработан запрос на получение пользователя.");
            return userDto;
        }
        return null; // !!!!!
    }

    public List<UserDto> getUsers() {
        log.info("GATEWAY: получен запрос на получение всех пользователей.");
        ResponseEntity<Object> response = userClient.getUsers();
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<UserDto> userDtos = (List<UserDto>) response.getBody();
            log.info("GATEWAY: обработан запрос на получение всех пользователей.");
            return userDtos;
        }
        return null; // !!!!!
    }

    public UserDto addUser(UserDto user) {
        log.info("GATEWAY: получен запрос на добавление пользователя.");
        ResponseEntity<Object> response = userClient.addUser(user);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            UserDto userDtoCreated = getBodyClass(response, UserDto.class);
            log.info("GATEWAY: обработан запрос на добавление пользователя.");
            return userDtoCreated;
        }
        return null; // !!!!!
    }

    public UserDto updateUser(Long id, UserDto user) {
        log.info("GATEWAY: получен запрос на обновление пользователя.");
        ResponseEntity<Object> response = userClient.updateUser(id, user);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            UserDto userDtoUpdated = (UserDto) response.getBody();
            log.info("GATEWAY: обработан запрос на обновление пользователя.");
            return userDtoUpdated;
        }
        return null; // !!!!!
    }

    public UserDto deleteUser(Long id) {
        log.info("GATEWAY: получен запрос на удаление пользователя.");
        ResponseEntity<Object> response = userClient.deleteUser(id);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            UserDto userDtoDeleted = (UserDto) response.getBody();
            log.info("GATEWAY: обработан запрос на удаление пользователя.");
            return userDtoDeleted;
        }
        return null; // !!!!!
    }

    private <T> T getBodyClass(ResponseEntity<Object> reo, Class<T> type) {
        try {
            if (reo.getBody() != null) {
                String json = objectMapper.writeValueAsString(reo.getBody());
                return objectMapper.readValue(json, type);
            } else return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
