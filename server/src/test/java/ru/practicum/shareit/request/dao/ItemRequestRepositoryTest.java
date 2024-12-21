package ru.practicum.shareit.request.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestRepositoryTest {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Test
    void findAllByCreatorId() {
        User user1 = getUser(1L);
        userRepository.save(user1);
        User user2 = getUser(2L);
        userRepository.save(user2);

        ItemRequest request1 = getItemRequest(1L, user1);
        ItemRequest request2 = getItemRequest(2L, user1);
        ItemRequest request3 = getItemRequest(3L, user2);
        ItemRequest request4 = getItemRequest(4L, user2);
        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);
        itemRequestRepository.save(request3);
        itemRequestRepository.save(request4);

        List<ItemRequest> requestsForUser1 = itemRequestRepository.findAllByCreatorId(1L);
        List<ItemRequest> requestsForUser2 = itemRequestRepository.findAllByCreatorId(2L);
        assertEquals(2, requestsForUser1.size());
        assertEquals(2, requestsForUser2.size());
        assertTrue(requestsForUser1.stream().allMatch(request -> request.getCreator().getId().equals(1L)));
        assertTrue(requestsForUser2.stream().allMatch(request -> request.getCreator().getId().equals(2L)));
    }

    @Test
    void findAllByNotCreatorId() {
        User user1 = getUser(1L);
        userRepository.save(user1);
        User user2 = getUser(2L);
        userRepository.save(user2);
        User user3 = getUser(3L);
        userRepository.save(user3);

        ItemRequest request1 = getItemRequest(1L, user1);
        ItemRequest request2 = getItemRequest(2L, user1);
        ItemRequest request3 = getItemRequest(3L, user2);
        ItemRequest request4 = getItemRequest(4L, user2);
        ItemRequest request5 = getItemRequest(5L, user2);
        ItemRequest request6 = getItemRequest(6L, user2);
        ItemRequest request7 = getItemRequest(7L, user3);
        ItemRequest request8 = getItemRequest(8L, user3);
        ItemRequest request9 = getItemRequest(9L, user3);
        itemRequestRepository.save(request1);
        itemRequestRepository.save(request2);
        itemRequestRepository.save(request3);
        itemRequestRepository.save(request4);
        itemRequestRepository.save(request5);
        itemRequestRepository.save(request6);
        itemRequestRepository.save(request7);
        itemRequestRepository.save(request8);
        itemRequestRepository.save(request9);

        Long userIdToExclude = 1L;
        List<ItemRequest> requests = itemRequestRepository.findAllByNotCreatorId(userIdToExclude, 0L, 2L);
        assertEquals(2, requests.size());
        assertTrue(requests.stream().noneMatch(request -> request.getCreator().getId().equals(userIdToExclude)));

        requests = itemRequestRepository.findAllByNotCreatorId(userIdToExclude, 1L, 5L);
        assertEquals(5, requests.size());
        assertTrue(requests.stream().noneMatch(request -> request.getCreator().getId().equals(userIdToExclude)));

        requests = itemRequestRepository.findAllByNotCreatorId(userIdToExclude, 4L, 20L);
        // assertEquals(4, requests.size());
        assertTrue(requests.stream().noneMatch(request -> request.getCreator().getId().equals(userIdToExclude)));
    }

    private ItemRequest getItemRequest(Long requestId, User author) {
        return ItemRequest.builder()
                .id(requestId)
                .description("description" + requestId)
                .created(LocalDateTime.now())
                .creator(author)
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