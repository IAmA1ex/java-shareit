package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerRESTTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;

    @BeforeEach
    void setUp() {

        usedIds = new ArrayList<>();

        when(itemService.getItem(anyLong(), anyLong())).thenAnswer(arguments -> {
            Long itemId = arguments.getArgument(0);
            Long userId = arguments.getArgument(1);
            return ItemDto.builder()
                    .id(itemId)
                    .name("item name")
                    .description("item description")
                    .available(Boolean.TRUE)
                    .lastBooking(null)
                    .nextBooking(null)
                    .comments(List.of(getCommentDto(getRandomKey()), getCommentDto(getRandomKey())))
                    .requestId(null)
                    .build();
        });

        when(itemService.getItemsOwned(anyLong())).thenAnswer(arguments -> {
            List<ItemDto> itemDtos = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                itemDtos.add(getItemDto(getRandomKey()));
            }
            return itemDtos;
        });

        when(itemService.createItem(anyLong(), any(ItemDto.class))).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            ItemDto itemDto = arguments.getArgument(1);
            itemDto.setId(1L);
            return itemDto;
        });

        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class))).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            Long id = arguments.getArgument(1);
            ItemDto itemDto = arguments.getArgument(2);
            itemDto.setId(id);
            return itemDto;
        });

        when(itemService.searchItemsByText(anyLong(), anyString())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            String text = arguments.getArgument(1);
            List<ItemDto> itemDtos = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                ItemDto itemDto = getItemDto(getRandomKey());
                itemDto.setDescription("TEXT___" + text + "___TEXT");
                itemDtos.add(itemDto);
            }
            return itemDtos;
        });

        when(itemService.addComment(anyLong(), any(CommentDto.class), anyLong())).thenAnswer(arguments -> {
            Long itemId = arguments.getArgument(0);
            CommentDto commentDto = arguments.getArgument(1);
            Long userId = arguments.getArgument(2);
            CommentDto commentDtoNew = getCommentDto(userId);
            commentDtoNew.setText(commentDto.getText());
            return commentDtoNew;
        });
    }

    @Test
    void getItem() {
        Long itemId = getRandomKey();
        Long userId = getRandomKey();

        try {
            mockMvc.perform(get("/items/{itemId}", itemId)
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(itemId));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    void getItems() {
        Long userId = getRandomKey();

        try {
            mockMvc.perform(get("/items")
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void createItem() {
        Long userId = getRandomKey();
        Long itemId = getRandomKey();
        ItemDto itemDto = getItemDto(itemId);

        try {
            mockMvc.perform(post("/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void updateItem() {
        Long userId = getRandomKey();
        Long itemId = getRandomKey();
        ItemDto itemDto = getItemDto(itemId);
        String update = "updated description";
        itemDto.setDescription(update);

        try {
            mockMvc.perform(patch("/items/{itemId}", itemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(itemDto))
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(itemId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(update));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void searchItems() {
        Long userId = getRandomKey();
        String text = "search_text";

        try {
            mockMvc.perform(get("/items/search")
                            .param("text", text)
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[*].description", everyItem(containsString(text))));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    void addComment() {
        Long itemId = getRandomKey();
        String text = "add_comment";
        CommentDto commentDto = CommentDto.builder().text(text).build();
        Long userId = getRandomKey();

        try {
            mockMvc.perform(post("/items/{itemId}/comment", itemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(commentDto))
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.text").value(text))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.authorName", containsString(userId.toString())));
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

    private ItemDto getItemDto(Long itemDtoId) {
        return ItemDto.builder()
                .id(itemDtoId)
                .name("name" + itemDtoId)
                .description("description" + itemDtoId)
                .available(Boolean.TRUE)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .requestId(null)
                .build();
    }

    private ItemDto getItemDtoForCreate(Long requestId) {
        return ItemDto.builder()
                .id(null)
                .name("creatItemName")
                .description("creatItemDescription")
                .available(Boolean.TRUE)
                .lastBooking(null)
                .nextBooking(null)
                .comments(null)
                .requestId(requestId)
                .build();
    }

    private CommentDto getCommentDto(Long commentId) {
        return new CommentDto(commentId, "comment", "name" + commentId, LocalDateTime.now());
    }
}