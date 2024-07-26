package ru.practicum.shareit.request;

import jakarta.validation.Valid;
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
    public List<ItemRequestDto> getAllItemRequestsFromOtherUsers(@RequestHeader("X-Sharer-User-Id") Long userRenterId,
                                                                 @RequestParam Long from,
                                                                 @RequestParam Long size) {
        return itemRequestService.getAllItemRequestsFromOtherUsers(userRenterId, from, size);
    }

    @GetMapping
    public ItemRequestDto getItemRequest(@RequestParam Long requestId) {
        return itemRequestService.getItemRequest(requestId);
    }


}
