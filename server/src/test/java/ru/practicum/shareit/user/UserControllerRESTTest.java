package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class UserControllerRESTTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 10; i++) {
            UserDto userDto = getUserDto(null);
            try {
                mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    void getUser() {
        try {
            mockMvc.perform(get("/users/{itemId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L));
            mockMvc.perform(get("/users/{itemId}", 6L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(6L));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getUsers() {
        try {
            mockMvc.perform(get("/users"))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(10));
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
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(11L));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void updateUser() {
        UserDto userDto = getUserDto(null);
        Long userId = 3L;
        try {
            mockMvc.perform(patch("/users/{id}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDto)))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(userDto.getName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(userDto.getEmail()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void deleteUser() {
        Long userId = 8L;
        try {
            mockMvc.perform(delete("/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userId));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private UserDto getUserDto(Long userId) {
        return UserDto.builder()
                .id(userId)
                .name("name" + userId)
                .email("email" + LocalDateTime.now().getNano() + "@email.com")
                .build();
    }
}