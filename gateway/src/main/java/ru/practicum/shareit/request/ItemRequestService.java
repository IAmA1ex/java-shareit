package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestService {

    private final ItemRequestClient itemRequestClient;

    public ItemRequestDto createItemRequest(Long userRenterId, ItemRequestDto itemRequestDto) {
        log.info("GATEWAY: поучен запрос на создание запроса.");
        ResponseEntity<Object> response = itemRequestClient.createItemRequest(userRenterId, itemRequestDto);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ItemRequestDto itemRequestDtoCreated = (ItemRequestDto) response.getBody();
            log.info("GATEWAY: обработан запрос на создание запроса.");
            return itemRequestDtoCreated;
        }
        return null; // !!!!!
    }

    public List<ItemRequestDto> getItemRequestsFromUser(Long userRenterId) {
        log.info("GATEWAY: поучен запрос на получение созданных запросов.");
        ResponseEntity<Object> response = itemRequestClient.getItemRequestsFromUser(userRenterId);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<ItemRequestDto> itemRequestDtos = (List<ItemRequestDto>) response.getBody();
            log.info("GATEWAY: обработан запрос на получение созданных запросов.");
            return itemRequestDtos;
        }
        return null; // !!!!!
    }

    public List<ItemRequestDto> getAllItemRequestsFromOtherUsers(Long userRenterId, Long from, Long size) {
        log.info("GATEWAY: поучен запрос на получение запросов других пользователей.");
        ResponseEntity<Object> response = itemRequestClient.getAllItemRequestsFromOtherUsers(userRenterId, from, size);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<ItemRequestDto> itemRequestDtos = (List<ItemRequestDto>) response.getBody();
            log.info("GATEWAY: обработан запрос на получение запросов других пользователей.");
            return itemRequestDtos;
        }
        return null; // !!!!!
    }

    public ItemRequestDto getItemRequest(Long userRenterId, Long requestId) {
        log.info("GATEWAY: поучен запрос на получение запроса.");
        ResponseEntity<Object> response = itemRequestClient.getItemRequest(userRenterId, requestId);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            ItemRequestDto itemRequestDto = (ItemRequestDto) response.getBody();
            log.info("GATEWAY: обработан запрос на получение запроса.");
            return itemRequestDto;
        }
        return null; // !!!!!
    }

}
