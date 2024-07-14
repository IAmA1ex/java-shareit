package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;
import java.util.List;

public interface ItemRepository {

    Item getItem(Long id);

    List<Item> getItemsOwned(Long userId);

    Item addItem(Item item);

    Item updateItem(Item item);

    List<Item> getItemsBySearch(Long userId, String search);

    boolean containsItem(Long itemId);
}
