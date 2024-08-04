package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createItemRequest(Long userRenterId, ItemRequestDto itemRequestDto) {
        return post("", userRenterId, itemRequestDto);
    }

    public ResponseEntity<Object> getItemRequestsFromUser(Long userRenterId) {
        return get("", userRenterId);
    }

    public ResponseEntity<Object> getAllItemRequestsFromOtherUsers(Long userRenterId, Long from, Long size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return post("/all?from={from}&size={size}", userRenterId, parameters);
    }

    public ResponseEntity<Object> getItemRequest(Long userRenterId, Long requestId) {
        Map<String, Object> parameters = Map.of(
                "requestId", requestId
        );
        return get("/{requestId}", userRenterId, parameters);
    }
}
