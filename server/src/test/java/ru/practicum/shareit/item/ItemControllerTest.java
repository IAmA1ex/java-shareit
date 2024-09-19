package ru.practicum.shareit.item;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class ItemControllerTest {

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
            try {
                ItemDto itemDto = getItemDto(null);
                mockMvc.perform(post("/items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemDto))
                                .header("X-Sharer-User-Id", 1L));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    void getItem() {
        try {
            mockMvc.perform(get("/items/{itemId}", 2L)
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(2L));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    void getItems() {
        try {
            mockMvc.perform(get("/items")
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(10));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void createItem() {
        ItemDto itemDto = getItemDto(null);
        try {
            mockMvc.perform(post("/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(11L));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void updateItem() {
        ItemDto itemDto = getItemDto(null);
        String update = "updated description";
        itemDto.setDescription(update);
        try {
            mockMvc.perform(patch("/items/{itemId}", 4L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(4L))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(update));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void searchItems() {
        UserDto userDto = getUserDto(null);
        try {
            mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        for (int i = 0; i < 5; i++) {
            try {
                ItemDto itemDto = getItemDto(null);
                itemDto.setDescription("hjvjhSeArChyg687g");
                mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2L));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        for (int i = 0; i < 5; i++) {
            try {
                ItemDto itemDto = getItemDto(null);
                itemDto.setDescription("fgjujbhnjoml");
                mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 2L));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        try {
            mockMvc.perform(get("/items/search")
                            .param("text", "search")
                            .header("X-Sharer-User-Id", 1L))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(5));
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

    private ItemDto getItemDto(Long itemDtoId) {
        LocalDateTime localDateTime = LocalDateTime.now();
        return ItemDto.builder()
                .id(itemDtoId)
                .name("name" + localDateTime)
                .description("description" + localDateTime)
                .available(Boolean.TRUE)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .requestId(null)
                .build();
    }
}