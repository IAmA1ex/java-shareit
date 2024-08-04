package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.ResponseHandler;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestService {

    private final ItemRequestClient itemRequestClient;
    private final ResponseHandler responseHandler;

    public ItemRequestDto createItemRequest(Long userRenterId, ItemRequestDto itemRequestDto) {
        log.info("GATEWAY: поучен запрос на создание запроса.");
        ResponseEntity<Object> response = itemRequestClient.createItemRequest(userRenterId, itemRequestDto);
        ItemRequestDto itemRequestDtoCreated = responseHandler.handleResponse(response,
                new TypeReference<ItemRequestDto>(){});
        log.info("GATEWAY: обработан запрос на создание запроса.");
        return itemRequestDtoCreated;
    }

    public List<ItemRequestDto> getItemRequestsFromUser(Long userRenterId) {
        log.info("GATEWAY: поучен запрос на получение созданных запросов.");
        ResponseEntity<Object> response = itemRequestClient.getItemRequestsFromUser(userRenterId);
        List<ItemRequestDto> itemRequestDtos = responseHandler.handleResponse(response,
                new TypeReference<List<ItemRequestDto>>() {});
        log.info("GATEWAY: обработан запрос на получение созданных запросов.");
        return itemRequestDtos;
    }

    public List<ItemRequestDto> getAllItemRequestsFromOtherUsers(Long userRenterId, Long from, Long size) {
        log.info("GATEWAY: поучен запрос на получение запросов других пользователей.");
        ResponseEntity<Object> response = itemRequestClient.getAllItemRequestsFromOtherUsers(userRenterId, from, size);
        List<ItemRequestDto> itemRequestDtos = responseHandler.handleResponse(response,
                new TypeReference<List<ItemRequestDto>>() {});
        log.info("GATEWAY: обработан запрос на получение запросов других пользователей.");
        return itemRequestDtos;
    }

    public ItemRequestDto getItemRequest(Long userRenterId, Long requestId) {
        log.info("GATEWAY: поучен запрос на получение запроса.");
        ResponseEntity<Object> response = itemRequestClient.getItemRequest(userRenterId, requestId);
        ItemRequestDto itemRequestDto = responseHandler.handleResponse(response,
                new TypeReference<ItemRequestDto>(){});
        log.info("GATEWAY: обработан запрос на получение запроса.");
        return itemRequestDto;
    }

}
