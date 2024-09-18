package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.MapperCommentDto;
import ru.practicum.shareit.item.dto.mapper.MapperItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceTest {

    private ItemService itemService;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private MapperItemDto mapperItemDto;
    private BookingRepository bookingRepository;
    private BookingDtoMapper bookingDtoMapper;
    private CommentRepository commentRepository;
    private MapperCommentDto mapperCommentDto;
    private ItemRequestRepository itemRequestRepository;

    private Long reservedId = 40L;
    private Long maxId = 100L;
    private List<Long> usedIds;
    private Map<Long, Item> itemMap;

    // для поиска по тексту
    private int count = 0;
    private Long currentUserId = 0L;

    // для проверка комментариев
    private boolean isBooked = false;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        mapperItemDto = new MapperItemDto();
        bookingRepository = mock(BookingRepository.class);
        bookingDtoMapper = new BookingDtoMapper(mapperItemDto);
        commentRepository = mock(CommentRepository.class);
        mapperCommentDto = new MapperCommentDto();
        itemRequestRepository = mock(ItemRequestRepository.class);
        itemService = new ItemService(itemRepository, userRepository, mapperItemDto, bookingRepository,
                bookingDtoMapper, commentRepository, mapperCommentDto, itemRequestRepository);

        usedIds = new ArrayList<>();
        itemMap = new HashMap<>();

        when(itemRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxId) {
                Item item = getItem(id, id, 1L, id + 1L);
                Item itemCopy = getItem(id, id, 1L, id + 1L);
                itemMap.put(id, item);
                return Optional.of(itemCopy);
            }
            return Optional.empty();
        });

        when(itemRepository.findItemsByOwnerId(anyLong())).thenAnswer(arguments -> {
            Long ownerId = arguments.getArgument(0);
            List<Item> items = new ArrayList<>();
            for (long i = 1; i <= 3; i++) {
                Long itemId = ownerId + i;
                items.add(getItem(itemId, itemId, i, itemId + 3 + i));
            }
            return items;
        });

        when(bookingRepository.getLastBooking(anyLong(), anyLong())).thenReturn(
                Booking.builder()
                        .id(1L)
                        .renter(new User(1L, "name1", "email1@email.com"))
                        .startTime(null)
                        .endTime(null)
                        .item(null)
                        .status(null)
                        .build()
        );

        when(bookingRepository.getNextBooking(anyLong(), anyLong(), any())).thenReturn(
                Booking.builder()
                        .id(2L)
                        .renter(new User(2L, "name1", "email1@email.com"))
                        .startTime(null)
                        .endTime(null)
                        .item(null)
                        .status(null)
                        .build());

        when(commentRepository.findAllByItemId(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            List<Comment> comments = new ArrayList<>();
            for (long i = 1; i <= 5; i++) {
                Long commentId = (id - 1) * 5 + i;
                comments.add(Comment.builder()
                        .id(commentId)
                        .text("text" + commentId)
                        .item(null)
                        .author(new User(i, "name" + i, "email" + i + "@email.com"))
                        .created(LocalDateTime.now())
                        .build());
            }
            return comments;
        });

        when(userRepository.existsById(anyLong())).thenAnswer(arguments -> {
            Long userId = arguments.getArgument(0);
            return userId > reservedId && userId <= maxId;
        });

        when(itemRequestRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxId) return Optional.of(getItemRequest(id));
            return Optional.empty();
        });

        when(userRepository.findById(anyLong())).thenAnswer(arguments -> {
            Long id = arguments.getArgument(0);
            if (id > 0 && id <= maxId) return Optional.of(getUser(id));
            return Optional.empty();
        });

        when(itemRepository.save(any())).thenAnswer(arguments -> {
            Item item = arguments.getArgument(0);
            item.setId(item.getOwner().getId());
            if (itemMap.containsKey(item.getId())) {
                itemMap.replace(item.getId(), item);
            } else {
                itemMap.put(item.getId(), item);
            }
            return item;
        });

        when(itemRepository.findItemsBySubstring(anyString())).thenAnswer(arguments -> {
            String substring = arguments.getArgument(0);
            List<Item> items = new ArrayList<>();
            for (long i = 1L; i <= count; i++) {
                Item item = getItem(i, i % 2 == 0 ? currentUserId : i, i, getRandomKey());
                if (i % 2 == 0) item.setName(substring);
                if (i % 3 == 0) item.setDescription(substring);
                items.add(item);
            }
            return items;
        });

        when(bookingRepository.isUserContainsCompletedBookingForItem(anyLong(), anyLong(),
                any(LocalDateTime.class))).thenAnswer(arguments -> {
                    Long authorId = arguments.getArgument(0);
                    Long itemId = arguments.getArgument(1);
                    LocalDateTime time = arguments.getArgument(2);
            if (isBooked) {
                return List.of(
                        Booking.builder()
                            .id(1L)
                            .renter(new User(authorId, "name" + authorId, "email" + authorId + "@email.com"))
                            .startTime(time.minusDays(2))
                            .endTime(time.minusDays(1))
                            .item(getItem(itemId, itemId, null, null))
                            .status(BookingStatus.APPROVED)
                            .build());
            }
            return List.of();
        });

        when(commentRepository.save(any(Comment.class))).thenAnswer(arguments -> {
            Comment comment = arguments.getArgument(0);
            comment.setId(1L);
            return comment;
        });

    }

    @Test
    void getItem() {
        Long realId = getRandomKey();
        Long ownerUserId = realId;
        Long notOwnerUserId = realId + 3;
        Long fakeId = getFakeKey();

        ItemDto itemDtoForOwner = itemService.getItem(realId, ownerUserId);
        ItemDto itemDtoForNotOwner = itemService.getItem(realId, notOwnerUserId);
        NotFoundException exception1 = assertThrows(NotFoundException.class, () -> itemService.getItem(fakeId, ownerUserId));
        NotFoundException exception2 = assertThrows(NotFoundException.class, () -> itemService.getItem(fakeId, notOwnerUserId));

        assertTrue(itemDtoForOwner.getLastBooking() != null && itemDtoForOwner.getNextBooking() != null);
        assertTrue(itemDtoForNotOwner.getLastBooking() == null && itemDtoForNotOwner.getNextBooking() == null);

        assertEquals("Вещь с id = " + fakeId + " не существует.", exception1.getMessage());
        assertEquals(exception1.getMessage(), exception2.getMessage());

        assertEquals(5, itemDtoForOwner.getComments().size());
        assertEquals(itemDtoForOwner.getComments().size(), itemDtoForNotOwner.getComments().size());
    }

    @Test
    void getItemsOwned() {
        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        List<ItemDto> itemDtos = itemService.getItemsOwned(realUserId);
        NotFoundException exception = assertThrows(NotFoundException.class, () -> itemService.getItemsOwned(fakeUserId));

        assertNotNull(itemDtos);
        assertFalse(itemDtos.isEmpty());

        assertTrue(itemDtos.stream().allMatch(i -> i.getLastBooking() != null && i.getNextBooking() != null));

        assertTrue(itemDtos.stream().allMatch(i -> i.getComments() != null && !i.getComments().isEmpty()));

        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exception.getMessage());
    }

    @Test
    void createItem() {
        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        Long realItemRequestId = getRandomKey();
        Long fakeItemRequestId = getFakeKey();

        ItemDto itemDto = getItemDtoForCreate(null);  // нормальная вещь без запроса
        ItemDto itemDtoByRequestOtherUser = getItemDtoForCreate(realItemRequestId);  // вещь с нормальным запросом
        ItemDto itemDtoWithFakeRequestId = getItemDtoForCreate(fakeItemRequestId);  // вещь с несуществующим запросом
        ItemDto itemDtoWithRealUserAsCreator = getItemDtoForCreate(realUserId);  // вещь с нормальным запросом у
        // которого создатель пользователь добавляющий вещь

        // Запрос на создание с неправильным пользователем
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemService.createItem(fakeUserId, itemDto);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Запрос на создание по запросу с несуществующим запросом
        NotFoundException exceptionItemRequest = assertThrows(NotFoundException.class, () -> {
            itemService.createItem(realUserId, itemDtoWithFakeRequestId);
        });
        assertEquals("Запрос с id = " + fakeItemRequestId + " не существует. Вещь не создана.",
                exceptionItemRequest.getMessage());

        // Запрос на создание по личному запросу
        BadRequestException exceptionSameUsers = assertThrows(BadRequestException.class, () -> {
            itemService.createItem(realUserId, itemDtoWithRealUserAsCreator);
        });
        assertEquals("Вещь не может быть добавлена по собственному запросу.", exceptionSameUsers.getMessage());

        // Запрос на успешное создание с запросом
        ItemDto itemDtoByRequestUser = itemService.createItem(realUserId, itemDtoByRequestOtherUser);
        assertNotNull(itemDtoByRequestUser);
        assertEquals(realItemRequestId, itemDtoByRequestUser.getRequestId());
        assertEquals(itemDtoByRequestUser.getId(), realUserId);

        // Запрос на успешное создание без запроса
        ItemDto itemDtoByRequestUserWithoutRequest = itemService.createItem(realUserId, itemDto);
        assertNotNull(itemDtoByRequestUserWithoutRequest);
        assertNull(itemDtoByRequestUserWithoutRequest.getRequestId());
        assertEquals(itemDtoByRequestUserWithoutRequest.getId(), realUserId);

    }

    @Test
    void updateItem() {

        Long realUserId = getRandomKey();
        Long realNotOwnerUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        Long realItemId = realUserId;
        Long fakeItemId = getFakeKey();

        ItemDto itemDto = getItemDtoForCreate(null);

        // Проверка на пользователя
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemService.updateItem(fakeUserId, realItemId, itemDto);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка на вещь
        NotFoundException exceptionItem = assertThrows(NotFoundException.class, () -> {
            itemService.updateItem(realUserId, fakeItemId, itemDto);
        });
        assertEquals("Вещь с id = " + fakeItemId + " не существует.", exceptionItem.getMessage());

        // Проверка на владение
        ForbiddenException exceptionOwner = assertThrows(ForbiddenException.class, () -> {
            itemService.updateItem(realNotOwnerUserId, realItemId, itemDto);
        });
        assertEquals("Пользователь с id = " + realNotOwnerUserId + " не владеет этой вещью с id = "
                + realItemId + ".", exceptionOwner.getMessage());

        // Проверка валидации: name
        ItemDto validNameItemDto = getItemDtoForCreate(null);
        validNameItemDto.setName("");
        ItemDto itemDtoUpdatedValidName = itemService.updateItem(realUserId, realItemId, validNameItemDto);
        assertEquals(itemDtoUpdatedValidName.getId(), realItemId);
        assertNotEquals(itemDtoUpdatedValidName.getName(), validNameItemDto.getName());

        // Проверка валидации: description
        ItemDto validDescriptionItemDto = getItemDtoForCreate(null);
        validDescriptionItemDto.setDescription("");
        ItemDto itemDtoUpdatedValidDescription = itemService.updateItem(realUserId, realItemId,
                validDescriptionItemDto);
        assertEquals(itemDtoUpdatedValidDescription.getId(), realItemId);
        assertNotEquals(itemDtoUpdatedValidDescription.getDescription(), validNameItemDto.getDescription());

        // Проверка успешного обновления: name
        ItemDto itemDtoForUpdate = getItemDtoForCreate(null);
        itemDtoForUpdate.setName("updatedName");
        itemDtoForUpdate.setDescription("updatedDescription");
        itemDtoForUpdate.setAvailable(Boolean.FALSE);
        ItemDto itemDtoAfterUpdate = itemService.updateItem(realUserId, realItemId, itemDtoForUpdate);
        assertNotNull(itemDtoAfterUpdate);
        assertEquals(realItemId, itemDtoAfterUpdate.getId());
        assertEquals("updatedName", itemDtoAfterUpdate.getName());
        assertEquals("updatedDescription", itemDtoAfterUpdate.getDescription());
        assertEquals(Boolean.FALSE, itemDtoAfterUpdate.getAvailable());
        assertNotNull(itemDtoAfterUpdate.getLastBooking());
        assertNotNull(itemDtoAfterUpdate.getNextBooking());
        assertEquals(itemDtoAfterUpdate.getComments().size(), 5);
        assertEquals("updatedName", itemMap.get(realUserId).getName());
        assertEquals("updatedDescription", itemMap.get(realUserId).getDescription());
        assertEquals(Boolean.FALSE, itemMap.get(realUserId).getAvailable());

    }

    @Test
    void searchItemsByText() {

        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();
        String substring = "substring";

        // Проверка на пользователя
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemService.searchItemsByText(fakeUserId, substring);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка пустой строки
        List<ItemDto> itemDtos = itemService.searchItemsByText(realUserId, "");
        assertTrue(itemDtos.isEmpty());

        currentUserId = realUserId;
        count = 10;
        List<ItemDto> itemDtosAfterSearch = itemService.searchItemsByText(realUserId, substring);
        assertEquals(count, itemDtosAfterSearch.size());
        for (ItemDto itemDto : itemDtosAfterSearch) {
            Long id = itemDto.getId();
            if (id % 2 == 0) {
                assertNotNull(itemDto.getNextBooking());
                assertNotNull(itemDto.getLastBooking());
                assertFalse(itemDto.getComments().isEmpty());
                assertEquals(substring, itemDto.getName());
            }
            if (id % 3 == 0) assertEquals(substring, itemDto.getDescription());
        }
    }

    @Test
    void addComment() {
        Long realUserId = getRandomKey();
        Long fakeUserId = getFakeKey();

        Long realItemId = getRandomKey();
        Long fakeItemId = getFakeKey();

        CommentDto commentDto = getCommentDto(1L);

        // Проверка на пользователя
        NotFoundException exceptionUser = assertThrows(NotFoundException.class, () -> {
            itemService.addComment(realItemId, commentDto, fakeUserId);
        });
        assertEquals("Пользователь с id = " + fakeUserId + " не существует.", exceptionUser.getMessage());

        // Проверка на вещь
        NotFoundException exceptionItem = assertThrows(NotFoundException.class, () -> {
            itemService.addComment(fakeItemId, commentDto, realUserId);
        });
        assertEquals("Вещь с id = " + fakeItemId + " не существует.", exceptionItem.getMessage());

        // аренды не было поэтому комментировать нельзя
        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            itemService.addComment(realItemId, commentDto, realUserId);
        });
        assertEquals("Нельзя создать отзыв на предмет который не был арендован.", badRequestException.getMessage());

        // аренда была
        isBooked = true;
        CommentDto commentDtoCreated = itemService.addComment(realItemId, commentDto, realUserId);
        assertNotNull(commentDtoCreated);
        assertEquals(1L, commentDtoCreated.getId());

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

    private ItemRequest getItemRequest(Long requestId) {
        return ItemRequest.builder()
                .id(requestId)
                .description("description" + requestId)
                .created(LocalDateTime.now())
                .creator(getUser(requestId))
                .build();
    }

    private User getUser(Long userId) {
        return new User(userId, "name" + userId, "email" + userId + "@email.com");
    }

    private CommentDto getCommentDto(Long commentId) {
        return new CommentDto(commentId, "comment", "name" + commentId, LocalDateTime.now());
    }
}