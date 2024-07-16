package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.mapper.BookingDtoMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.mapper.MapperItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ObjectValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ObjectValidator<ItemDto> itemValidator;
    private final MapperItemDto mapperItemDto;
    private final BookingRepository bookingRepository;
    private final BookingDtoMapper bookingDtoMapper;

    public ItemDto getItem(Long itemId) {
        log.info("Запрос на получение вещи с id = {}.", itemId);
        Optional<Item> opt = itemRepository.findById(itemId);
        Item item = opt.orElse(null);
        if (item == null) {
            throw new NotFoundException("Вещь с id = " + itemId + " не существует.");
        }
        log.info("Получена вещь {}.", item);
        return mapperItemDto.toItemDto(item);
    }

    public List<ItemDto> getItemsOwned(Long userId) {
        log.info("Запрос на получение вещей с owner = {}.", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        List<Item> items = itemRepository.findItemsByOwnerId(userId);
        List<ItemDto> itemDtos = items.stream().map(mapperItemDto::toItemDto).toList();
        for (ItemDto itemDto : itemDtos) {
            itemDto.setLastBooking(
                    bookingDtoMapper.toBookingForItem(
                            bookingRepository.getLastBooking(userId, itemDto.getId())
                    )
            );
            itemDto.setNextBooking(
                    bookingDtoMapper.toBookingForItem(
                            bookingRepository.getNextBooking(userId, itemDto.getId())
                    )
            );
        }
        log.info("Получено {} вещей.", itemDtos.size());
        return itemDtos;
    }

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Запрос на создание вещи {} с owner = {}.", itemDto, userId);
        Optional<User> optRenter = userRepository.findById(userId);
        if (optRenter.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        User user = optRenter.get();
        Item item = mapperItemDto.fromItemDto(itemDto);
        item.setOwner(user);
        Item created = itemRepository.save(item);
        log.info("Создана вещь {}.", created);
        return mapperItemDto.toItemDto(created);
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Запрос на обновление веши с itemId = {}, пользователем с userId = {}, полями {}.", itemId, userId, itemDto);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        Optional<Item> opt = itemRepository.findById(itemId);
        Item item = opt.orElse(null);
        if (item == null) {
            throw new NotFoundException("Вещь с id = " + itemId + " не существует.");
        }
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Пользователь с id = " + userId + " не владеет этой вещью с id = "
                    + itemId + ".");
        }
        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());
        itemValidator.validateObject(mapperItemDto.toItemDto(item));
        Item updated = itemRepository.save(item);
        log.info("Вещь обновлена {}.", updated);
        return mapperItemDto.toItemDto(updated);
    }

    public List<ItemDto> searchItemsByText(Long userId, String text) {
        log.info("Запрос на поиск вещей по тексту '{}'.", text);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        if (text.isBlank()) {
            log.info("Найдено 0 вещей. Пустой текст.");
            return List.of();
        }
        List<Item> items = itemRepository.findItemsBySubstring(text.toLowerCase());
        log.info("Найдено {} вещей.", items.size());
        return items.stream().map(mapperItemDto::toItemDto).toList();
    }
}
