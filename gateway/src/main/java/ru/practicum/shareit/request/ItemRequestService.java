package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.CommentRepository;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final MapperItemRequestDto mapperItemRequestDto;
    private final MapperItemDto mapperItemDto;
    private final CommentRepository commentRepository;
    private final MapperCommentDto mapperCommentDto;


    public ItemRequestDto createItemRequest(Long userRenterId, ItemRequestDto itemRequestDto) {
        log.info("Запрос (userId = {}) на создание запроса.", userRenterId);
        User user = userRepository.findById(userRenterId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userRenterId + " не существует."));
        ItemRequest itemRequest = mapperItemRequestDto.toItemRequest(itemRequestDto);
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setCreator(user);
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        ItemRequestDto result = mapperItemRequestDto.toItemRequestDto(savedItemRequest);
        log.info("Создан запрос c id = {}.", savedItemRequest.getId());
        return result;
    }

    public List<ItemRequestDto> getItemRequestsFromUser(Long userRenterId) {
        log.info("Запрос (userId = {}) на получение созданных запросов.", userRenterId);
        if (!userRepository.existsById(userRenterId)) {
            throw new NotFoundException("Пользователь с id = " + userRenterId + " не существует.");
        }
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByCreatorId(userRenterId);
        List<ItemRequestDto> itemRequestsDto = itemRequests.stream()
                .map(mapperItemRequestDto::toItemRequestDto)
                .peek(this::fillItemsAndCommentsInItemRequestDto)
                .toList();
        log.info("Найдено {} запросов.", itemRequests.size());
        return itemRequestsDto;
    }

    public List<ItemRequestDto> getAllItemRequestsFromOtherUsers(Long userRenterId, Long from, Long size) {
        log.info("Запрос (userId = {}) на получение запросов других пользователей.", userRenterId);
        if (!userRepository.existsById(userRenterId)) {
            throw new NotFoundException("Пользователь с id = " + userRenterId + " не существует.");
        }
        if (userRepository.count() < from) {
            throw new NotFoundException("Страница запросов с " + from + " по " + (from + size) + " не существует.");
        }
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByNotCreatorId(userRenterId, from, size);
        List<ItemRequestDto> itemRequestsDto = itemRequests.stream()
                .map(mapperItemRequestDto::toItemRequestDto)
                .peek(this::fillItemsAndCommentsInItemRequestDto)
                .toList();
        log.info("Найдено {} запросов.", itemRequests.size());
        return itemRequestsDto;
    }

    public ItemRequestDto getItemRequest(Long requestId) {
        log.info("Запрос на получение запроса с id = {}.", requestId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос с id = " + requestId + " не существует."));
        ItemRequestDto itemRequestDto = mapperItemRequestDto.toItemRequestDto(itemRequest);
        fillItemsAndCommentsInItemRequestDto(itemRequestDto);
        log.info("Получен запрос {}.", itemRequestDto);
        return itemRequestDto;
    }

    private void fillItemsAndCommentsInItemRequestDto(ItemRequestDto itemRequestDto) {
        List<Item> items = itemRepository.findAllByRequestId(itemRequestDto.getId());
        List<ItemDto> itemDtos = items.stream()
                .map(mapperItemDto::toItemDto)
                .peek(itemDto -> {
                    List<Comment> comments = commentRepository.findAllByItemId(itemDto.getId());
                    List<CommentDto> commentDtos = comments.stream()
                            .map(mapperCommentDto::toCommentDto)
                            .toList();
                    itemDto.setComments(commentDtos);
                })
                .toList();
        itemRequestDto.setItems(itemDtos);
    }
}
