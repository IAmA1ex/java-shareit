package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.MapperItemDto;
import ru.practicum.shareit.item.model.Item;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final MapperItemDto mapperItemDto;

    public ItemController(ItemService itemService, MapperItemDto mapperItemDto) {
        this.itemService = itemService;
        this.mapperItemDto = mapperItemDto;
    }

    @GetMapping("/{id}")
    public ItemDto getItem(@PathVariable("id") Long itemId) {
        return mapperItemDto.toItemDto(itemService.getItem(itemId));
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemsOwned(userId).stream().map(mapperItemDto::toItemDto).toList();
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @Valid @RequestBody Item item) {
        return mapperItemDto.toItemDto(itemService.createItem(userId, item));
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("id") Long id) {
        return mapperItemDto.toItemDto(itemService.updateItem(userId, id, itemDto));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestParam String text) {
        return itemService.searchItemsByText(userId, text).stream().map(mapperItemDto::toItemDto).toList();
    }

}
