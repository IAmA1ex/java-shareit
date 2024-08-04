package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerRESTTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;

    @BeforeEach
    void setUp() {

        usedIds = new ArrayList<>();

        when(userService.getUser(anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            return getUserDto(userId);
        });

        when(userService.getUsers()).thenAnswer(arguments -> {
            List<UserDto> userDtos = new ArrayList<>();
            for (long i = 1; i <= 5; i++) {
                UserDto userDto = getUserDto(i);
                userDtos.add(userDto);
            }
            return userDtos;
        });

        when(userService.addUser(any(UserDto.class))).thenAnswer(arguments -> {
            UserDto userDto = arguments.getArgument(0);
            userDto.setId(1L);
            return userDto;
        });

        when(userService.updateUser(anyLong(), any(UserDto.class))).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            UserDto userDto = arguments.getArgument(1);
            userDto.setId(userId);
            return userDto;
        });

        when(userService.deleteUser(anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            return getUserDto(userId);
        });

    }

    @Test
    void getUser() {
        Long userId = getRandomKey();

        try {
            mockMvc.perform(get("/users/{itemId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getUsers() {
        try {
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(5));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void addUser() {
        UserDto userDto = getUserDto(null);

        try {
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void updateUser() {
        UserDto userDto = getUserDto(null);
        Long userId = getRandomKey();

        try {
            mockMvc.perform(patch("/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDto)))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void deleteUser() {
        Long userId = getRandomKey();
        try {
            mockMvc.perform(delete("/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId));
        } catch (Exception e) {
            fail(e.getMessage());
        }
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

    private UserDto getUserDto(Long userId) {
        return UserDto.builder()
                .id(userId)
                .name("name")
                .email("email@email.com")
                .build();
    }
}