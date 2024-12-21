package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader("X-Sharer-User-Id") Long userRenterId,
                                            @Valid @RequestBody final ItemRequestDto itemRequestDto) {
        return itemRequestService.createItemRequest(userRenterId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestsFromUser(@RequestHeader("X-Sharer-User-Id") Long userRenterId) {
        return itemRequestService.getItemRequestsFromUser(userRenterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequestsFromOtherUsers(
            @RequestHeader("X-Sharer-User-Id") Long userRenterId,
            @RequestParam @Positive(message = "Параметр from должен быть положительным.") Long from,
            @RequestParam @Positive(message = "Параметр size должен быть положительным.") Long size) {
        return itemRequestService.getAllItemRequestsFromOtherUsers(userRenterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@RequestHeader("X-Sharer-User-Id") Long userRenterId,
                                         @PathVariable Long requestId) {
        return itemRequestService.getItemRequest(userRenterId, requestId);
    }


}
