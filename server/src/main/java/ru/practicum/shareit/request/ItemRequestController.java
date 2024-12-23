package ru.practicum.shareit.request;

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
                                            @RequestBody final ItemRequestDto itemRequestDto) {
        return itemRequestService.createItemRequest(userRenterId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequestsFromUser(@RequestHeader("X-Sharer-User-Id") Long userRenterId) {
        return itemRequestService.getItemRequestsFromUser(userRenterId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequestsFromOtherUsers(@RequestHeader("X-Sharer-User-Id") Long userRenterId,
                                                                 @RequestParam Long from,
                                                                 @RequestParam Long size) {
        return itemRequestService.getAllItemRequestsFromOtherUsers(userRenterId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@PathVariable Long requestId) {
        return itemRequestService.getItemRequest(requestId);
    }


}
