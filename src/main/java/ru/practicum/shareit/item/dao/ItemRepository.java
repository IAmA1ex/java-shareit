package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findItemsByOwner(Long userId);

    @Query(value = """
        select i
        from Item as i
        where i.available = true and (
            lower(i.name) like lower('%' || ?1 || '%') or
            lower(i.description) like lower('%' || ?1 || '%')
        )
    """)
    List<Item> findItemsBySubstring(String text);
}
