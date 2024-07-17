package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.MapperCommentDto;
import ru.practicum.shareit.item.dto.mapper.MapperItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ObjectValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ObjectValidator<ItemDto> itemValidator;
    private final MapperItemDto mapperItemDto;
    private final BookingRepository bookingRepository;
    private final BookingDtoMapper bookingDtoMapper;
    private final CommentRepository commentRepository;
    private final MapperCommentDto mapperCommentDto;

    public ItemDto getItem(Long itemId, Long userId) {
        log.info("Запрос (userId = {}) на получение вещи с id = {}.", userId, itemId);
        Optional<Item> opt = itemRepository.findById(itemId);
        Item item = opt.orElse(null);
        if (item == null) {
            throw new NotFoundException("Вещь с id = " + itemId + " не существует.");
        }
        ItemDto itemDto = mapperItemDto.toItemDto(item);
        if (item.getOwner().getId().equals(userId)) {
            fillItemsDtoByBookingsDto(itemDto, userId);
        }
        fillItemsDtoByCommentsDto(itemDto);
        log.info("Получена вещь {}.", item);
        return itemDto;
    }

    public List<ItemDto> getItemsOwned(Long userId) {
        log.info("Запрос на получение вещей с owner = {}.", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        List<Item> items = itemRepository.findItemsByOwnerId(userId);
        List<ItemDto> itemDtos = items.stream().map(mapperItemDto::toItemDto).toList();
        for (ItemDto itemDto : itemDtos) {
            fillItemsDtoByBookingsDto(itemDto, userId);
            fillItemsDtoByCommentsDto(itemDto);
        }
        log.info("Получено {} вещей.", itemDtos.size());
        return itemDtos;
    }

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Запрос на создание вещи {} с owner = {}.", itemDto, userId);
        Optional<User> optRenter = userRepository.findById(userId);
        if (optRenter.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        User user = optRenter.get();
        Item item = mapperItemDto.fromItemDto(itemDto);
        item.setOwner(user);
        Item created = itemRepository.save(item);
        log.info("Создана вещь {}.", created);
        return mapperItemDto.toItemDto(created);
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Запрос на обновление веши с itemId = {}, пользователем с userId = {}, полями {}.", itemId, userId, itemDto);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        Optional<Item> opt = itemRepository.findById(itemId);
        Item item = opt.orElse(null);
        if (item == null) {
            throw new NotFoundException("Вещь с id = " + itemId + " не существует.");
        }
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь с id = " + userId + " не владеет этой вещью с id = "
                    + itemId + ".");
        }
        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());
        itemValidator.validateObject(mapperItemDto.toItemDto(item));
        Item updated = itemRepository.save(item);
        ItemDto itemDtoUpdsted = mapperItemDto.toItemDto(updated);
        if (item.getOwner().getId().equals(userId)) {
            fillItemsDtoByBookingsDto(itemDtoUpdsted, userId);
        }
        fillItemsDtoByCommentsDto(itemDtoUpdsted);
        log.info("Вещь обновлена {}.", updated);
        return itemDtoUpdsted;
    }

    public List<ItemDto> searchItemsByText(Long userId, String text) {
        log.info("Запрос на поиск вещей по тексту '{}'.", text);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        if (text.isBlank()) {
            log.info("Найдено 0 вещей. Пустой текст.");
            return List.of();
        }
        List<Item> items = itemRepository.findItemsBySubstring(text.toLowerCase());
        List<ItemDto> itemDtos = items.stream().map(mapperItemDto::toItemDto).toList();
        for (ItemDto itemDto : itemDtos) {
            fillItemsDtoByBookingsDto(itemDto, userId);
            fillItemsDtoByCommentsDto(itemDto);
        }
        log.info("Найдено {} вещей.", itemDtos.size());
        return itemDtos;
    }

    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        LocalDateTime commentTime = LocalDateTime.now();
        Optional<User> optRenter = userRepository.findById(userId);
        if (optRenter.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        User user = optRenter.get();
        Optional<Item> optItem = itemRepository.findById(itemId);
        if (optItem.isEmpty()) {
            throw new NotFoundException("Вещь с id = " + itemId + " не существует.");
        }
        Item item = optItem.get();
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(user)
                .created(commentTime)
                .build();
        validateCommentTimeAndBooking(comment);
        Comment savedComment = commentRepository.save(comment);
        return mapperCommentDto.toCommentDto(savedComment);
    }

    private void fillItemsDtoByBookingsDto(ItemDto itemDto, Long userId) {
        Booking last = bookingRepository.getLastBooking(userId, itemDto.getId());
        if (last != null) itemDto.setLastBooking(bookingDtoMapper.toBookingForItem(last));
        Booking next = bookingRepository.getNextBooking(userId, itemDto.getId());
        if (last != null) itemDto.setNextBooking(bookingDtoMapper.toBookingForItem(next));
    }

    private void fillItemsDtoByCommentsDto(ItemDto itemDto) {
        List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId());
        List<CommentDto> commentDtos = comments.stream()
                .map(mapperCommentDto::toCommentDto)
                .toList();
        itemDto.setComments(commentDtos);
    }

    private void validateCommentTimeAndBooking(Comment comment) {
        List<Booking> bookings = bookingRepository.isUserContainsCompletedBookingForItem(comment.getAuthor().getId(),
                comment.getItem().getId(), comment.getCreated());
        if (bookings.isEmpty()) {
            throw new BadRequestException("Нельзя создать отзыв на предмет который не был арендован.");
        }
    }
}
