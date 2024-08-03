package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemClient itemClient;

    public ItemDto getItem(Long itemId, Long userId) {
        log.info("GATEWAY: получен запрос на получение вещи.");
        ResponseEntity<Object> response = itemClient.getItem(userId, itemId);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ItemDto itemDto = (ItemDto) response.getBody();
            log.info("GATEWAY: обработан запрос на получение вещи.");
            return itemDto;
        }
        return null; // !!!!!
    }

    public List<ItemDto> getItemsOwned(Long userId) {
        log.info("GATEWAY: получен запрос на получение вещей владельца.");
        ResponseEntity<Object> response = itemClient.getItems(userId);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<ItemDto> itemDtos = (List<ItemDto>) response.getBody();
            log.info("GATEWAY: обработан запрос на получение вещей владельца.");
            return itemDtos;
        }
        return null; // !!!!!
    }

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("GATEWAY: получен запрос на создание вещи.");
        ResponseEntity<Object> response = itemClient.createItem(userId, itemDto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ItemDto itemDtoCreated = (ItemDto) response.getBody();
            log.info("GATEWAY: обработан запрос на создание вещи.");
            return itemDtoCreated;
        }
        return null; // !!!!!
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("GATEWAY: получен запрос на обновление веши.");
        ResponseEntity<Object> response = itemClient.updateItem(userId, itemId, itemDto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ItemDto itemDtoUpdated = (ItemDto) response.getBody();
            log.info("GATEWAY: обработан запрос на обновление вещи.");
            return itemDtoUpdated;
        }
        return null; // !!!!!
    }

    public List<ItemDto> searchItemsByText(Long userId, String text) {
        log.info("GATEWAY: получен запрос на поиск вещей по тексту.");
        ResponseEntity<Object> response = itemClient.findByText(userId, text);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<ItemDto> itemDtos = (List<ItemDto>) response.getBody();
            log.info("GATEWAY: обработан запрос на поиск вещей по тексту.");
            return itemDtos;
        }
        return null; // !!!!!
    }

    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {
        log.info("GATEWAY: получен запрос на добавление комментария к вещи.");
        ResponseEntity<Object> response = itemClient.addComment(userId, itemId, commentDto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            CommentDto comment = (CommentDto) response.getBody();
            log.info("GATEWAY: обработан запрос на добавление комментария к вещи.");
            return comment;
        }
        return null; // !!!!!
    }
}
