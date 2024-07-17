package ru.practicum.shareit.booking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByRenterIdOrderByIdDesc(Long userId);

    List<Booking> findAllByOwnerIdOrderByIdDesc(Long userId);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and current_timestamp between b.startTime and b.endTime
        order by b.id asc
    """)
    List<Booking> getBookingsCurrentForRenter(Long userId);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2 and
            current_timestamp > b.endTime
        order by b.id desc
    """)
    List<Booking> getBookingsPastForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and (b.status = ?2 or b.status = ?3) and
            current_timestamp < b.startTime
        order by b.id desc
    """)
    List<Booking> getBookingsFutureForRenter(Long userId, BookingStatus status1, BookingStatus status2);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2
        order by b.id desc
    """)
    List<Booking> getBookingsWaitingForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.status = ?2
        order by b.id desc
    """)
    List<Booking> getBookingsRejectedForRenter(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and current_timestamp between b.startTime and b.endTime
        order by b.id asc
    """)
    List<Booking> getBookingsCurrentForOwner(Long userId);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2 and current_timestamp > b.endTime
        order by b.id desc
    """)
    List<Booking> getBookingsPastForOwner(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and (b.status = ?2 or b.status = ?3) and current_timestamp < b.startTime
        order by b.id desc
    """)
    List<Booking> getBookingsFutureForOwner(Long userId, BookingStatus status1, BookingStatus status2);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2
        order by b.id desc
    """)
    List<Booking> getBookingsWaitingForOwner(Long userId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.status = ?2
        order by b.id desc
    """)
    List<Booking> getBookingsRejectedForOwner(Long userId, BookingStatus status);

    @Query("""
        select not exists (select b
        from Booking as b
        where b.item = ?1 and b.status = ?2 and (
            ?3 between b.startTime and b.endTime or ?4 between b.startTime and b.endTime
        ))
    """)
    boolean availableAtTime(Item item, BookingStatus bookingStatus, LocalDateTime startTime, LocalDateTime endTime);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.item.id = ?2 and
        b.startTime < current_timestamp
        order by b.startTime desc
        limit 1
    """)
    Booking getLastBooking(Long userId, Long itemId);

    @Query("""
        select b
        from Booking as b
        where b.owner.id = ?1 and b.item.id = ?2 and
        current_timestamp < b.startTime and b.status = ?3
        order by b.startTime asc
        limit 1
    """)
    Booking getNextBooking(Long userId, Long itemId, BookingStatus status);

    @Query("""
        select b
        from Booking as b
        where b.renter.id = ?1 and b.item.id = ?2 and b.endTime < ?3
    """)
    List<Booking> isUserContainsCompletedBookingForItem(Long authorId, Long itemId, LocalDateTime created);
}
