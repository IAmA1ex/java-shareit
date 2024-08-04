package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.ResponseHandler;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemClient itemClient;
    private final ResponseHandler responseHandler;

    public ItemDto getItem(Long itemId, Long userId) {
        log.info("GATEWAY: получен запрос на получение вещи.");
        ResponseEntity<Object> response = itemClient.getItem(userId, itemId);
        ItemDto itemDto = responseHandler.handleResponse(response, new TypeReference<ItemDto>(){});
        log.info("GATEWAY: обработан запрос на получение вещи.");
        return itemDto;
    }

    public List<ItemDto> getItemsOwned(Long userId) {
        log.info("GATEWAY: получен запрос на получение вещей владельца.");
        ResponseEntity<Object> response = itemClient.getItems(userId);
        List<ItemDto> itemDtos = responseHandler.handleResponse(response, new TypeReference<List<ItemDto>>(){});
        log.info("GATEWAY: обработан запрос на получение вещей владельца.");
        return itemDtos;
    }

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("GATEWAY: получен запрос на создание вещи.");
        ResponseEntity<Object> response = itemClient.createItem(userId, itemDto);
        ItemDto itemDtoCreated = responseHandler.handleResponse(response, new TypeReference<ItemDto>(){});
        log.info("GATEWAY: обработан запрос на создание вещи.");
        return itemDtoCreated;
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("GATEWAY: получен запрос на обновление веши.");
        ResponseEntity<Object> response = itemClient.updateItem(userId, itemId, itemDto);
        ItemDto itemDtoUpdated = responseHandler.handleResponse(response, new TypeReference<ItemDto>(){});
        log.info("GATEWAY: обработан запрос на обновление вещи.");
        return itemDtoUpdated;
    }

    public List<ItemDto> searchItemsByText(Long userId, String text) {
        log.info("GATEWAY: получен запрос на поиск вещей по тексту.");
        ResponseEntity<Object> response = itemClient.findByText(userId, text);
        List<ItemDto> itemDtos = responseHandler.handleResponse(response, new TypeReference<List<ItemDto>>(){});
        log.info("GATEWAY: обработан запрос на поиск вещей по тексту.");
        return itemDtos;
    }

    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        log.info("GATEWAY: получен запрос на добавление комментария к вещи.");
        ResponseEntity<Object> response = itemClient.addComment(userId, itemId, commentDto);
        CommentDto comment = responseHandler.handleResponse(response, new TypeReference<CommentDto>(){});
        log.info("GATEWAY: обработан запрос на добавление комментария к вещи.");
        return comment;
    }
}
