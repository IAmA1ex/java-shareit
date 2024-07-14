package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class RAMItemRepository implements ItemRepository {

    private Long id = 1L;
    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item getItem(Long id) {
        return items.get(id);
    }

    @Override
    public List<Item> getItemsOwned(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(userId))
                .toList();
    }

    @Override
    public Item addItem(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        items.replace(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> getItemsBySearch(Long userId, String search) {
        return items.values().stream()
                .filter(item -> {
                    return item.getAvailable().equals(Boolean.TRUE) && (
                            item.getName().toLowerCase().contains(search.toLowerCase()) ||
                            item.getDescription().toLowerCase().contains(search.toLowerCase())
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean containsItem(Long itemId) {
        return items.containsKey(itemId);
    }

    private Long generateId() {
        return id++;
    }
}
