package ru.practicum.shareit.request.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    List<ItemRequest> findAllByCreatorId(Long userRenterId);

    @Query("""
        select ir
        from ItemRequest as ir
        where ir.creator.id != :userRenterId
        order by ir.created desc
        limit :size
        offset :from
    """)
    List<ItemRequest> findAllByNotCreatorId(Long userRenterId, Long from, Long size);
}
