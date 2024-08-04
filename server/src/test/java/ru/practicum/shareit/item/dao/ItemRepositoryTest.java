package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Test
    void findItemsByOwnerId() {
        User user1 = getUser(1L);
        User user2 = getUser(2L);
        userRepository.save(user1);
        userRepository.save(user2);
        Item item1 = getItem(null, 1L, null, null);
        Item item2 = getItem(null, 1L, null, null);
        Item item3 = getItem(null, 2L, null, null);
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        List<Item> items = itemRepository.findItemsByOwnerId(1L);
        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(item -> item.getOwner().getId().equals(1L)));
    }

    @Test
    void findItemsBySubstring() {
        User user1 = getUser(1L);
        userRepository.save(user1);
        User user2 = getUser(2L);
        userRepository.save(user2);
        Item item1 = getItem(null, 1L, null, null);
        item1.setName("Apple Watch");
        item1.setDescription("A smart watch");
        Item item2 = getItem(null, 1L, null, null);
        item2.setName("Smartphone");
        item2.setDescription("A very smart phone");
        Item item3 = getItem(null, 1L, null, null);
        item3.setName("Laptop");
        item3.setDescription("A powerful laptop");
        Item item4 = getItem(null, 1L, null, null);
        item4.setName("Washing Machine");
        item4.setDescription("Cleans clothes");
        Item item5 = getItem(null, 1L, null, null);
        item5.setName("Smart");
        item5.setDescription("Car");
        item5.setAvailable(false);
        Item item6 = getItem(null, 2L, null, null);
        item1.setName("Apple Watch 2");
        item1.setDescription("A smart watch 2");
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);
        itemRepository.save(item5);
        itemRepository.save(item6);
        List<Item> items = itemRepository.findItemsBySubstring("smart");
        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(item -> item.getOwner().getId().equals(1L)));
        assertTrue(items.stream().anyMatch(item -> item.getName().contains("Smartphone")));
        assertTrue(items.stream().anyMatch(item -> item.getDescription().contains("smart watch")));
    }

    @Test
    void findAllByRequestId() {
        User user1 = getUser(1L);
        userRepository.save(user1);
        User user2 = getUser(2L);
        userRepository.save(user2);
        User user3 = getUser(3L);
        userRepository.save(user3);

        ItemRequest request1 = getItemRequest(1L, 3L);
        ItemRequest request2 = getItemRequest(2L, 3L);
        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);

        Item item1 = getItem(null, 1L, 1L, null); // Связан с запросом 1
        Item item2 = getItem(null, 1L, 1L, null); // Связан с запросом 1
        Item item3 = getItem(null, 2L, 2L, null); // Связан с запросом 2
        Item item4 = getItem(null, 2L, 1L, null); // Связан с запросом 1
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        itemRepository.save(item4);

        List<Item> items = itemRepository.findAllByRequestId(1L);
        assertEquals(3, items.size());
        assertTrue(items.stream().allMatch(item -> item.getRequest().getId().equals(1L)));
    }

    private Item getItem(Long itemId, Long ownerId, Long requestId, Long requestOwnerId) {
        return Item.builder()
                .id(itemId)
                .name("name" + itemId)
                .description("description" + itemId)
                .available(Boolean.TRUE)
                .owner(getUser(ownerId))
                .request(requestId == null ? null : getItemRequest(requestId, requestOwnerId))
                .build();
    }

    private ItemRequest getItemRequest(Long requestId, Long requestOwnerId) {
        return ItemRequest.builder()
                .id(requestId)
                .description("description" + requestId)
                .created(LocalDateTime.now())
                .creator(getUser(requestOwnerId))
                .build();
    }

    private User getUser(Long userId) {
        return User.builder()
                .id(userId)
                .name("name" + userId)
                .email("email" + userId + "@email.com")
                .build();
    }
}