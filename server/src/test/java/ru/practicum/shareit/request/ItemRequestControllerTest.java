package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserDto userDto = getUserDto(null);
        try {
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        for (int i = 0; i < 10; i++) {
            try {
                ItemRequestDto requestDto = getItemRequestDto(null);
                mockMvc.perform(post("/requests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                                .header("X-Sharer-User-Id", 1L));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    void createItemRequest() {

        UserDto userDto = getUserDto(null);
        try {
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        ItemRequestDto requestDto = getItemRequestDto(null);
        try {
            mockMvc.perform(post("/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .header("X-Sharer-User-Id", 2L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(11L));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    void getItemRequestsFromUser() {
        try {
            mockMvc.perform(get("/requests")
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(10));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getAllItemRequestsFromOtherUsers() {
        UserDto userDto = getUserDto(null);
        try {
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            mockMvc.perform(get("/requests/all")
                            .param("from", Long.toString(1))
                            .param("size", Long.toString(3))
                            .header("X-Sharer-User-Id", 2L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getItemRequest() {
        setUp();
        try {
            mockMvc.perform(get("/requests/{requestId}", 6L)
                            .header("X-Sharer-User-Id", 2L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(6L));
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

    private ItemRequestDto getItemRequestDto(Long itemRequestId) {
        LocalDateTime localDateTime = LocalDateTime.now();
        return ItemRequestDto.builder()
                .id(itemRequestId)
                .description("description" + localDateTime)
                .created(localDateTime)
                .items(List.of())
                .build();
    }
}