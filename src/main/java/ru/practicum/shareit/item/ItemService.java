package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.validation.ObjectValidator;
import java.util.List;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ObjectValidator<Item> itemValidator;

    public ItemService(ItemRepository itemRepository, UserRepository userRepository, ObjectValidator<Item> objectValidator) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemValidator = objectValidator;
    }

    public Item getItem(Long itemId) {
        log.info("Запрос на получение вещи с id = {}.", itemId);
        if (!itemRepository.containsItem(itemId)) {
            throw new NotFoundException("Вещь с id = " + itemId + " не существует.");
        }
        Item item = itemRepository.getItem(itemId);
        log.info("Получена вещь {}.", item);
        return item;
    }

    public List<Item> getItemsOwned(Long userId) {
        log.info("Запрос на получение вещей с owner = {}.", userId);
        if (!userRepository.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        List<Item> items = itemRepository.getItemsOwned(userId);
        log.info("Получено {} вещей.", items.size());
        return items;
    }

    public Item createItem(Long userId, Item item) {
        log.info("Запрос на создание вещи {} с owner = {}.", item, userId);
        if (!userRepository.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        item.setOwner(userId);
        Item created = itemRepository.addItem(item);
        log.info("Создана вещь {}.", created);
        return created;
    }

    public Item updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Запрос на обновление веши с itemId = {}, пользователем с userId = {}, полями {}.", itemId, userId, itemDto);
        if (!userRepository.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        if (!itemRepository.containsItem(itemId)) {
            throw new NotFoundException("Вещь с id = " + itemId + " не существует.");
        }
        Item item = itemRepository.getItem(itemId);
        if (!item.getOwner().equals(userId)) {
            throw new ForbiddenException("Пользователь с id = " + userId + " не владеет этой вещью с id = "
                    + itemId + ".");
        }
        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        itemValidator.validateObject(item);

        Item updated = itemRepository.updateItem(item);
        log.info("Вещь обновлена {}.", updated);
        return updated;
    }

    public List<Item> searchItemsByText(Long userId, String text) {
        log.info("Запрос на поиск вещей по тексту '{}'.", text);
        if (!userRepository.containsUser(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не существует.");
        }
        if (text.isBlank()) {
            log.info("Найдено 0 вещей. Пустой текст.");
            return List.of();
        }
        List<Item> items = itemRepository.getItemsBySearch(userId, text);
        log.info("Найдено {} вещей.", items.size());
        return items;
    }
}
