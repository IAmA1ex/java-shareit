package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CommentRepositoryTest {

    @Autowired
    private final CommentRepository commentRepository;

    @Autowired
    private final ItemRepository itemRepository;

    @Autowired
    private final UserRepository userRepository;

    @Test
    void findAllByItemId() {
        User user1 = getUser(1L);
        userRepository.save(user1);
        User user2 = getUser(2L);
        userRepository.save(user2);
        User user3 = getUser(3L);
        userRepository.save(user3);

        Item item1 = getItem(1L, user1);
        Item item2 = getItem(2L, user2);
        itemRepository.save(item1);
        itemRepository.save(item2);

        Comment comment1 = getComment(null, item1, user2);
        Comment comment2 = getComment(null, item1, user3);
        Comment comment3 = getComment(null, item2, user1);
        Comment comment4 = getComment(null, item2, user3);
        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);
        commentRepository.save(comment4);

        List<Comment> commentsForItem1 = commentRepository.findAllByItemId(1L);
        List<Comment> commentsForItem2 = commentRepository.findAllByItemId(2L);
        assertEquals(2, commentsForItem1.size());
        assertEquals(2, commentsForItem2.size());
        assertTrue(commentsForItem1.stream().allMatch(comment -> comment.getItem().getId().equals(1L)));
        assertTrue(commentsForItem2.stream().allMatch(comment -> comment.getItem().getId().equals(2L)));
    }

    private Comment getComment(Long id, Item item, User author) {
        return Comment.builder()
                .id(id)
                .text("text" + id)
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }

    private Item getItem(Long itemId, User owner) {
        return Item.builder()
                .id(itemId)
                .name("name" + itemId)
                .description("description" + itemId)
                .available(Boolean.TRUE)
                .owner(owner)
                .request(null)
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