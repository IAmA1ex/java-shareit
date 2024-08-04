package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerRESTTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;

    @BeforeEach
    void setUp() {

        usedIds = new ArrayList<>();

        when(itemRequestService.createItemRequest(anyLong(), any(ItemRequestDto.class))).thenAnswer(arguments -> {
           Long userId = arguments.getArgument(0);
           ItemRequestDto requestDto = arguments.getArgument(1);
           requestDto.setId(getRandomKey());
           requestDto.setDescription("description" + userId);
           return requestDto;
        });

        when(itemRequestService.getItemRequestsFromUser(anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            List<ItemRequestDto> requestDtos = new ArrayList<>();
            for (long i = 0L; i < 3; i++) {
                ItemRequestDto requestDto = getItemRequestDto(getRandomKey());
                requestDto.setDescription("description" + userId);
                requestDtos.add(requestDto);
            }
            return requestDtos;
        });

        when(itemRequestService.getAllItemRequestsFromOtherUsers(anyLong(), anyLong(), anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            Long from  = arguments.getArgument(1);
            Long size = arguments.getArgument(2);
            List<ItemRequestDto> requestDtos = new ArrayList<>();
            for (Long i = from; i < from + size; i++) {
                ItemRequestDto itemRequestDto = getItemRequestDto(getRandomKey());
                itemRequestDto.setDescription("description" + userId);
                requestDtos.add(itemRequestDto);
            }
            return requestDtos;
        });

        when(itemRequestService.getItemRequest(anyLong(), anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            Long requestId = arguments.getArgument(1);
            ItemRequestDto requestDto = getItemRequestDto(requestId);
            requestDto.setDescription("description" + userId);
            return requestDto;
        });

    }

    @Test
    void createItemRequest() {
        Long userId = getRandomKey();
        ItemRequestDto requestDto = getItemRequestDto(null);

        try {
            mockMvc.perform(post("/requests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto))
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description", containsString(userId.toString())));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getItemRequestsFromUser() {
        Long userId = getRandomKey();

        try {
            mockMvc.perform(get("/requests")
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(3))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].description", containsString(userId.toString())));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getAllItemRequestsFromOtherUsers() {
        Long from = 90L;
        Long size = 5L;
        Long userId = getRandomKey();

        try {
            mockMvc.perform(get("/requests/all")
                            .param("from", from.toString())
                            .param("size", size.toString())
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(size.intValue()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[*].description", everyItem(containsString(userId.toString()))));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getItemRequest() {
        Long userId = getRandomKey();
        Long requestId = getRandomKey();

        try {
            mockMvc.perform(get("/requests/{requestId}", requestId)
                            .header("X-Sharer-User-Id", userId))
                    .andExpect(status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(requestId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description", containsString(userId.toString())));
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

    private ItemRequestDto getItemRequestDto(Long itemRequestId) {
        return ItemRequestDto.builder()
                .id(itemRequestId)
                .description("description")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();
    }
}