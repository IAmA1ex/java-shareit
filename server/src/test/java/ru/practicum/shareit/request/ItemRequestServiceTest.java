package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.mapper.MapperCommentDto;
import ru.practicum.shareit.item.dto.mapper.MapperItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.mapper.MapperItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceTest {

    private ItemRequestRepository itemRequestRepository;
    private UserRepository userRepository;
    private ItemRepository itemRepository;
    private MapperItemRequestDto mapperItemRequestDto;
    private MapperItemDto mapperItemDto;
    private CommentRepository commentRepository;
    private MapperCommentDto mapperCommentDto;
    private ItemRequestService itemRequestService;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;

    @BeforeEach
    void setUp() {
        itemRequestRepository = mock(ItemRequestRepository.class);
        userRepository = mock(UserRepository.class);
        itemRepository = mock(ItemRepository.class);
        commentRepository = mock(CommentRepository.class);
        mapperItemRequestDto = new MapperItemRequestDto();
        mapperItemDto = new MapperItemDto();
        mapperCommentDto = new MapperCommentDto();
        itemRequestService = new ItemRequestService(itemRequestRepository, userRepository, itemRepository,
                mapperItemRequestDto, mapperItemDto, commentRepository, mapperCommentDto);

        usedIds = new ArrayList<>();

        when(userRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxId) return Optional.of(getUser(id));
            return Optional.empty();
        });

        when(userRepository.existsById(anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            return userId > reservedId && userId <= maxId;
        });

        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(arguments -> {
            ItemRequest itemRequest = arguments.getArgument(0);
            itemRequest.setId(1L);
            return itemRequest;
        });

        when(itemRequestRepository.findAllByCreatorId(anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            List<ItemRequest> itemRequests = new ArrayList<>();
            for (long i = 1L; i < 3L; i++) {
                itemRequests.add(getItemRequest(i, userId));
            }
            return itemRequests;
        });

        when(itemRepository.findAllByRequestId(anyLong())).thenAnswer(arguments -> {
            Long requestId = arguments.getArgument(0);
            List<Item> items = new ArrayList<>();
            items.add(getItem(requestId, 5L, null, null));
            return items;
        });

        when(commentRepository.findAllByItemId(anyLong())).thenAnswer(arguments -> {
           Long itemId = arguments.getArgument(0);
           List<Comment> comments = new ArrayList<>();
           User user = getUser(33L);
           user.setName("commentAuthor" + itemId);
           comments.add(getComment(itemId, null, user));
           return comments;
        });

        when(userRepository.count()).thenReturn(maxId);

        when(itemRequestRepository.findAllByNotCreatorId(anyLong(), anyLong(), anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            Long from  = arguments.getArgument(1);
            Long size = arguments.getArgument(2);
            long maxReturnCount = 5L; // только тут
            long count = Math.min(Math.min(size, maxReturnCount), (maxId - from + 1));
            List<ItemRequest> itemRequests = new ArrayList<>();
            for (long i = 0; i < count; i++) {
                Long id = from + i;
                itemRequests.add(getItemRequest(id, 23L));
            }
            return itemRequests;
        });

        when(itemRequestRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long requestId = arguments.getArgument(0);
            if (requestId > 0 && requestId <= maxId) return Optional.of(getItemRequest(requestId, requestId));
            return Optional.empty();
        });
    }


    @Test
    void createItemRequest() {

        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        ItemRequestDto itemRequestDto = getItemRequestDto(null);

        // Запрос на создание с неправильным пользователем
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemRequestService.createItemRequest(fakeUserId, itemRequestDto);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Запрос на создание с нормальным пользователем
        ItemRequestDto itemRequestDtoCreated = itemRequestService.createItemRequest(realUserId, itemRequestDto);
        assertNotNull(itemRequestDtoCreated);
        assertEquals(1L, itemRequestDtoCreated.getId());
    }

    @Test
    void getItemRequestsFromUser() {

        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        // Запрос на получение с неправильным пользователем
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getItemRequestsFromUser(fakeUserId);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Запрос на получение с нормальным пользователем
        List<ItemRequestDto> itemRequestDtos = itemRequestService.getItemRequestsFromUser(realUserId);
        assertNotNull(itemRequestDtos);
        assertFalse(itemRequestDtos.isEmpty());
        assertNotNull(itemRequestDtos.getFirst().getItems());
        assertFalse(itemRequestDtos.getFirst().getItems().isEmpty());
        assertNotNull(itemRequestDtos.getFirst().getItems().getFirst().getComments());
        assertFalse(itemRequestDtos.getFirst().getItems().getFirst().getComments().isEmpty());

    }

    @Test
    void getAllItemRequestsFromOtherUsers() {

        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        Long incorrectFrom = maxId + 1;
        Long from = maxId - 10;
        Long size = 3L;

        // Запрос на получение с неправильным пользователем
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getAllItemRequestsFromOtherUsers(fakeUserId, from, size);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Запрос на получение с неправильной страницей
        NotFoundException exceptionFrom = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getAllItemRequestsFromOtherUsers(realUserId, incorrectFrom, size);
        });
        assertEquals("Страница запросов с " + incorrectFrom +
                " по " + (incorrectFrom + size) + " не существует.",
                exceptionFrom.getMessage());

        // Запрос на получение с нормальной страницей
        List<ItemRequestDto> itemRequestDtos =
                itemRequestService.getAllItemRequestsFromOtherUsers(realUserId, from, size);
        assertNotNull(itemRequestDtos);
        assertFalse(itemRequestDtos.isEmpty());
        assertNotNull(itemRequestDtos.getFirst().getItems());
        assertFalse(itemRequestDtos.getFirst().getItems().isEmpty());
        assertNotNull(itemRequestDtos.getFirst().getItems().getFirst().getComments());
        assertFalse(itemRequestDtos.getFirst().getItems().getFirst().getComments().isEmpty());
    }

    @Test
    void getItemRequest() {

        Long realRequestId = getRandomKey();
        Long fakeRequestId = getFakeKey();

        // Запрос на получение с несуществующим запросом
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemRequestService.getItemRequest(fakeRequestId);
        });
        assertEquals("Запрос с id = " + fakeRequestId + " не существует.", exceptionUser.getMessage());

        ItemRequestDto itemRequestDto = itemRequestService.getItemRequest(realRequestId);
        assertNotNull(itemRequestDto);
        assertEquals(realRequestId, itemRequestDto.getId());
        assertNotNull(itemRequestDto.getItems());
        assertFalse(itemRequestDto.getItems().isEmpty());
        assertNotNull(itemRequestDto.getItems().getFirst().getComments());
        assertFalse(itemRequestDto.getItems().getFirst().getComments().isEmpty());

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

    private ItemRequest getItemRequest(Long itemRequestId, Long creatorId) {
        return ItemRequest.builder()
                .id(itemRequestId)
                .description("description" + itemRequestId)
                .created(LocalDateTime.now())
                .creator(getUser(creatorId))
                .build();
    }

    private Item getItem(Long itemId, Long ownerId, Long requestId, Long requestOwnerId) {
        return Item.builder()
                .id(itemId)
                .name("name" + itemId)
                .description("description" + itemId)
                .available(Boolean.TRUE)
                .owner(getUser(ownerId)
                )
                .request(ItemRequest.builder()
                        .id(requestId)
                        .description("description" + requestId)
                        .created(LocalDateTime.now())
                        .creator(getUser(requestOwnerId))
                        .build()
                )
                .build();
    }

    private Comment getComment(Long commentId, Item item, User author) {
        return Comment.builder()
                .id(commentId)
                .text("text" + commentId)
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }

    private User getUser(Long userId) {
        return new User(userId, "name" + userId, "email" + userId + "@email.com");
    }
}